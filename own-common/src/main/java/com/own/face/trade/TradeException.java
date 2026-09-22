package com.own.face.trade;

public class TradeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final Object data;

    public TradeException(int status, String message) {
        this(status, message, null);
    }
    public TradeException(int status, String message, Object data) {
        super(message);
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }
    public Object getData() { return data; }

    public static TradeException badRequest(String message) {
        return new TradeException(400, message);
    }

    public static TradeException forbidden(String message) {
        return new TradeException(403, message);
    }

    public static TradeException notFound(String message) {
        return new TradeException(404, message);
    }

    public static TradeException conflict(String message) {
        return new TradeException(409, message);
    }
    public static TradeException conflict(String message, Object data) { return new TradeException(409, message, data); }

    public static TradeException unprocessable(String message) {
        return new TradeException(422, message);
    }
}
