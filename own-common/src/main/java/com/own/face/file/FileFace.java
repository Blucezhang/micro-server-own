package com.own.face.file;

import java.util.List;

import com.own.face.core.FaceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestTemplate;

public class FileFace extends FaceBase {

    @Value("FileServiceUrl")
    private  String serviceUrl;

    protected org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());


    public List<String> getFileList(String type) {
        List<String> fileList = get(serviceUrl
                + "/file/{type}", List.class, type);
            return fileList;
    }
    
}
 