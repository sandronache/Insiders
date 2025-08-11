package com.insiders.http;

public final class ApiResult<T> {
    public final boolean success;
    public final String message;
    public final T data;
    public final int status;

    private ApiResult(boolean s, String m, T d, int st){ success=s; message=m; data=d; status=st; }

    public static <T> ApiResult<T> ok(T d, String m, int s){ return new ApiResult<>(true, m, d, s); }

    public static <T> ApiResult<T> fail(String m, T d, int s){ return new ApiResult<>(false, m, d, s); }
}
