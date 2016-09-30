package com.gotlib.eaei.spi;

import org.w3c.dom.Document;

public interface SaveResultsFormatterInterface {
	
	public static final String FLAG_OUTPUT_DIR 				= 	"-od";
	public static final String DEFAULT_OUTPUT_DIR 			=	"out";

	/*
	 * Return the formatter name
	 */
	String getFormatterName();
	
	/*
	 * Save the DOM object outputDoc in the directory as specified by FLAG_OUTPUT_DIR 
	 */
	void saveEAExportFile(Document outputDoc, ParamatersManager paramsManager);

/*
 * Return String = a description of the valid command line parameters that expected by this implementation of ParamatersManager.
 * For example: "-f <file name>"
 */
	String getCLParamaters();
}

