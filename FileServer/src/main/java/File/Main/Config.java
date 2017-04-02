package File.Main;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="my",locations="classpath:Config/application.properties")
public class Config {
	
	private String tmpInfoFilePath;
	private String realInfofilePath;
	
	
	public String getTmpInfoFilePath() {
		System.out.println("------------------------路过");
		return tmpInfoFilePath;
	}
	public void setTmpInfoFilePath(String tmpInfoFilePath) {
		this.tmpInfoFilePath = tmpInfoFilePath;
	}
	public String getRealInfofilePath() {
		return realInfofilePath;
	}
	public void setRealInfofilePath(String realInfofilePath) {
		this.realInfofilePath = realInfofilePath;
	}
	
	

}
