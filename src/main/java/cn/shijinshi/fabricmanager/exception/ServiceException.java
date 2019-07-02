package cn.shijinshi.fabricmanager.exception;

public class ServiceException extends RuntimeException {
    private String causeMsg;
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.causeMsg = cause.getMessage();
    }

    public ServiceException(String message, String causeMsg) {
        super(message);
        this.causeMsg = causeMsg;
    }

    public String getExceptionMessage() {
        if (causeMsg == null || causeMsg.isEmpty()) {
            return null;
        } else {
            return causeMsg;
        }
    }
}
