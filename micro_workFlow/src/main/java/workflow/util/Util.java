package workflow.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	public static final String toStringAndTrim(Object object) {

		if (object == null)
			return "";
		else
			return object.toString().trim();
	}

	public static String toJsonString(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		try {
			jsonStr = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}
	
	
	 public static Object createJson2Bean(String value, Class calsz)
	    {
	        if(value == null || "".equals(value))
	            return null;
	        ObjectMapper mapper = new ObjectMapper();
	        Object object = null;
	        try
	        {
	            object = mapper.readValue(value, calsz);
	        }
	        catch(IOException e)
	        {
	            e.printStackTrace();
	        }
	        return object;
	    }

}
