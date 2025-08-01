package main.java.controller;

public class ResponseApi<T> {
    private boolean success;
    private T data;
    private String message;

    public ResponseApi() {}

    public ResponseApi(boolean success, T data ){
        this.success = success;
        this.data = data;
    }

    public ResponseApi(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
