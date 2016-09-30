package com.gotlib.eaei.spi;

public interface ParamatersManager {

	/*
	 * Return String - the value of the parameter with the key 'key'. If no such parameter was found. return defaultValue 
	 */
	public String getStringParam(String key, String defaultValue);
	
	/*
	 * Return boolean - return TRUE if the value of the 'key' is 'value' and FALSE otherwise  
	 */
	public boolean is (String key, String value);
	
	

}
