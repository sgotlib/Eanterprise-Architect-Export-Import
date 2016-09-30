package com.gotlib.eaei.sp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gotlib.eaei.spi.ParamatersManager;
import com.gotlib.eaei.spi.SaveResultsFormatterInterface;

public class DOMToXMLFormatter implements SaveResultsFormatterInterface {

	public static final String FLAG_XML_FILE_NAME 			= 	"-xfn";
	public static final String DEFAULT_XML_OUTPUT_FILE_NAME =	"eaeiExportXMLFile.xml";


	@Override
	public	String getCLParamaters() {
		return FLAG_XML_FILE_NAME + " <output XML file name> (Defalt: " + DEFAULT_XML_OUTPUT_FILE_NAME + ")";
	}

	@Override
	public String getFormatterName() {
		return "DOM to XML formatter";
	}

	@Override
	public void saveEAExportFile(Document outputDoc, ParamatersManager pm) {
		
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DOMToXMLFormatter.class.getName());
		logger.log(Level.FINEST, getFormatterName() + "In saveEAExportFile"); 

		
		String outputFileName = pm.getStringParam(SaveResultsFormatterInterface.FLAG_OUTPUT_DIR, "") + "\\" + pm.getStringParam(FLAG_XML_FILE_NAME, "eaeiOutput"+Math.round(Math.random()*100000000));
		logger.log(Level.FINEST, "Save the extracted information to: " + outputFileName); 
		
		
		// Use a Transformer for output

		TransformerFactory tFactory =
				TransformerFactory.newInstance();
		Transformer transformer = null;


		try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource source = new DOMSource(outputDoc);
		StreamResult result = new StreamResult(new File(outputFileName));
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		logger.log(Level.FINEST, getFormatterName() + "Out saveEAExportFile"); 


		
	}
	
	
	


	
	public static void main(String[] args) {
		System.out.println("TO DO: get as input xml file name and launch saveEAExportFile");


	}

	
}
