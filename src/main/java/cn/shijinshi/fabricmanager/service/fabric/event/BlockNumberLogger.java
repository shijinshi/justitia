package cn.shijinshi.fabricmanager.service.fabric.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jungle
 * @version V1.0
 * @date 2019/3/7 17:43
 * @Title: LogNumberObserver.java
 * @Package com.holmes.fabric.core.event
 * @Description: 回写区块号的观察者
 * copyright © 2019- holmes.com
 */
public class BlockNumberLogger implements BlockNumberObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockNumberLogger.class);
    private static BlockNumberLogger blockNumberLogger;

    private final Object objectLock = new Object();
    private final File blockNumberLogFile;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> channelBlockNumber = new ConcurrentHashMap<>();
    private final Properties prop = new Properties();

    public static synchronized BlockNumberLogger getInstance(File blockNumberLogFile) throws IOException {
        if (blockNumberLogger == null) {
            if (blockNumberLogFile == null) {
                throw new IllegalArgumentException("Parameter is null");
            }
            blockNumberLogger = new BlockNumberLogger(blockNumberLogFile);
        }
        return blockNumberLogger;
    }

    private BlockNumberLogger(File blockNumberLogFile) throws IOException {
        if (!blockNumberLogFile.exists()) {
            LOGGER.info("create block number log file in path :%s", blockNumberLogFile.getAbsolutePath());
            fileProber(blockNumberLogFile);
            if (!blockNumberLogFile.createNewFile()) {
                throw new IOException("Unable to create file " + blockNumberLogFile.getPath());
            }
        }
        this.blockNumberLogFile = blockNumberLogFile;

        LOGGER.debug("retrieval handled block number from log file:%s ", blockNumberLogFile.getAbsolutePath());
        InputStream in = new BufferedInputStream(new FileInputStream(blockNumberLogFile));
        prop.load(in);
        Set<Map.Entry<Object, Object>> entries = prop.entrySet();
        for (Map.Entry entry : entries) {
            String channelId = (String) entry.getKey();
            String blockNumber = (String) entry.getValue();

            long lastLoggedBlockNumber = blockNumber != null ? Long.parseLong(blockNumber) : 0L;
            ConcurrentHashMap<String, Long> blockNumberMap = new ConcurrentHashMap<>();
            blockNumberMap.put("lastLoggedBlockNumber", lastLoggedBlockNumber);
            blockNumberMap.put("lastBlockNumber", lastLoggedBlockNumber);
            channelBlockNumber.put(channelId, blockNumberMap);
        }

        new Thread(new UpdateChannelNumber()).start();
    }

    private boolean fileProber(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (fileProber(parentFile)) {
                return parentFile.mkdirs();
            } else {
                throw new IOException("Unable to create directory " + parentFile.getPath());
            }
        }
        return true;
    }

    @Override
    public Long getLastLoggedBlockNumber(String channelId) {
        Map<String, Long> blockNumberMap = channelBlockNumber.get(channelId);
        if (blockNumberMap != null) {
            return blockNumberMap.get("lastLoggedBlockNumber");
        } else {
            return 0L;
        }
    }

    @Override
    public void informChange(String channelId, Long lastBlockNumber) {
        ConcurrentHashMap<String, Long> blockNumberMap = channelBlockNumber.get(channelId);
        if (blockNumberMap != null) {
            blockNumberMap.replace("lastBlockNumber", lastBlockNumber);
        } else {
            blockNumberMap = new ConcurrentHashMap<>();
            blockNumberMap.put("lastLoggedBlockNumber", lastBlockNumber - 1);
            blockNumberMap.put("lastBlockNumber", lastBlockNumber);
            channelBlockNumber.put(channelId, blockNumberMap);
        }
        synchronized (objectLock) {
            objectLock.notifyAll();
        }
    }

    private class UpdateChannelNumber implements Runnable {
        @Override
        public void run() {
            while (true) {
                boolean updated = false;
                Set<Map.Entry<String, ConcurrentHashMap<String, Long>>> entries = channelBlockNumber.entrySet();
                for (Map.Entry entry : entries) {
                    String channelId = (String) entry.getKey();
                    ConcurrentHashMap<String, Long> blockNumberMap = (ConcurrentHashMap<String, Long>) entry.getValue();
                    Long lastBlockNumber = blockNumberMap.get("lastBlockNumber");
                    Long lastLoggedBlockNumber = blockNumberMap.get("lastLoggedBlockNumber");
                    if (lastBlockNumber > lastLoggedBlockNumber) {
                        if (lastBlockNumber != (lastLoggedBlockNumber + 1)) {
                            LOGGER.warn(String.format("The expected block number is %d, but the actual block number is %d, " +
                                    "the next time we will start at %d.", lastLoggedBlockNumber, lastBlockNumber, lastBlockNumber));
                        }

                        try {
                            FileOutputStream oFile = new FileOutputStream(blockNumberLogFile);
                            prop.setProperty(channelId, lastBlockNumber.toString());
                            prop.store(oFile, "The latestBlockNumber block number");
                            oFile.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            LOGGER.warn("unable to log latest block number :%d", lastBlockNumber);
                        }

                        blockNumberMap.replace("lastLoggedBlockNumber", lastBlockNumber);
                        updated = true;
                    }
                }

                if (!updated) {
                    synchronized (objectLock) {
                        try {
                            objectLock.wait(300000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
