package com.campusflow.service;

public class ServiceResult {
    private final boolean success;
    private final String message;
    private final Object payload;

    public ServiceResult(boolean success, String message, Object payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public static ServiceResult ok(String message) {
        return new ServiceResult(true, message, null);
    }

    public static ServiceResult ok(String message, Object payload) {
        return new ServiceResult(true, message, payload);
    }

    public static ServiceResult fail(String message) {
        return new ServiceResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getPayload() {
        return payload;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayload(Class<T> clazz) {
        return (T) payload;
    }
}

