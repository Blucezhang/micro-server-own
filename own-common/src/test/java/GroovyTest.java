import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.StreamingTemplateEngine;
import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;

public class GroovyTest {
	
	static public void  main(String[] args){
		
		String el = "2+3";
 		GroovyTest test = new GroovyTest();
		//test.testFormat(el);
		
		test.testParse("");
		System.out.println("====================");
	}
 	
		private GroovyShell shell	=	null;
	     	
 
 	 
		
		//交易执行之后做数据转换
		public void testFormat(String els){
			
			Writer writer = new CharArrayWriter();
			
			StreamingTemplateEngine engine = new StreamingTemplateEngine();     
			
			FileInputStream fin = null;
			String tempStr = null;
			
			try {
				fin = new FileInputStream("D:\\tempTest.txt");
				byte[] buffer = new byte[4098];
				fin.read(buffer);
				tempStr = new String(buffer);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Template template;
			
			try {
				template = engine.createTemplate(tempStr);
	    
				Map mapData = new HashMap();
 				mapData.put("1", "11111111111");
				mapData.put("2", "22222222222");
				mapData.put("3", "33333333333");
				mapData.put("4", "44444444444");
				
				
				
				Map<String, Object> model = new HashMap<>();  
				model.put("helloWorld", new HelloWorldTool());
				model.put("string1", "String1");
				model.put("string2", "String2");
				model.put("string3", "String3");
				model.put("mapData", mapData);
				
				model.put("format", new base.entry.transformer.FormatTool());
				model.put("parse", new base.entry.transformer.ParseTool());
				
				
				Writable output = template.make(model);                             
		 
				output.writeTo(writer);
				
				System.out.println("===============>"+writer.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
 		}
 
		public void testParse(String els){
			
			Writer writer = new CharArrayWriter();
			
			StreamingTemplateEngine engine = new StreamingTemplateEngine();     
			
			FileInputStream fin = null;
			String tempStr = null;
			
			try {
				fin = new FileInputStream("D:\\tempTest1.txt");
				byte[] buffer = new byte[4098];
				fin.read(buffer);
				tempStr = new String(buffer);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Template template;
			
			try {
				template = engine.createTemplate(tempStr);
	    
				Map mapData = new HashMap();
				
				String receData ="111111111111111111112222222222222222222233333333333333334444444444444444444444444555555555555555";
	 			InputStream inputStream = new ByteArrayInputStream(receData.getBytes());
				
	 		 
				Map<String, Object> model = new HashMap<>();  
				model.put("input", inputStream);
 				model.put("mapData", mapData);
				
				model.put("format", new base.entry.transformer.FormatTool());
				model.put("parse", new base.entry.transformer.ParseTool());
				
				
				Writable output = template.make(model);                             
		 
				output.writeTo(writer);
				
				System.out.println("===============>"+mapData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			
		}
}
