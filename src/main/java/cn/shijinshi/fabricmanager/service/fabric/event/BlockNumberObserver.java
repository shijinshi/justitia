package cn.shijinshi.fabricmanager.service.fabric.event;

/**
 * @author jungle
 * @version V1.0
 * @date 2019/3/7 17:39
 * @Title: BlockNumberObserver.java
 * @Package com.holmes.fabric.core.event
 * @Description: 区块号码监听
 * copyright © 2019- holmes.com
 */
public interface BlockNumberObserver {

    /**
     * 通知新的区块变更。
     *
     * @param newBlockNumber 新区块号码
     */
    void informChange(String channelId, Long newBlockNumber);

    /**
     * 获取最后一次通知的号码。
     *
     * @return last logged number
     */
    Long getLastLoggedBlockNumber(String channelId);
}
