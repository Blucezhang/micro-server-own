package com.own.send.server.util;

import com.own.send.server.util.sms.MsgResult;
import com.own.send.server.util.sms.SmsGatewayException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SendSmsTest {

    @Test
    public void returnsStructuredSuccessForAcceptedGatewayResponse() {
        SendSms sender = new StubSendSms("12345");

        MsgResult result = sender.sendSms("13800138000", "hello", "http://localhost", "sn", "pwd");

        assertEquals("succ", result.getState());
        assertEquals("12345", result.getSysSuccMsg());
    }

    @Test(expected = SmsGatewayException.class)
    public void rejectsBlankGatewayResponse() {
        new StubSendSms(" ").sendSms("13800138000", "hello", "http://localhost", "sn", "pwd");
    }

    @Test(expected = SmsGatewayException.class)
    public void rejectsNonNumericGatewayResponse() {
        new StubSendSms("not-a-number").sendSms("13800138000", "hello", "http://localhost", "sn", "pwd");
    }

    @Test
    public void createsStableUppercaseMd5() {
        assertEquals("E10ADC3949BA59ABBE56E057F20F883E", new SendSms().getMD5("123456"));
    }

    private static class StubSendSms extends SendSms {
        private final String gatewayResponse;

        private StubSendSms(String gatewayResponse) {
            this.gatewayResponse = gatewayResponse;
        }

        @Override
        public String mdsmssend(String mobile, String content, String ext, String stime,
                                String rrid, String msgfmt, String serviceUrl, String sn, String pwd) {
            return gatewayResponse;
        }
    }
}
