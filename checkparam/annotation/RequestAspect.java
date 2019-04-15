package com.huaer.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.huaer.entity.CodeMsg;
import com.huaer.entity.Result;
import com.huaer.utils.CheckUtils;

/**
 * 此类为一个切面类，主要作用就是对接口的请求进行拦截
 */
@Aspect
@Component
public class RequestAspect {
 
    //使用org.slf4j.Logger,这是spring实现日志的方法
	private static final Logger logger = LogManager.getLogger(RequestAspect.class);
 
    /**
     * @param joinPoint 连接点，就是被拦截点
     */
    @Before(value = "@annotation(com.huaer.annotation.CheckParam)")
    public void doBefore(JoinPoint joinPoint) {
        //获取到请求的属性
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获取到请求对象
        HttpServletRequest request = attributes.getRequest();
        Signature signature = joinPoint.getSignature();
        //URL：根据请求对象拿到访问的地址
        logger.info("url=" + request.getRequestURL());
        //获取请求的方法，是Get还是Post请求
        logger.info("method=" + request.getMethod());
        //ip：获取到访问
        logger.info("ip=" + request.getRemoteAddr());
        //获取被拦截的类名和方法名
		logger.info("class=" + signature.getDeclaringTypeName() +
                "and method name=" + signature.getName());
       
 
    }
    
    @Around(value = "@annotation(com.huaer.annotation.CheckParam)")
    public Object test(ProceedingJoinPoint pjp) throws Throwable{
    	boolean checkFlag = true;
    	String msg = null;
    	Signature signature = pjp.getSignature();
    	 //获取
		MethodSignature ms = (MethodSignature) signature;
		Method method = ms.getMethod();
		CheckParam annotation = method.getAnnotation(CheckParam.class);
		String[] fields = annotation.fields();
		CheckType[] types = annotation.types();
		Object[] args = pjp.getArgs();
		for(int m = 0; m < fields.length; m++){
			String field = fields[m];
			CheckType ct = types[m];
			//参数
			String[] paramNames = ms.getParameterNames();
			Class<CheckUtils> clazz = CheckUtils.class;
			for(int i = 0; i < paramNames.length; i++){
				String paramName = paramNames[i];
				Object obj = args[i];
				Object paramObj = new Object();
				if(obj instanceof String && paramName.equals(field)){
					paramObj = obj;
				}
				if(obj instanceof Map){
					@SuppressWarnings("unchecked")
					Map<String,Object> map = (Map<String, Object>) obj;
					paramObj = map.get(fields[m]);
				}
				else{
					Class<? extends Object> class1 = obj.getClass();
					Field field2 = null;
					try{
						field2 = class1.getDeclaredField(field);
					}catch(Exception e){
					}
					if(field2 == null){
						continue;
					}else{
						field2.setAccessible(true);
						Object object = field2.get(obj);
						paramObj = object;
					}
				}
				Method utilMethod = clazz.getMethod(ct.getMethodName(), ct.getParamaClass());
				boolean invoke = (boolean) utilMethod.invoke(clazz, paramObj);
				if(invoke){
					checkFlag = false;
					msg = field+"为空或不符合规范";
				}
			}
		}
        if(!checkFlag){
        	return Result.error(CodeMsg.PARAMETER_ERROR,msg);
        }else{
        	Object retVal = pjp.proceed(args);
        	return retVal;
        }
    }
}
