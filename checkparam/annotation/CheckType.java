package com.huaer.annotation;

public enum CheckType {
	
	ISNULL("isNull",String.class);
	
	public String methodName;
	public Class<?> paramaClass;
	
	CheckType(String methodName,Class<?> paramaClass){
		this.paramaClass = paramaClass;
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?> getParamaClass() {
		return paramaClass;
	}

	public void setParamaClass(Class<?> paramaClass) {
		this.paramaClass = paramaClass;
	}
	
	
	
	
}
