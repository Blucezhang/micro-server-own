import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;


import javafx.application.Application;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@WebAppConfiguration
//(value = "src/main/webapp")
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations ={"classpath*:IocConf/*.xml"})
@ContextConfiguration(classes = Application.class)
public class TestBase {
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	protected MockMvc mockMvc;

	@Before
	public void setup() throws Exception{
//		 MockitoAnnotations.initMocks(this);// 初始化mock对象
//		 mockMvc = MockMvcBuilders.standaloneSetup(partyAction).build();
		 mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();  
	}
	
	
	protected Date nowDate	=	new Date(System.currentTimeMillis());
	protected Timestamp now	=	new Timestamp(System.currentTimeMillis());
	
	BigDecimal getDecimal(float value){
		return new BigDecimal(value);
	}
	
	public void printResult(String result) {
		System.out.println("=====");
		System.out.println("===============================result print start================================");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		try {
			System.out.println(JsonFormatTool.beautify(result));
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("=====");
		System.out.println("===============================result print end================================");
		System.out.println("=====");
	}
	
	
	public void putTest(String path,String jsonParam) throws Throwable{
		try {
			printResult(mockMvc.perform(put(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonParam)
					)
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString()
					);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
	public void postTest(String path,String jsonParam) throws Throwable{
		try {
			printResult(mockMvc.perform(post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonParam)
					)
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString()
					);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
	public void deleteTest(String path) throws Throwable{
		try {
			printResult(mockMvc.perform(delete(path)
					.contentType(MediaType.APPLICATION_JSON)
					)
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString()
					);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
}
