package com.own.send.server.util.sms;

import java.io.Serializable;

/**
 * Result returned by the legacy SMS gateway adapter.
 */
public class MsgResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String errMsg;
    private String sysSuccMsg;
    private String state;

    public static MsgResult success(String gatewayMessageId) {
        MsgResult result = new MsgResult();
        result.setState("succ");
        result.setSysSuccMsg(gatewayMessageId);
        return result;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getSysSuccMsg() {
        return sysSuccMsg;
    }

    public void setSysSuccMsg(String sysSuccMsg) {
        this.sysSuccMsg = sysSuccMsg;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
