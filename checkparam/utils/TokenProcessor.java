package com.huaer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.pagehelper.util.StringUtil;

public class TokenProcessor {

	private static TokenProcessor instance = new TokenProcessor();  
	  
    private static long previous;  
  
    protected TokenProcessor() {  
    }  
  
    public static TokenProcessor getInstance() { 
        return instance;  
    }  
    
    public static String getUUID(){
    	return UUID.randomUUID().toString().replace("-", "");
    }
  
    public static String getSignature(String supplierId,Map<String,String> source,String supplierSecret,long timestamp) throws Exception{
    	StringBuffer sb = new StringBuffer();
    	sb.append(supplierId);
    	List<String> list = new ArrayList<>();
    	if(source != null){
    		for(String key : source.keySet()){
    			String value = source.get(key);
    			if(StringUtil.isNotEmpty(value)){
    				list.add(value.toUpperCase());
    			}
    		}
    	}
    	Collections.sort(list);
    	 for(String value : list) {
             sb.append(value);
         }
    	 sb.append(timestamp);
    	 sb.append(supplierSecret);
    	 String sha1 = MD5Util.getSha1(sb.toString()).toUpperCase();
    	 return sha1;
    }
    
    
    public synchronized static String generateToken(String msg, boolean timeChange) {  
        try {  
  
            long current = System.currentTimeMillis();  
            if (current == previous){  
            	current++;   
            }
            previous = current;   
            MessageDigest md = MessageDigest.getInstance("MD5");  
            md.update(msg.getBytes());  
            if (timeChange) {  
                byte now[] = (new Long(current)).toString().getBytes();  
                md.update(now);  
            }  
            return toHex(md.digest());  
        } catch (NoSuchAlgorithmException e) {  
            return null;  
        }  
    }  
  
    private static String toHex(byte buffer[]) {  
        StringBuffer sb = new StringBuffer(buffer.length * 2);  
        for (int i = 0; i < buffer.length; i++) {  
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));  
            sb.append(Character.forDigit(buffer[i] & 15, 16));  
        }  
        return sb.toString();  
    }  
    

}
