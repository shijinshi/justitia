package cn.shijinshi.fabricmanager.service.fabric.event;

import org.hyperledger.fabric.sdk.BlockEvent;

/**
 * @author jungle
 * @version V1.0
 * @date 2019/3/19 10:48
 * @Title: ChainCodeEventProcessedCallBack.java
 * @Package com.holmes.fabric.core.event
 * @Description: 链码事件处理完回调
 * copyright © 2019- holmes.com
 */
public interface EventPostProcessor {
    void callBack(BlockEvent event);
}
