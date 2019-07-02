//package cn.shijinshi.fabricmanager.service.fabric.docker.helper;
//
//import com.github.dockerjava.api.model.Network;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//public class NetworkHelperTest {
//    private NetworkHelper networkHelper;
//    private String networkName = "fabric_ca_network";
//
//    @Before
//    public void setUp() throws Exception {
//        DockerClientHelper clientHelper = new DockerClientHelper(null);
//        networkHelper = clientHelper.getNetworkHelper("testHost");
//    }
//
//
//
//    @Test
//    public void createNetwork() throws DockerClientException {
//        networkHelper.createNetwork(networkName);
//    }
//
//    @Test
//    public void removeNetwork() throws DockerClientException {
//        networkHelper.removeNetwork(networkName);
//    }
//
//    @Test
//    public void listNetwork() {
//        List<Network> networks = networkHelper.listNetwork();
//        System.out.println();
//    }
//
//    @Test
//    public void inspectNetwork() {
//        Network network = networkHelper.inspectNetwork(networkName);
//        System.out.println();
//    }
//
//    @Test
//    public void connectToNetwork() {
//
//    }
//
//    @Test
//    public void disconnectFromNetwork() {
//    }
//}