package com.mytest.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j //provided by lombok
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3client;

    public String uploadFile(MultipartFile multipartFile)
    {
        File file=convertMultiPartFileToFile(multipartFile);
        String fileName=System.currentTimeMillis()+"_"+multipartFile.getOriginalFilename();
        s3client.putObject(new PutObjectRequest(bucketName,fileName,file));
        file.delete(); //Good practice to delete the file once it is uploaded else it will keep accumulating in current dir.
        return "File uploaded :"+fileName;
    }

    public byte[] downloadFile(String fileName)
    {
        S3Object s3Object=s3client.getObject(bucketName,fileName);
        S3ObjectInputStream inputStream= s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFile(String fileName)
    {
        s3client.deleteObject(bucketName,fileName);
        return fileName+" removed...";
    }
    private File convertMultiPartFileToFile(MultipartFile file)
    {
        File convertedFile=new File( (file.getOriginalFilename()));
        try(FileOutputStream fos=new FileOutputStream((convertedFile)))
        {
            fos.write((file.getBytes()));
            return convertedFile;
        }
        catch (IOException e)
        {
            log.error("Error in converting multipart file to file",e);
        }
        return null;
    }
}
