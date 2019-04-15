package com.huaer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@org.springframework.context.annotation.Configuration
@PropertySource("classpath:application-dev.properties")
public class QiNiuUtils {

	@Value(value="${qiniu.accessKey}")
	private  String accessKey;
	
	@Value(value="${qiniu.secretKey}")
	private  String secretKey;
	
	//库名
	@Value(value="${qiniu.bucket}")
	private  String bucket;
	
	//文件前缀
	@Value(value="${qiniu.delimiter}")
	private  String delimiter; 
	
	//外链路径
	@Value(value="${qiniu.url}")
	private String url;
	
	/*
	 * 获取文件名
	 */
	public String getFileName(String url){
		String fname =url.trim();
		if(url.contains("?")){
			fname = url.substring(fname.lastIndexOf("/")+1,fname.indexOf("?"));
		}else{
			fname = url.substring(fname.lastIndexOf("/")+1);
		}
		return fname;
	}
	
	public String getUpToken(){
		String upToken = null;
		try{
			Auth auth = Auth.create(accessKey, secretKey);
			StringMap putPolicy = new StringMap();
			putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
			long expireSeconds = 3600;
			upToken = auth.uploadToken(bucket, null, expireSeconds, putPolicy);
		}catch(Exception e){
			e.printStackTrace();
		}
		return upToken;
	}
	
	public  String qiniuxUploadFile(byte[] fileByte,String fileName){
		//构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration(Zone.zone0());
		//...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		//...生成上传凭证，然后准备上传
		//如果是Windows情况下，格式是 D:\\qiniu\\test.png
		//默认不指定key的情况下，以文件内容的hash值作为文件名
		Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH);
		String fileType = fileName.substring(fileName.lastIndexOf("."),fileName.length()); 
		fileName = UUID.randomUUID().toString() + fileType;
		String key = year+"/"+month+"/" + delimiter+"/"+ fileName;

		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);

		try {
		    com.qiniu.http.Response response = uploadManager.put(fileByte, key, upToken);
		    //解析上传成功的结果
		    DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
		    System.out.println(putRet.key);
		    System.out.println(putRet.hash);
		    return url+putRet.key;
		    //FvjNXM7JjT0nRMhxIRiVekZGGRlJ
		   // FvjNXM7JjT0nRMhxIRiVekZGGRlJ
		} catch (QiniuException ex) {
		    return null;
		}


	}
	
	/**
   * 通过发送http get 请求获取文件资源
   */
	  public  byte[] getFileByte(String url) {
		  byte[] data= null;
		    OkHttpClient client = new OkHttpClient();
		    System.out.println(url);
		    Request req = new Request.Builder().url(url).build();
		    Response resp = null;
		    try {
		      resp = client.newCall(req).execute();
		      System.out.println(resp.isSuccessful());
		      if (resp.isSuccessful()) {
		        ResponseBody body = resp.body();
		        InputStream is = body.byteStream();
		        data = readInputStream(is);
		      }
		    } catch (IOException e) {
		      e.printStackTrace();
		      System.out.println("Unexpected code " + resp);
		    }
		    return data;
	  }

	public  void ouputFile(String filepath, String fileName, byte[] data)
			throws FileNotFoundException, IOException {
		//判断文件夹是否存在，不存在则创建
		File file = new File(filepath);
		if (!file.exists() && !file.isDirectory()) {
		  System.out.println("===文件夹不存在===创建====");
		  file.mkdir();
		}
		File imgFile = new File(filepath+File.separator+fileName);
		FileOutputStream fops = new FileOutputStream(imgFile);
		fops.write(data);
		fops.close();
	}

	  /**
	   * 读取字节输入流内容
	   */
	  public  byte[] readInputStream(InputStream is) {
	    ByteArrayOutputStream writer = new ByteArrayOutputStream();
	    byte[] buff = new byte[1024 * 2];
	    int len = 0;
	    try {
	      while ((len = is.read(buff)) != -1) {
	        writer.write(buff, 0, len);
	      }
	      is.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    return writer.toByteArray();
	  }
}
