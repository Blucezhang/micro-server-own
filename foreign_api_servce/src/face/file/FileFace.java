package face.file;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestTemplate;

public class FileFace {
    @Autowired        // Created automatically by Spring Cloud
    @LoadBalanced
    protected RestTemplate restTemplate; 
    
    @Value("FileServiceUrl")
    private  String serviceUrl;

    public List<String> getFileList(String type) {
        List<String> fileList = restTemplate.getForObject(serviceUrl
                + "/file/{type}", List.class, type);
 
            return fileList;
    }
    
}
 