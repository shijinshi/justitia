package cn.shijinshi.fabricmanager.service.fabric.event;

import org.hyperledger.fabric.sdk.ChaincodeEventListener;

import java.util.regex.Pattern;

/**
 * @author jungle
 * @version V1.0
 * @date 2019/3/7 18:01
 * @Title: ChainCodeEventSubscriber.java
 * @Package com.holmes.fabric.core.event
 * @Description: 链码事件订阅者
 * copyright © 2019- holmes.com
 */
public class ChainCodeEventSubscriber {

    private final Pattern chainCodeIdPattern;

    private final Pattern eventNamePattern;

    private final ChaincodeEventListener listener;

    private final String handle;

    public ChainCodeEventSubscriber(Pattern chainCodeIdPattern, Pattern eventNamePattern, ChaincodeEventListener listener, String handle) {
        this.chainCodeIdPattern = chainCodeIdPattern;
        this.eventNamePattern = eventNamePattern;
        this.listener = listener;
        this.handle = handle;
    }

    public boolean isMatched(String chainCodeId, String eventName) {

        boolean isChainCodeMatched = chainCodeIdPattern.matcher(chainCodeId)
                                                       .matches();
        boolean isEventNameMatched = eventNamePattern.matcher(eventName).matches();

        return isChainCodeMatched && isEventNameMatched;
    }

    /**
     * 获取 listener。
     *
     * @return {@link #listener}
     */
    public ChaincodeEventListener getListener() {
        return listener;
    }

    /**
     * 获取 handle。
     *
     * @return {@link #handle}
     */
    public String getHandle() {
        return handle;
    }
}
