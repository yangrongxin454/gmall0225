package com.atguigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyUploadUtil {
    public static String uploadImage(MultipartFile multipartFile) {

        String imgUrl = "Http://192.168.174.100";
        // 先连链接tracker
        String path = MyUploadUtil.class.getClassLoader().getResource("tracker.txt").getPath();

        try {
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();

        }

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 通过tracker获得storage
        StorageClient storageClient = new StorageClient(connection,null);

       // 通过storage上传

        try {
            byte[] bytes = multipartFile.getBytes();
            String originalFilename = multipartFile.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String substring = originalFilename.substring(i + 1);
            String[] jpgs = new String[0];
            jpgs = storageClient.upload_file(bytes, substring, null);
            for (String jpg : jpgs) {
                imgUrl += "/"+jpg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return imgUrl;
    }
}
