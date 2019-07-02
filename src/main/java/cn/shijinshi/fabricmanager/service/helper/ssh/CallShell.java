package cn.shijinshi.fabricmanager.service.helper.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CallShell {
    private static final Logger log = Logger.getLogger(CallShell.class);

    private static final int DEFAULT_TIME = 10;
    private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

    /**
     * 调用shell返回结果
     */
    public class Result {
        private boolean success;
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

        public boolean isSuccess() {
            return success;
        }


        public int getExitValue() {
            return exitValue;
        }

        public void setExitValue(int exitValue) {
            this.exitValue = exitValue;
            if (exitValue == 0) {
                this.success = true;
            } else {
                this.success = false;
            }
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
                    "exitValue:" + exitValue + "\n" +
                    "printInfo:" + printInfo + "\n" +
                    "errorInfo:" + errorInfo + "\n";
        }

    }


    protected Map<String, Future> readResult(Process process) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Future<String> inputFuture = threadPool.submit(new Reader(process.getInputStream(), process));
        Future<String> errorFuture = threadPool.submit(new Reader(process.getErrorStream(), process));

        Map<String, Future> map = new HashMap<>();
        map.put("inputFuture", inputFuture);
        map.put("errorFuture", errorFuture);
        return map;
    }

    protected Map<String, Future> readResult(Channel channel, InputStream inputStream, InputStream errorStream) {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Future<String> inputFuture = threadPool.submit(new Reader(inputStream, channel));
        Future<String> errorFuture = threadPool.submit(new Reader(errorStream, channel));

        Map<String, Future> map = new HashMap<>();
        map.put("inputFuture", inputFuture);
        map.put("errorFuture", errorFuture);
        return map;
    }

    protected Result getResult(Map<String, Future> futures, int exitValue) {
        return getResult(futures, exitValue, DEFAULT_TIME, DEFAULT_UNIT);
    }

    protected Result getResult(Map<String, Future> futures, int exitValue , long timeout, TimeUnit unit) {
        Future<String> inputFuture = futures.get("inputFuture");
        Future<String> errorFuture = futures.get("errorFuture");
        String inputInfo = null;
        String errorInfo = null;
        try {
            if (inputFuture != null) inputInfo = inputFuture.get(timeout, unit);
            if (errorFuture != null) errorInfo = errorFuture.get(timeout, unit);
        } catch (ExecutionException | InterruptedException e) {
            log.warn(e);
        } catch (TimeoutException e) {
            log.info(e);
        }

        return new Result(exitValue, inputInfo, errorInfo);
    }


    protected class Reader implements Callable<String> {
        private InputStream is;
        private StringBuilder sb;
        private boolean exit;
        private Object object;

        public Reader(InputStream is, Object object) {
            this.is = is;
            sb = new StringBuilder();
            exit = false;
            this.object = object;
        }

        public String getResult() {
            return sb.toString();
        }

        /**
         * 避免因为输入流阻塞导致线程死循环
         */
        public void stopRead() {
            this.exit = true;
        }

        @Override
        public String call() throws Exception {
            byte[] tmp = new byte[1024];
            while (!exit) {
                try {
                    while (is != null && is.available() > 0) {
                        int i = is.read(tmp, 0, 1024);
                        if (i < 0) break;
                        String res = new String(tmp, 0, i);
                        sb.append(res);
                    }
                } catch (IOException e) {
                    log.info(e);
                    break;
                }


                if ("java.lang.UNIXProcess".equals(object.getClass().getName())) {
                    Process process = (Process) this.object;
                    if (!process.isAlive()) {
                        if (is.available() > 0) {
                            continue;
                        }
                        break;
                    }
                } else if (object.getClass() == ChannelExec.class || object.getClass() == ChannelSftp.class) {
                    Channel channel = (Channel) object;
                    if (channel.isClosed()) {
                        if (is.available() > 0) {
                            continue;
                        }
                        break;
                    }
                } else {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    log.info(ee);
                }
            }

            return sb.toString();
        }
    }

}


