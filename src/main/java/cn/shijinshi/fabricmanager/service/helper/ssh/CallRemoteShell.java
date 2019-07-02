package cn.shijinshi.fabricmanager.service.helper.ssh;

import cn.shijinshi.fabricmanager.service.helper.ssh.exception.CallShellException;
import com.jcraft.jsch.*;

import java.io.*;
import java.util.Map;
import java.util.concurrent.Future;

public class CallRemoteShell extends CallShell {
    private static long timeout;  //milliseconds

    private Session session;
    private String user;
    private String password;
    private String host;
    private int port;

    private static class MyUserInfo implements UserInfo {

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassword(String s) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return true;
        }

        @Override
        public void showMessage(String s) {

        }
    }


    /**
     * 通过ssh将脚本文件和证书文件发送到需要作为节点的虚拟机上
     *
     * @param user     虚拟机ssh用户名
     * @param password 密码
     * @param host     虚拟机ip
     * @param port     端口
     * @param timeout  命令执行的最大时间，单位毫秒
     */
    public CallRemoteShell(String user, String password, String host, int port, long timeout) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        CallRemoteShell.timeout = timeout;
    }

    public CallRemoteShell(String user, String password, String host, int port) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        CallRemoteShell.timeout = 30000;
    }

    private Channel connect(String type) throws CallShellException {
        Channel channel = null;
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(user, host, port);
        } catch (JSchException e) {
            throw new CallShellException("Failed to create jsch to " + host + ":" + port + "\n" + e);
        }
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");   //第一次链接的时候不做检查
        session.setUserInfo(new MyUserInfo());

        try {
            int retryCount;
            for (retryCount = 0; !session.isConnected() && retryCount < 3; retryCount++) {
                session.connect(30000);
            }
            if (retryCount >= 3) {
                throw new CallShellException("Failed to link " + session.getHost() + ":" + session.getPort());
            }

            channel = session.openChannel(type);
        } catch (Exception e) {
            throw new CallShellException("Failed to link host " + host + ":" + port + ", user:" + user + ",password:" + password);
        }
        return channel;
    }

    private void disConnect(Session session, Channel channel) {
        session.disconnect();
        channel.disconnect();
    }

    //---------------------------------------------执行命令---------------------------------------------

    /**
     * 在远程主机上执行shell命令
     *
     * @param command 被执行的命令
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execCmd(String command) throws IOException, JSchException, CallShellException, InterruptedException {
        return execCmd(command, null, null);
    }


    /**
     * 在远程主机上执行shell命令
     *
     * @param command 被执行的命令
     * @param env     环境变量
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execCmd(String command, Map<String, String> env) throws IOException, JSchException, CallShellException, InterruptedException {
        return execCmd(command, env, null);
    }


    /**
     * 在远程主机上执行shell命令
     *
     * @param command 被执行的命令
     * @param output  执行命令需要输入的内容
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execCmd(String command, String output) throws IOException, JSchException, CallShellException, InterruptedException {
        return execCmd(command, null, output);
    }

    /**
     * 在远程主机上执行shell命令
     *
     * @param command 被执行的命令
     * @param env     环境变量
     * @param output  执行命令需要输入的内容
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execCmd(String command, Map<String, String> env, String output) throws IOException, JSchException, CallShellException, InterruptedException {
        Channel channel = connect("exec");
        if (channel == null) {
            throw new CallShellException("Remote host connection failed");
        }

        ChannelExec channelExec = (ChannelExec) channel;
        channelExec.setCommand(command + "\n");   //加上\n以确保这是一条完整命令

        //设置执行时的环境变量
        if (env != null && !env.isEmpty()) {
            for (Map.Entry<String, String> entry : env.entrySet())
                channelExec.setEnv(entry.getKey(), entry.getValue());
        }
        //必须在connect之前获取流对象
        InputStream inputStream = channelExec.getInputStream();
        InputStream errStream = channelExec.getErrStream();
        //连接远程shell，执行命令
        channelExec.connect();
        //写入需要输入的值
        if (output != null) {
            OutputStream out = channelExec.getOutputStream();
            out.write((output + "\n").getBytes());
            out.flush();
        }

        //读取执行结果
        Map<String, Future> futureMap = readResult(channelExec, inputStream, errStream);
        channelExec.wait(timeout);
        Result res = getResult(futureMap, channelExec.getExitStatus());
        disConnect(session, channelExec);
        return res;
    }


    /**
     * 在执行的命令前附加sudo，并输入密码来获取权限
     *
     * @param command  被执行的命令
     * @param password sudo命令密码
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execWithRoot(String command, String password) throws JSchException, IOException, CallShellException, InterruptedException {
        command = "sudo -S -p '' " + command;
        return execCmd(command, null, password);
    }


    /**
     * 在执行的命令前附加sudo，并输入密码来获取权限
     *
     * @param command  被执行的命令
     * @param output   执行命令需要输入的内容
     * @param password sudo命令密码
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execWithRoot(String command, String output, String password) throws JSchException, IOException, CallShellException, InterruptedException {
        command = "sudo -S -p '' " + command;
        output = password + "\n" + output;
        return execCmd(command, null, output);
    }


    /**
     * 在执行的命令前附加sudo，并输入密码来获取权限
     *
     * @param command  被执行的命令
     * @param env      环境变量
     * @param password sudo命令密码
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execWithRoot(String command, Map<String, String> env, String password) throws JSchException, IOException, CallShellException, InterruptedException {
        command = "sudo -S -p '' " + command;
        return execCmd(command, env, password);
    }


    /**
     * 在执行的命令前附加sudo，并输入密码来获取权限
     *
     * @param command  被执行的命令
     * @param env      环境变量
     * @param output   执行命令需要输入的内容
     * @param password sudo命令密码
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws CallShellException
     */
    public Result execWithRoot(String command,Map<String, String> env, String output, String password) throws JSchException, IOException, CallShellException, InterruptedException {
        command = "sudo -S -p '' " + command;
        output = password + "\n" + output;
        return execCmd(command, env, output);
    }


    //---------------------------------------------上传文件---------------------------------------------

    /**
     * 发送本地文件到远程主机
     *
     * @param filePath  本地文件路径，也可以是一个目录的路径
     * @param directory 远程主机接收文件的存放路径
     * @return
     * @throws CallShellException
     * @throws IOException
     * @throws SftpException
     * @throws JSchException
     */
    public Result sendFile(String filePath, String directory) throws CallShellException, IOException,
            SftpException, JSchException, InterruptedException {

        ChannelSftp channelSftp = (ChannelSftp) connect("sftp");
        if (channelSftp == null) {
            throw new CallShellException("Remote host connection failed");
        }

        //必须在connect之前获取流对象
        InputStream inputStream = channelSftp.getInputStream();
        channelSftp.connect();

        String[] dirs = directory.split(File.separator);
        String dir = "";
        for (int i = 0; i < dirs.length; i++) {
            dir = dir + dirs[i] + File.separator;
            createDirectory(dir, channelSftp);
        }

        upload(filePath, directory, channelSftp);

        Map<String, Future> futureMap = readResult(channelSftp, inputStream, null);
        channelSftp.wait(timeout);
        Result res = getResult(futureMap, channelSftp.getExitStatus());
        disConnect(session, channelSftp);
        return res;
    }

    /**
     * 在远程主机上创建文件夹，但连接远程主机的用户需要有创建路径的权限
     *
     * @param directory 文件夹路径，可以是相对（home）路径或者绝对路径
     * @param sftp
     * @throws CallShellException
     * @throws SftpException
     */
    private void createDirectory(String directory, ChannelSftp sftp) throws CallShellException, SftpException {
        //判断路径directory是否存在
        //ChannelSftp无法去判读远程linux主机的文件路径，我们用ls命令测试
        try {
            sftp.ls(directory);
        } catch (SftpException e) {
            if ("No such file".equals(e.getMessage())) {
                try {
                    sftp.mkdir(directory);
                } catch (SftpException e1) {
                    if ("Permission denied".equals(e1.getMessage())) {
                        throw new CallShellException("Permission denied");
                    } else {
                        throw e1;
                    }
                }
            }
        }
    }

    private void upload(String filePath, String directory, ChannelSftp sftp) throws CallShellException, FileNotFoundException,
            UnsupportedEncodingException, SftpException {

        File file = new File(filePath);
        if (file.exists()) {
            directory = directory + File.separator + file.getName();
            if (file.isDirectory()) {
                createDirectory(directory, sftp);

                File[] files = file.listFiles();
                if (files != null) {
                    for (File file2 : files) {
                        String fileDir = file2.getAbsolutePath();
                        upload(fileDir, directory, sftp);
                    }
                }
            } else {
                InputStream ins = new FileInputStream(file);

                //中文名称的
                String fileName = new String(directory.getBytes(), "UTF-8");
                sftp.setFilenameEncoding("UTF-8");
                sftp.put(ins, fileName);
            }
        } else {
            throw new FileNotFoundException("No such file or directory," + file.getPath());
        }
    }
}
