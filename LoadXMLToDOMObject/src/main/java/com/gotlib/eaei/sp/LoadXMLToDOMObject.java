package com.gotlib.eaei.sp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.gotlib.eaei.spi.EAEIXMLDOMLoaderInterface;
import com.gotlib.eaei.spi.ParamatersManager;

public class LoadXMLToDOMObject implements EAEIXMLDOMLoaderInterface {

	public static final String FLAG_XML_FILE_NAME 			= 	"-xfn";
	public static final String DEFAULT_XML_INPUT_FILE_NAME =	"eaeiImportXMLFile.xml";


	@Override
	public	String getCLParamaters() {
		return FLAG_XML_FILE_NAME + " <output XML file name> (Defalt: " + DEFAULT_XML_INPUT_FILE_NAME + ")";
	}

	/**
	 * Load the input file into org.w3c.dom.Document implementation 
	 * @param ParamatersManager
	 * @return Document interface
	 */

	@Override
	public Document getDocument(ParamatersManager pm) {

			String importFileName = pm.getStringParam(EAEIXMLDOMLoaderInterface.FLAG_INPUT_DIR, EAEIXMLDOMLoaderInterface.DEFAULT_INPUT_DIR) + "\\" + pm.getStringParam(FLAG_XML_FILE_NAME, DEFAULT_XML_INPUT_FILE_NAME);

			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoadXMLToDOMObject.class.getName());
			logger.log(Level.FINEST, "In loadInputFile");


			File fXmlFile = new File(importFileName);
			if (fXmlFile == null) {
				return null;
			}
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;




			try {
				dBuilder = dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				logger.log(Level.SEVERE, e.toString());
				return null;
			}
			Document doc = null;
			try {
				doc = dBuilder.parse(fXmlFile);
			} catch (Exception  e) {
				logger.log(Level.SEVERE, e.toString());
				return null;
			}

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			logger.log(Level.FINEST, "Out loadInputFile"); 

			return doc;
		}

	

	@Override
	public String getDOMLoaderName() {
		return "Construct DOM from XML";
	}
	public static void main(String[] args) {
		System.out.println("TO DO: get as input ParamatersManager and launch getDocument");


	}

	
}
