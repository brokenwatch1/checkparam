package com.huaer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.github.pagehelper.util.StringUtil;

/**
 * MD5加密
 */
public class MD5Util {
 
	 public static final String KEY_MD5 = "MD5";

	    /**
	     * 默认编码格式
	     */
	    public static final String DEFAULTCHARSET = "UTF-8";

	    /**
	     * 用来将字节转换成 16 进制表示的字符
	     */
	    private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	  //下面四个import放在类名前面 包名后面
	  //import java.io.UnsupportedEncodingException;
	  //import java.security.MessageDigest;
	  //import java.security.NoSuchAlgorithmException;
	  //import java.util.Arrays;
	   
	  public static String getSha1(String str) throws Exception{
	      if (null == str || 0 == str.length()){
	          return null;
	      }
	          MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
	          mdTemp.update(str.getBytes("UTF-8"));
	           
	          byte[] md = mdTemp.digest();
	          int j = md.length;
	          char[] buf = new char[j * 2];
	          int k = 0;
	          for (int i = 0; i < j; i++) {
	              byte byte0 = md[i];
	              buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
	              buf[k++] = hexDigits[byte0 & 0xf];
	          }
	          return new String(buf);
	  }
	    
	    /**
	     * MD5加密
	     *
	     * @param source
	     * @param charSet 字符集
	     * @return
	     * @throws Exception
	     */
	    public static String encryptMD5(String source, String charSet) throws Exception {
	        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
	        if (StringUtil.isNotEmpty(charSet)) {
	            md5.update(source.getBytes(charSet));
	        }else {
	            md5.update(source.getBytes(DEFAULTCHARSET));
	        }
	        byte[] encryptStr = md5.digest();
	        char str[] = new char[16 * 2];
	        int k = 0;
	        for (int i = 0; i < 16; i++) {
	            byte byte0 = encryptStr[i];
	            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	            str[k++] = hexDigits[byte0 & 0xf];
	        }
	        return new String(str);
	    }

	    /**
	     * 默认字符集
	     * @param source
	     * @return
	     * @throws Exception
	     */
	    public static String encryptMD5(String source) throws Exception {
	        return encryptMD5(source,DEFAULTCHARSET);
	    }


	    public static void main(String[] args) throws Exception {
	        String inputStr = "简单加密";
	        System.err.println("原文:" + inputStr);
	        String result = encryptMD5(inputStr,null);
	        System.err.println("MD5:" + result);
	    }
	
	public static String crypt(String str) {
		if (str == null || str.length() == 0) {
			throw new IllegalArgumentException("String to encript cannot be null or zero length");
		}
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] hash = md.digest();
			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString.toString();
	}
	
	
 
}
