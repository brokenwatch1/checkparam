package com.huaer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class HttpClientUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class); // 日志记录

    private static RequestConfig requestConfig = null;

    static{
        // 设置请求和传输超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
    }
    
    // 通过get请求得到读取器响应数据的数据流
    public static InputStream getInputStreamByGet(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url)
                    .openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                return inputStream;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

        // 将服务器响应的数据流存到本地文件
    public static void saveData(InputStream is, File file) {
        try (BufferedInputStream bis = new BufferedInputStream(is);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        		) {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    //获取参数字符串
    public static String getParamString(Map<String,String> map){
    	StringBuffer sb = new StringBuffer();
    	for (String name : map.keySet()) {  
            try {
            	String value = map.get(name); 
            	if(value == null || "null".equals(value)){
            		value ="";
            	}
				sb.append(name).append("=").append(  
				        java.net.URLEncoder.encode(value,  
				                "UTF-8")).append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}  
        }  
        String temp_params = sb.toString();  
        String params = temp_params.substring(0, temp_params.length() - 1); 
        return params;
    }
    
    /**
     * post请求传输json参数
     */
    public static JSONObject httpPost(String url, Object obj){
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        // 设置请求和传输超时时间
        httpPost.setConfig(requestConfig);
        try{
        	String jsonStr=JSONObject.toJSONString(obj);
            if (null!=jsonStr){
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonStr, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                String str = "";
                try{
                    // 读取服务器返回过来的json字符串数据
                    str = EntityUtils.toString(result.getEntity(), "utf-8");
                    // 把json字符串转换成json对象
                    jsonResult = JSONObject.parseObject(str);
                }
                catch (Exception e){
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        }catch (IOException e){
            logger.error("post请求提交失败:" + url, e);
            e.printStackTrace();
        }finally {
            httpPost.releaseConnection();
        }
        return jsonResult;
    }

    /**
     * post请求传输String参数 
     */
    public static JSONObject httpPost(String url, String strParam) {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        try {
            if (null != strParam){
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(strParam, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String str = "";
                try{
                    // 读取服务器返回过来的json字符串数据
                    str = EntityUtils.toString(result.getEntity(), "utf-8");
                    // 把json字符串转换成json对象
                    jsonResult = JSON.parseObject(str);
                }catch (Exception e){
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        }catch (IOException e){
            logger.error("post请求提交失败:" + url, e);
            e.printStackTrace();
        }finally{
            httpPost.releaseConnection();
        }
        return jsonResult;
    }

    //form表单提交的方式
    public static JSONObject doPost(String url, String params,Map<String,String> header) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);// 创建httpPost
        httpPost.setHeader("Accept", "application/json");
//      httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        if(header != null){
        	for(String key : header.keySet()){
        		httpPost.setHeader(key,header.get(key));
        	}
        }
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(params, charSet);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
                response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                String jsonString = EntityUtils.toString(responseEntity);
                return JSONObject.parseObject(jsonString);
            }
            else{
                logger.error("请求返回:"+state+"("+url+")");
            }
        }
        finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 发送get请求
     */
    public static JSONObject httpGet(String url,Map<String,String> header){
        // get请求返回结果
        JSONObject jsonResult = null;
        CloseableHttpClient client = HttpClients.createDefault();
        // 发送get请求
        HttpGet request = new HttpGet(url);
        if(header != null){
        	for(String key : header.keySet())
        	request.setHeader(key, header.get(key));
        }
        request.setConfig(requestConfig);
        try{
            CloseableHttpResponse response = client.execute(request);

            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                // 读取服务器返回过来的json字符串数据
                HttpEntity entity = response.getEntity();
                String strResult = EntityUtils.toString(entity, "utf-8");
                // 把json字符串转换成json对象
                jsonResult = JSON.parseObject(strResult);
            }else{
                logger.error("get请求提交失败:" + url);
            }
        }
        catch (IOException e){
            logger.error("get请求提交失败:" + url, e);
            e.printStackTrace();
        }finally{
            request.releaseConnection();
        }
        return jsonResult;
    }

    public static String postFileMultiPart(String url,Map<String,ContentBody> reqParam,Map<String,String> header) throws ClientProtocolException, IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        try {
            // 创建httpget.
            HttpPost httppost = new HttpPost(url);
        	
            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if(header != null){
            	for(String key : header.keySet()){
            		httpPost.setHeader(key,header.get(key));
            	}
            }
            System.out.println("executing request " + httppost.getURI());
            
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for(Entry<String,ContentBody> param : reqParam.entrySet()){
            	multipartEntityBuilder.addPart(param.getKey(), param.getValue());
            }
            HttpEntity reqEntity = multipartEntityBuilder.build();
            httppost.setEntity(reqEntity);
            
            // 执行post请求.    
            CloseableHttpResponse response = httpclient.execute(httppost);
            
            System.out.println("got response");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            	try {  
                    // 获取响应实体    
                    HttpEntity entity = response.getEntity();  
                    //System.out.println("--------------------------------------");  
                    // 打印响应状态    
                    //System.out.println(response.getStatusLine());  
                    if (entity != null) { 
                    	return EntityUtils.toString(entity,Charset.forName("UTF-8"));
                    }
                    //System.out.println("------------------------------------");  
                } finally {  
                    response.close();
                }
            }else{
                logger.error("formdata请求提交失败:" + url);
            }
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }
        return null;  
    }
}
