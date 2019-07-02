package cn.shijinshi.fabricmanager.service.helper.ssh;

import cn.shijinshi.fabricmanager.service.helper.ssh.exception.CallShellException;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

public class CallLocalShell extends CallShell {

    private long timeout;
    private TimeUnit unit;

    /**
     * @param timeout 执行命令最大时间
     * @param unit    执行命令时间单位
     */
    public CallLocalShell(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    public CallLocalShell() {
        this.timeout = 10;
        this.unit = TimeUnit.SECONDS;
    }

    public void setTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    /**
     * 调用本地shell执行外部程序/命令
     *
     * @param command shell命令，也可以是本地shell脚本名称
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Result execCmd(String command) throws IOException, InterruptedException {
        return execCmd(command, null, null);
    }

    /**
     * 调用本地shell执行外部程序/命令
     *
     * @param command shell命令，也可以是本地shell脚本名称
     * @param envp    环境变量
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Result execCmd(String command, String[] envp) throws IOException, InterruptedException {
        return execCmd(command, envp, null);
    }


    /**
     * 调用本地shell执行外部程序/命令
     *
     * @param command shell命令，也可以是本地shell脚本名称
     * @param dir     指定执行路径
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Result execCmd(String command, File dir) throws IOException, InterruptedException {
        return execCmd(command, null, dir);
    }


    /**
     * 调用本地shell执行外部程序/命令
     *
     * @param command shell命令，也可以是本地shell脚本名称
     * @param envp    环境变量
     * @param dir     指定执行路径
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Result execCmd(String command, String[] envp, File dir) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command, envp, dir);
        Map<String, Future> futureMap = readResult(process);
        int exitValue;
        if (process.waitFor(timeout, unit)) {
            exitValue = process.exitValue();
        } else {
            process.destroy();
            exitValue = process.exitValue();
        }
        //exitValue : 143 表示没有等待命令执行完成，而是通过destroy方法强制退出了
        return getResult(futureMap, exitValue);
    }


    public Result execScripts(File scripts, File dir) throws CallShellException, IOException, InterruptedException {
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
            throw new CallShellException("Windows system cannot execute script files with .sh suffix");
        }
        if (!scripts.exists()) {
            throw new FileNotFoundException("No such file or directory:" + scripts.getPath());
        }
        String command = "./" + scripts.getName();
        return execCmd(command, dir);
    }

}
