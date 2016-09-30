package com.gotlib.eaei.utils;



import com.gotlib.eaei.spi.ParamatersManager;

public class LaunchingConfiguration implements ParamatersManager{
	

	private String[] 		args = null;

	
	



	public void init(String[] args) throws Exception {
		this.args = args;

		

	}
	
	@Override
	public String getStringParam(String key, String defaultValue) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareToIgnoreCase(key) == 0) { 
				if (i+1 < args.length) 	return args[i+1];
				else 					return null;
			}
		}
		return defaultValue;
	}
	
	@Override
	public boolean is (String key, String value) {
		return (getStringParam(key, "").compareToIgnoreCase(value) == 0);
	}
	


	
}
