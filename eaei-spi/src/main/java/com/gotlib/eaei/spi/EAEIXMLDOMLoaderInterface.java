package com.gotlib.eaei.spi;

import org.w3c.dom.Document;

public interface EAEIXMLDOMLoaderInterface {
	
	public static final String FLAG_INPUT_DIR 				= 	"-id";
	public static final String DEFAULT_INPUT_DIR 			=	"in";

	/*
	 * Return the DOM loader name
	 */
	String getDOMLoaderName();
	
	/*
	 * load the the data to update and format it to EAEI XML.
	 * Return Document - DOM object  
	 */
	Document getDocument(ParamatersManager paramsManager);

/*
 * Return String = a description of the valid command line parameters that expected by this implementation of ParamatersManager.
 * For example: "-f <file name>"
 */
	String getCLParamaters();
}

