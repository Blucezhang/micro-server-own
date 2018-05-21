import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

public class FlowTest extends TestBase  {


	@Test
	public void getFlowTrans() throws Exception {

		String result = mockMvc
				.perform(
						get("/Flow/1234").contentType(
								MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
		 
		this.printResult(result);

	}	
	
}
