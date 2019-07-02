package cn.shijinshi.fabricmanager.service.fabric.event;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.hyperledger.fabric.sdk.helper.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * @author jungle
 * @version V1.0
 * @date 2019/3/7 17:33
 * @Title: ChainCodeEventHandler.java
 * @Package com.holmes.fabric.core.event
 * @Description: ChainCode事件处理器
 * copyright © 2019- holmes.com
 */
public class ChainCodeEventHandler implements Runnable {
    /**
     * 事件订阅者列表。
     */
    private final transient LinkedList<ChainCodeEventSubscriber> codeEventSubscribers = new LinkedList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    /**
     * 已处理的区块号码。
     */
    private final AtomicLong handledBlockNumber = new AtomicLong(-1);
    /**
     * 区块号码记录器。
     */
    private BlockNumberObserver blockNumberLogger;
    /**
     * 运行标识，等待添加停止功能。
     */
    private boolean runningFlag = true;
    /**
     * 已缓存的区块事件。
     */
    private ConcurrentHashMap<Long, BlockEvent> cachedBlockEvents = new ConcurrentHashMap<>();

    private EventPostProcessor callBack;
    private String channelId;

    public ChainCodeEventHandler(BlockNumberLogger blockNumberLogger, String channelId) {
        this.channelId = channelId;
        this.blockNumberLogger = blockNumberLogger;
        handledBlockNumber.set(blockNumberLogger.getLastLoggedBlockNumber(channelId));
    }

    /**
     * 从交易事件中抽取出链码事件。
     *
     * @param transactionEvents 交易事件集{@link Iterable}
     *
     * @return LinkedList {@link ChaincodeEvent}
     */
    private LinkedList<ChaincodeEvent> extractChaincodeEventsFromTransactionEvents(Iterable<BlockEvent.TransactionEvent> transactionEvents) {
        LinkedList<ChaincodeEvent> chaincodeEvents = new LinkedList<>();
        for (BlockEvent.TransactionEvent transactionEvent : transactionEvents) {
            if (!transactionEvent.isValid()) {
                continue;
            }
            for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo info : transactionEvent.getTransactionActionInfos()) {
                ChaincodeEvent event = info.getEvent();
                if (null != event) {
                    chaincodeEvents.add(event);
                }
            }
        }
        return chaincodeEvents;
    }

    /**
     * 将事件信息与订阅者进行匹配。
     *
     * @param chaincodeEvents      链码事件
     * @param codeEventSubscribers 订阅者列表
     *
     * @return List {@link MatchPair}
     */
    private List<MatchPair> matchEventAndSubscriber(LinkedList<ChaincodeEvent> chaincodeEvents, LinkedList<ChainCodeEventSubscriber> codeEventSubscribers) {
        List<MatchPair> matches = new LinkedList<>(); //Find matches.
        for (ChainCodeEventSubscriber eventSubscriber : codeEventSubscribers) {
            for (ChaincodeEvent chaincodeEvent : chaincodeEvents) {
                if (eventSubscriber.isMatched(chaincodeEvent.getChaincodeId(), chaincodeEvent.getEventName())) {
                    matches.add(new MatchPair(eventSubscriber, chaincodeEvent));
                }
            }
        }
        return matches;
    }

    /**
     * 订阅ChainCode事件。
     *
     * @param chaincodeId            链码id
     * @param eventName              事件名
     * @param chaincodeEventListener 事件处理器
     */
    public void subscribe(Pattern chaincodeId, Pattern eventName, ChaincodeEventListener chaincodeEventListener) {
        String handle = "MY_TAG" + Utils.generateUUID() + "MY_TAG";
        codeEventSubscribers.add(new ChainCodeEventSubscriber(chaincodeId, eventName, chaincodeEventListener, handle));
    }

    /**
     * 添加事件后置处理，新区块中事件不为空，并且处理完了才会调用这个。
     */
    public void addPostProcessor(EventPostProcessor callBack) {
        this.callBack = callBack;
    }


    /**
     * 缓存新的区块事件。
     *
     * @param blockNumber 区块号码
     * @param event       事件
     */
    public void cacheNewBlockEvent(Long blockNumber, BlockEvent event) {
        if (blockNumber > handledBlockNumber.get()) {
            cachedBlockEvents.putIfAbsent(blockNumber, event);
        }
    }

    @Override
    public void run() {
        while (runningFlag) {
            Long expectedBlockNumber = handledBlockNumber.get() + 1;
            BlockEvent blockEvent = cachedBlockEvents.get(expectedBlockNumber);
            if (blockEvent != null) {
                handleEvent(blockEvent);
                handledBlockNumber.incrementAndGet();
                blockNumberLogger.informChange(channelId, handledBlockNumber.get());
                cachedBlockEvents.remove(expectedBlockNumber);
            } else {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 处理事件。
     *
     * @param blockEvent {@link BlockEvent}
     */
    private void handleEvent(BlockEvent blockEvent) {
        Iterable<BlockEvent.TransactionEvent> transactionEvents = blockEvent.getTransactionEvents();
        LinkedList<ChaincodeEvent> chaincodeEvents = extractChaincodeEventsFromTransactionEvents(transactionEvents);
        if (!chaincodeEvents.isEmpty()) {
            List<MatchPair> matchPairs = matchEventAndSubscriber(chaincodeEvents, codeEventSubscribers);
            informsAllMatchSubscribers(matchPairs, blockEvent);
        }
    }

    /**
     * 从匹配的订阅者列表里构建通知任务列表。
     *
     * @param matchPairs     匹配列表
     * @param blockEvent     链码事件
     * @param countDownLatch 倒数计数器
     *
     * @return List {@link Callable}
     */
    private List<Callable<Void>> buildInformTasksFromMatches(List<MatchPair> matchPairs, BlockEvent blockEvent, CountDownLatch countDownLatch) {
        List<Callable<Void>> callableList = new ArrayList<>();
        if (matchPairs != null) {
            for (MatchPair match : matchPairs) {
                ChainCodeEventSubscriber subscriber = match.subscriber;
                callableList.add(new ListenerInformTask(
                        subscriber.getHandle(),
                        blockEvent,
                        match.event,
                        countDownLatch,
                        subscriber.getListener()
                ));
            }
        }
        return callableList;
    }

    /**
     * 通知所有订阅者。
     *
     * @param matchPairs 匹配的订阅者与事件
     * @param blockEvent 区块事件
     */
    private void informsAllMatchSubscribers(List<MatchPair> matchPairs, BlockEvent blockEvent) {
        if (matchPairs != null && matchPairs.size() > 0) {
            CountDownLatch countDownLatch = new CountDownLatch(matchPairs.size());
            List<Callable<Void>> callableList = buildInformTasksFromMatches(matchPairs, blockEvent, countDownLatch);
            try {
                executorService.invokeAll(callableList);
                countDownLatch.await();
                if (this.callBack != null) {
                    callBack.callBack(blockEvent);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class MatchPair {
        private final ChainCodeEventSubscriber subscriber;
        private final ChaincodeEvent event;

        private MatchPair(ChainCodeEventSubscriber subscriber, ChaincodeEvent event) {
            this.subscriber = subscriber;
            this.event = event;
        }
    }

    private class ListenerInformTask implements Callable<Void> {

        private String handle;
        private BlockEvent blockEvent;
        private ChaincodeEvent chaincodeEvent;
        private CountDownLatch countDownLatch;
        private ChaincodeEventListener listener;

        ListenerInformTask(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent, CountDownLatch countDownLatch, ChaincodeEventListener listener) {
            this.handle = handle;
            this.blockEvent = blockEvent;
            this.chaincodeEvent = chaincodeEvent;
            this.countDownLatch = countDownLatch;
            this.listener = listener;
        }

        @Override
        public Void call() {
            listener.received(handle, blockEvent, chaincodeEvent);
            countDownLatch.countDown();
            return null;
        }
    }
}
