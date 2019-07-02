import cn.shijinshi.fabricmanager.Application;
import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.service.fabric.helper.ChannelHelper;
import cn.shijinshi.fabricmanager.service.fabric.helper.exception.FabricClientException;
import cn.shijinshi.fabricmanager.service.fabric.tools.ConfigTxGen;
import cn.shijinshi.fabricmanager.service.fabric.tools.FabricToolsException;
import cn.shijinshi.fabricmanager.service.utils.StringConverter;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class SpringTest {

    @Autowired
    private FabricCaUserService fabricCaUserService;
    @Test
    public void test() throws NotFoundBySqlException {
        UserAndCerts userCerts = fabricCaUserService.getUserCerts("orderer4", "CA_server");
        System.out.println();
    }


    @Autowired
    private FabricCaServerService fabricCaServerService;
    @Test
    public void testFabricCaServer() {
        List<String> ca_server = fabricCaServerService.selectCaChildServerName("CA_server");
        System.out.println();
    }


    @Autowired
    private StringConverter stringConverter;
    @Test
    public void converterString() {
        String decrypt = stringConverter.decrypt("X03MO1qnZdYdgyfeuILPmQ==");
        System.out.println(decrypt);
    }

    @Autowired
    private ChannelHelper  channelHelper;
    @Test
    public void getChnanelConfig() throws TransactionException, FabricClientException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvalidProtocolBufferException, InvocationTargetException {
        byte[] channelConfigurationBytesFromOrderer = channelHelper.getChannelConfigurationBytesFromOrderer("testchainid", "orderer0");
        System.out.println();
    }

    @Test
    public void fadfad() throws TransactionException, FabricClientException, InvalidArgumentException {
        byte[] mychannels = channelHelper.getChannelConfigurationBytes("mychannel");
        System.out.println();
    }



    @Test
    public void sdafdasfg() throws TransactionException, FabricClientException, InvalidArgumentException {
        channelHelper.submitChannelConfig("testchainid","orderer0", null, null);
        System.out.println();
    }

    @Autowired
    private ConfigTxGen configTxGen;
    @Test
    public void dsfadgdsgfdgfaggfh() throws FabricToolsException {
        configTxGen.createChannelTx("mychannel", "myconsor");
        System.out.println();
    }
}
