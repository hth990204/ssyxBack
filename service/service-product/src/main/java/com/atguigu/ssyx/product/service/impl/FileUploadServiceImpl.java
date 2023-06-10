package com.atguigu.ssyx.product.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.ssyx.product.service.FileUploadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {


    @Value("${aliyun.endpoint}")
    private String endpoint;

    @Value("${aliyun.keyid}")
    private String accessKeyId;

    @Value("${aliyun.keysecret}")
    private String accessKeySecret;

    @Value("${aliyun.bucketname}")
    private String bucketName;

    @Override
    public String fileUpload(MultipartFile file) {


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObjectRequest对象。

            String objectName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            objectName = uuid + objectName;

            String currentDateTime = new DateTime().toString("yyyy/MM/dd");

            objectName = currentDateTime + "/" + objectName;


            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);

            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println(result.getResponse().getStatusCode());
            System.out.println(result.getResponse().getErrorResponseAsString());
            System.out.println(result.getResponse().getUri());
            // 返回图片路径
            String url = result.getResponse().getUri();
            return url;
        } catch (Exception ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
