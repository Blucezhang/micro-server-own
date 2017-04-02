package party.util;

public class Util {

	public Util() {
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param object
	 * @return
	 */
	public static boolean isNullOrEmpty(Object object) {
		if (object == null || "".equals(object.toString()))
			return true;
		return false;
	}

	/**
	 * 转字符串，去空格
	 * @param object
	 * @return
	 */
	public static String toStringAndTrim(Object object) {
		if (object == null)
			return "";
		else
			return object.toString().trim();
	}

}
