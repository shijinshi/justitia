package cn.shijinshi.fabricmanager.service.helper.ssh;

import cn.shijinshi.fabricmanager.service.helper.ssh.exception.CallShellException;
import com.jcraft.jsch.JSchException;
import org.junit.Test;

import java.io.IOException;

public class CallRemoteShellTest {
    private static final String userName = "dev";
    private static final String password = "1qaz@WSX";
    private static final String ip = "192.168.32.116";
    private static final int sshPort = 22;

    private static final String rootPwd = "1qaz@WSX";

    @Test
    public void execCmd() throws JSchException, IOException, CallShellException, InterruptedException {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
        CallShell.Result result = shell.execCmd(command);
        System.out.println(result.isSuccess());
        System.out.println(result.getPrintInfo());
        System.out.println(result.getErrorInfo());
    }

    @Test
    public void execCmd1() throws JSchException, IOException, CallShellException, InterruptedException {
        String command = "ls";
        String output = "";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
        shell.execCmd(command, output);
    }

    @Test
    public void execCmd2() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void execCmd3() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void execWithRoot() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void execWithRoot1() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void execWithRoot2() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void execWithRoot3() {
        String command = "ls";
        CallRemoteShell shell = new CallRemoteShell(userName, password, ip, sshPort);
    }

    @Test
    public void sendFile() {
    }
}