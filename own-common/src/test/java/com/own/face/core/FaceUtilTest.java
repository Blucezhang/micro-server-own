package com.own.face.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FaceUtilTest {

    @Test
    public void excludesOnlyNullAndEmptyStrings() {
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("empty", "");
        source.put("text", new String("value"));
        source.put("number", Integer.valueOf(0));

        Map<String, Object> result = FaceUtil.transBean2MapNotNull(new SampleBean(source));

        assertFalse(result.containsKey("empty"));
        assertEquals("value", result.get("text"));
        assertEquals(Integer.valueOf(0), result.get("number"));
    }

    private static class SampleBean {
        private final Map<String, Object> values;

        private SampleBean(Map<String, Object> values) {
            this.values = values;
        }

        public String getEmpty() {
            return (String) values.get("empty");
        }

        public String getText() {
            return (String) values.get("text");
        }

        public Integer getNumber() {
            return (Integer) values.get("number");
        }
    }
}
