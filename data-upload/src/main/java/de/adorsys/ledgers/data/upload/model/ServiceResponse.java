package de.adorsys.ledgers.data.upload.model;

public class ServiceResponse<T> {
    private boolean isSuccess;
    private T body;
    private String message;

    public ServiceResponse(T body) {
        this.isSuccess = true;
        this.body = body;
    }

    public ServiceResponse(boolean isSuccess, T body) {
        this.isSuccess = isSuccess;
        this.body = body;
    }

    public ServiceResponse() {
        this.isSuccess = true;
    }

    public ServiceResponse(String message) {
        this.isSuccess = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public ServiceResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public String getMessage() {
        return message;
    }
}
