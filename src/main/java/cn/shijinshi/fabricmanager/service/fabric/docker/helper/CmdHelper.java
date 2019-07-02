package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CmdHelper extends DockerHelper{

    public CmdHelper(DockerClient client) {
        super(client);
    }

    /**
     * 创建CMD
     *
     * @param containerId 容器id
     * @param workingDir  命令执行目录
     * @param userName    执行命令的用户
     * @param env         运行时环境变量
     * @param cmd         待执行命令
     * @return
     */
    public ExecCreateCmdResponse createCmd(String containerId, String workingDir, String userName, List<String> env, String... cmd) {
        ExecCreateCmd createCmd = client.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true);
        //命令执行路径
        if (workingDir != null && !workingDir.isEmpty()) {
            createCmd.withWorkingDir(workingDir);
        }
        //执行命令的用户身份
        if (userName != null && !userName.isEmpty()) {
            createCmd.withUser(userName);
        }
        //运行时环境变量
        if (env != null && !env.isEmpty()) {
            createCmd.withEnv(env);
        }
        return createCmd.exec();
    }

    /**
     * 运行CMD命令
     *
     * @param cmdId   createCmd方法返回的执行终端id
     * @param detach  是否在后台执行
     * @param input   执行命令需要输入的内容
     * @param timeout 等待超时时间，单位为秒。超时则不等待后续执行结果返回
     * @throws InterruptedException
     */
    public Result startCmd(String cmdId, boolean detach, String input, long timeout) {
        ExecStartCmd startCmd = client.execStartCmd(cmdId).withDetach(false);
        //命令是否在后台执行
        startCmd.withDetach(detach);
        //设置输入流
        if (input != null && !input.isEmpty()) {
            startCmd.withStdIn(new ByteArrayInputStream(input.getBytes()));
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ExecStartResultCallback callback = startCmd.exec(new ExecStartResultCallback(outputStream, errorStream));

        Result result = new Result();
        try {
            if (timeout == 0) {
                callback.awaitCompletion();
            } else {
                callback.awaitCompletion(timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.info(e);
        } finally {
            InspectExecResponse response = client.inspectExecCmd(cmdId).exec();
            result.setPrintInfo(new String(outputStream.toByteArray()));
            result.setErrorInfo(new String(errorStream.toByteArray()));
            if (response.isRunning()){
                result.setRunning(true);
            } else {
                result.setExitValue(response.getExitCode());
            }

            try {
                outputStream.close();
                errorStream.close();
            } catch (IOException e) {
                LOGGER.info(e);
            }

            startCmd.close();

        }
        return result;
    }

    public class Result {
        private boolean success;
        private boolean running;     //还没有执行完成，但是超时退出了
        private int exitValue;
        private String printInfo;
        private String errorInfo;

        public Result() {
        }

        public Result(int exitValue, String printInfo, String errorInfo) {
            setExitValue(exitValue);
            this.printInfo = printInfo;
            this.errorInfo = errorInfo;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            if (isRunning()) {
                this.success = false;
                this.exitValue = -1;
            }
            this.running = running;
        }

        public boolean isSuccess() {
            return success;
        }


        public int getExitValue() {
            return exitValue;
        }

        public void setExitValue(int exitValue) {
            this.exitValue = exitValue;
            this.success = exitValue == 0;
        }

        public String getPrintInfo() {
            return printInfo;
        }

        public void setPrintInfo(String printInfo) {
            this.printInfo = printInfo;
        }

        public String getErrorInfo() {
            return errorInfo;
        }

        public void setErrorInfo(String errorInfo) {
            this.errorInfo = errorInfo;
        }

        @Override
        public String toString() {
            return "success:" + success + "\n" +
                    "running:" + running + "\n" +
                    "exitValue:" + exitValue + "\n" +
                    "printInfo:" + printInfo + "\n" +
                    "errorInfo:" + errorInfo + "\n";
        }
    }
}