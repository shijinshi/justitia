package cn.shijinshi.fabricmanager.service.fabric.channel;

import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.service.fabric.event.BlockNumberLogger;
import cn.shijinshi.fabricmanager.service.fabric.event.BlockNumberObserver;
import cn.shijinshi.fabricmanager.service.fabric.helper.HFClientHelper;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Set;

//@Service
public class BlockListener {

    private static BlockNumberObserver blockNumberObserver;
    private static HFClient client;

    private final HFClientHelper clientHelper;

    @Autowired
    public BlockListener(HFClientHelper clientHelper) throws IOException {
        blockNumberObserver = BlockNumberLogger.getInstance(new File(ExternalResources.getTemp("channelNumber.bat")));
        this.clientHelper = clientHelper;
    }

    private HFClient getClient() throws FabricClientException {
        if (client == null) {
            client = clientHelper.createHFClient();
        }
        return client;
    }

    /**
     * 获取通道对象
     *
     * @param channelName 通道名称
     * @return
     * @throws FabricClientException    Fabric客户端对象创建失败
     * @throws InvalidArgumentException Fabric通道对象创建失败
     * @throws TransactionException
     */
    private Channel getChannel(String channelName, Set<String> peersName) throws FabricClientException, InvalidArgumentException, TransactionException {
        Channel.PeerOptions options = Channel.PeerOptions.createPeerOptions();
        options.startEvents(blockNumberObserver.getLastLoggedBlockNumber(channelName));
        return clientHelper.createChannel(getClient(), channelName, Organization.DEFAULT_ORDERER_NAME, peersName, options);
    }
}
