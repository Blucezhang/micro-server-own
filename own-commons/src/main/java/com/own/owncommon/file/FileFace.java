package com.own.owncommon.file;

import java.util.List;

import com.own.owncommon.core.FaceBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class FileFace extends FaceBase {

    @Value("FileServiceUrl")
    private  String serviceUrl;

    public List<String> getFileList(String type) {
        List<String> fileList = get(serviceUrl
                + "/file/{type}", List.class, type);
            return fileList;
    }
    
}
 