//package cn.shijinshi.fabricmanager.service.fabric.docker.helper;
//
//import com.github.dockerjava.api.command.ExecCreateCmdResponse;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//public class CmdHelperTest {
//    private CmdHelper cmdHelper;
//    private String containerId = "ca";
//
//    @Before
//    public void setUp() throws Exception {
//        DockerClientHelper clientHelper = new DockerClientHelper(null);
//        cmdHelper = clientHelper.getCmdHelper("testHost");
//    }
//
//    @Test
//    public void execCmd() {
//        String workingDir = "/etc/hyperledger";
//        String userName = "root";
//        List<String> env = null;
//
//        ExecCreateCmdResponse response = cmdHelper.createCmd(containerId, workingDir, userName, env, "pwd");
//        CmdHelper.Result result = cmdHelper.startCmd(response.getId(), false, null, 30);
//        System.out.println(result.toString());
//    }
//}