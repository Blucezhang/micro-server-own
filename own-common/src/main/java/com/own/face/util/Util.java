package com.own.face.util;

import java.io.*;

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

	/**
	 * 将图片从临时目录移动到真实目录，并删除临时路径图片
	 * @param tmpInfoFilePath 临时图片存放路径
	 * @param realInfofilePath 真实图片存放路径
	 * @param fileNames 移动图片的名称数组 例如 [a.png,b.jpb,c.png]
	 */
	public static void copyFileToRealPath(String tmpInfoFilePath,String realInfofilePath,String[] fileNames){
		File file = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		for(String fileName : fileNames){
			try{
				file = new File(tmpInfoFilePath+"/"+fileName);
				if(!file.exists()){
					continue;
				}
				fis = new FileInputStream(file);
				bis=new BufferedInputStream(fis);
				fos = new FileOutputStream(realInfofilePath+"/"+fileName);//写入文件
				byte data[]=new byte[4096];
				int size=0;
				size=bis.read(data);
				while (size!=-1){
					fos.write(data,0,size);
					size=bis.read(data);
				}
				fos.flush();
			}catch(IOException e){
				e.printStackTrace();
				//throw new IFException("500","将图片从临时路径拷贝到真实路径出错...");
			}finally{
				try{
					if(fis != null)
						fis.close();
					if(bis != null)
						bis.close();
					if(fos != null)
						fos.close();
					if(file.exists())
						file.delete();//删除临时路径图片
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}


}
