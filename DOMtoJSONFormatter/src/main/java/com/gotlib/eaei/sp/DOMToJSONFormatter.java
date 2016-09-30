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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gotlib.eaei.spi.ParamatersManager;
import com.gotlib.eaei.spi.SaveResultsFormatterInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DOMToJSONFormatter implements SaveResultsFormatterInterface {

	public static final String FLAG_JSON_FILE_NAME 			= 	"-jfn";
	public static final String DEFAULT_JSON_OUTPUT_FILE_NAME =	"eaeiExportJSONFile";


	@Override
	public	String getCLParamaters() {
		return FLAG_JSON_FILE_NAME + " <output JASON file name> (Defalt: " + DEFAULT_JSON_OUTPUT_FILE_NAME + ")";
	}

	@Override
	public String getFormatterName() {
		return "DOM to JSON formatter";
	}

	@Override
	public void saveEAExportFile(Document outputDoc, ParamatersManager pm) {
		
		

		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DOMToJSONFormatter.class.getName());
		logger.log(Level.FINEST, getFormatterName() + "In saveEAExportFile for DOMToJSON"); 

		String outputFileName = pm.getStringParam(SaveResultsFormatterInterface.FLAG_OUTPUT_DIR, "") + "\\" + pm.getStringParam(FLAG_JSON_FILE_NAME, "eaeiOutput"+Math.round(Math.random()*100000000));
		logger.log(Level.FINEST, "Save the extracted information to: " + outputFileName); 
		

		JSONObject jsonFormat = DOM2JSON(outputDoc.getChildNodes().item(0));
		try {

			FileWriter file = new FileWriter(outputFileName);
			file.write(jsonFormat.toJSONString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}




		logger.log(Level.FINEST, getFormatterName() + "Out saveEAExportFile"); 

		
	}
	
	private static JSONObject DOM2JSON (Node node) {

		JSONObject dataObject 	= new JSONObject();
		JSONObject innerObject 	= new JSONObject();
		Map<String, JSONArray>  dataElements 	= new HashMap();


		if (node.getNodeType() == Node.ELEMENT_NODE) {
			addAttributes(node.getAttributes(), innerObject);
		}

		NodeList childNodes = node.getChildNodes();
		boolean noChildeNode = true;
		if (childNodes.getLength() > 0) {
			for (int count = 0; count < childNodes.getLength(); count++) {
				Node tempNode = childNodes.item(count);
				if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
					noChildeNode = false;
					if (!dataElements.containsKey(tempNode.getNodeName())) {
						dataElements.put(tempNode.getNodeName(), new JSONArray());
					}
					dataElements.get(tempNode.getNodeName()).add(DOM2JSON(tempNode));
				}
			}
			for (Map.Entry<String, JSONArray> e : dataElements.entrySet()) {
				if (e.getValue().isEmpty()) {
					innerObject.put(e.getKey(), "");
				} else if (e.getValue().size() == 1) {
					innerObject.put(e.getKey(), ((JSONObject)e.getValue().get(0)));
				} else {
					ValuesCollector valuesCollector = new ValuesCollector();

					e.getValue().forEach(valuesCollector);
					dataObject.put(e.getKey(), valuesCollector.getArray());
				}

			}
		} 
		if (innerObject.isEmpty() && noChildeNode) {
			if (!node.getTextContent().isEmpty()) {
				dataObject.put(node.getNodeName(), node.getTextContent());
			} else {
				dataObject.put(node.getNodeName(), "");
				//			}
			}
		} else if (!innerObject.isEmpty()){
			dataObject.put(node.getNodeName(), innerObject);
		}

		return dataObject;

	}
	
	
	private static void addAttributes(NamedNodeMap attributes, JSONObject dataObject) {


		for (int i = 0; i < attributes.getLength(); i++) {
			dataObject.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
		}

	}

	
	
	private static class ValuesCollector implements Consumer {
		JSONArray array = new JSONArray();
		@Override
		public void accept(Object t) {
			JSONObject obj = (JSONObject)t;
			array.add ((JSONObject)obj.values().iterator().next());

		}
		public JSONArray getArray() {
			return array;
		}
	}



	
	public static void main(String[] args) {
		System.out.println("TO DO: get as input xml file name and launch saveEAExportFile");


	}

	
}
