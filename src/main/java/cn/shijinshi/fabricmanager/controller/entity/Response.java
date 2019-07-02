package cn.shijinshi.fabricmanager.controller.entity;

public class Response {
    private static final String OK = "ok";
    private static final String ERROR = "error";

    private Meta meta;
    private Object data;

    public Response success() {
        this.meta = new Meta(true);
        return this;
    }

    public Response success(String message) {
        this.meta = new Meta(true, message, null);
        this.data = null;
        return this;
    }

    public Response success(Object data) {
        this.meta = new Meta(true);
        this.data = data;
        return this;
    }

    public Response failure() {
        this.meta = new Meta(false);
        return this;
    }

    public Response failure(String message) {
        this.meta = new Meta(false, message, null);
        return this;
    }

    public Response failure(String message, String cause) {
        this.meta = new Meta(false, message, cause);
        return this;
    }

    public Response failure(Object data) {
        this.meta = new Meta(false);
        this.data = data;
        return this;
    }

    public Meta getMeta() {
        return meta;
    }

    public Object getData() {
        return data;
    }

    public class Meta {
        private boolean success;
        private String message;
        private String cause;

        public Meta(boolean success) {
            this.success = success;
        }

        public Meta(boolean success, String message, String cause) {
            this.success = success;
            this.message = message;
            this.cause = cause;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getCause() {
            return cause;
        }
    }
}
