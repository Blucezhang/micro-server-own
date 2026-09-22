package com.own.face.util;

import java.io.Serializable;

/**
 * Stable response envelope shared by all HTTP-facing modules.
 */
public class Resp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object data;
    private Integer status;
    private String message;

    public Resp() {
        this(null);
    }

    public Resp(Object data) {
        this(data, 200, "success");
    }

    public Resp(Object data, Integer status, String message) {
        this.data = data;
        this.status = status;
        this.message = message;
    }

    public static Resp error(Integer status, String message) {
        return new Resp(null, status, message);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
