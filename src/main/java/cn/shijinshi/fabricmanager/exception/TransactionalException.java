package cn.shijinshi.fabricmanager.exception;

/**
 * 事务异常，用于事务回滚
 */
public class TransactionalException extends RuntimeException {
    public TransactionalException(String message) {
        super(message);
    }
}
