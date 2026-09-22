package com.own.send.server.util.sms;

/**
 * Preserves a provider failure as structured application information.
 */
public class SmsGatewayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String providerResponse;
    private final String providerCode;

    public SmsGatewayException(String message, String providerResponse, String providerCode) {
        super(message);
        this.providerResponse = providerResponse;
        this.providerCode = providerCode;
    }

    public SmsGatewayException(String message, String providerResponse, String providerCode, Throwable cause) {
        super(message, cause);
        this.providerResponse = providerResponse;
        this.providerCode = providerCode;
    }

    public String getProviderResponse() {
        return providerResponse;
    }

    public String getProviderCode() {
        return providerCode;
    }
}
