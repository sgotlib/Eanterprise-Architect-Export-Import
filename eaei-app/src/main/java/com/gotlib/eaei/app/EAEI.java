package com.gotlib.eaei.app;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sparx.Attribute;
import org.sparx.AttributeTag;
import org.sparx.Collection;
import org.sparx.Connector;
import org.sparx.ConnectorConstraint;
import org.sparx.ConnectorTag;
import org.sparx.Constraint;
import org.sparx.CustomProperty;
import org.sparx.Diagram;
import org.sparx.DiagramLink;
import org.sparx.DiagramObject;
import org.sparx.Effort;
import org.sparx.Issue;
import org.sparx.Method;
import org.sparx.MethodConstraint;
import org.sparx.MethodTag;
import org.sparx.Metric;
import org.sparx.Package;
import org.sparx.Parameter;
import org.sparx.Partition;
import org.sparx.Repository;
import org.sparx.TaggedValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gotlib.eaei.exceptions.CannotOpenRepositoryException;
import com.gotlib.eaei.exceptions.NoRepositoryWasDefined;
import com.gotlib.eaei.spi.EAEIXMLDOMLoaderInterface;
import com.gotlib.eaei.spi.SaveResultsFormatterInterface;
import com.gotlib.eaei.utils.EAEIEnvironmentData;
import com.gotlib.eaei.utils.LaunchingConfiguration;
import com.gotlib.eaei.utils.NumberedConnector;
import com.gotlib.eaei.utils.NumberedConnectorGenerator;

public class EAEI {


	private static final String VERSION 				= "version";
	private static final String CEAML_VERSION 			= "0.1";
	private static final String REPOSITORY 				= "repository";
	private static final String NAME 					= "name";
	private static final String REPOSITORY_ELEMENT_ID 	= "EA_REPOSITORY_ELEMENT";
	private static final String PACKAGE 				= "package";
	private static final String GUID 					= "GUID";
	private static final String TYPE 					= "type";
	private static final String OUTPUT_TYPE 			= "output";
	private static final String NOTES 					= "notes";
	private static final String PACKAGE_ELEMENT 		= "package_element";
	private static final String ELEMENTS 				= "elements";
	private static final String DIAGRAMS 				= "diagrams";
	private static final String DIAGRAM 				= "diagram";
	private static final String DIAGRAM_OBJECT 			= "diagram_object";
	private static final String DIAGRAM_OBJECTS 			= "diagram_objects";
	private static final String ELEMENT 				= "element";
	private static final String CUSTOM_PROPERTIES 		= "custom_pEaDoc2XMLroperties";
	private static final String CUSTOM_PROPERTY 		= "custom_property";
	private static final String TAGGED_VALUE 			= "tagged_value";
	private static final String TAGGED_VALUES 			= "tagged_values";
	private static final String META_DATA 				= "meta_data";
	private static final String MODEL 					= "model";
	private static final String REFERENCES				= "references";
	private static final String DIAGRAM_LINK 			= "diagram_link";
	private static final String DIAGRAM_LINKS 			= "diagram_links";

	private static final String ATTRIBUTE 			= "attribute";
	private static final String ATTRIBUTEEX			= "attributeex";
	private static final String ATTRIBUTES 			= "attributes";
	private static final String LINK_DOCUMENTS 		= "linked documents";
	private static final String LINKED_DOC_POSTFIX 	= " linked document.rtf";
	private static final int DEFAULT_BUFFER_SIZE 	= 2000;
	private static final String REALIZATE			= "realize";
	private static final String REALIZATES 			= "realizes";
	private static final String REALIZ_ELEMENT  	= "element.realize";
	private static final String PARTITION 			= "partition";
	private static final String METRIC 				= "metric";
	private static final String METRICS 			= "metrics";
	private static final String METHODS 			= "methods";
	private static final String METHOD 				= "method";
	private static final String METHODEXS 			= "methodexs";
	private static final String METHODEX 			= "methodex";
	private static final String PARTITIONS 			= "partition";
	private static final String ISSUE 				= "issue";
	private static final String ISSUES 				= "issues";
	private static final String FILES 				= "files";
	private static final String FILE 				= "file";
	private static final String EFFORT 				= "effort";
	private static final String EFFORTS 			= "efforts";
	private static final String DIAGRAMS_DIR 		= "diagrams";
	private static final String FILTER 				= "metadata.filter";
	



	public static void main(String[] args) {
		
		


		printOpenMessage();

		LaunchingConfiguration cnfg				= null;
		cnfg = new LaunchingConfiguration ();
		try {
			cnfg.init(args);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		EAEIEnvironmentData env = new EAEIEnvironmentData();
		try {
			env.init(cnfg);
		} catch (CannotOpenRepositoryException e1) {
			env.logger.log(Level.SEVERE, e1.toString()); 
			env.logger.log(Level.SEVERE, "Exit EAEI with no export/import processing"); 
			printUsageHelpMessage(env);
			return;
		}

		if (env.isUnknown() || env.getRepository() == null) {
			printUsageHelpMessage(env);
		} else if (env.isExport()) {
			env.loadExportProperties();
			Document outputDoc = null;
			try {
				outputDoc = exportDocInfo (env);
			} catch (Exception e) {
				printUsageHelpMessage(env);
				return;
			}
				
			saveResults (outputDoc, env);

			if (env.shouldSaveFullConfig())	{
				saveConfigExportWriter(env);
			}
			

			env.logger.log(Level.INFO, "Finish export processing");

		} else 	if (env.isImport()) {
			env.loadImportProperties();

			try {
				importDocInfo (env);
			} catch (Exception e) {
				env.logger.log(Level.SEVERE, e.toString());
				env.logger.log(Level.SEVERE, "Cannot process the import request!");
				return;
			}


			env.logger.log(Level.INFO, "Finish import processing"); 
		} else {			// If the structure of the call is ok or help is required...
			printUsageHelpMessage(env);
		}
		env.destroy();
	}




	private static void saveResults(Document outputDoc, EAEIEnvironmentData env) {

		Iterator<SaveResultsFormatterInterface> serviceProviders =  env.getSaveResultsServiceProviders();
		
		if (!serviceProviders.hasNext()) {
			env.logger.log(Level.SEVERE, "No service providers - cannot save the results!"); 
		} else {
			SaveResultsFormatterInterface saveResultsFormatter = null;
			String outputFileName = null;
			while (serviceProviders.hasNext()) {
				saveResultsFormatter = (SaveResultsFormatterInterface)serviceProviders.next();
				env.logger.log(Level.INFO, "Send output to: " + saveResultsFormatter.getFormatterName()); 
				saveResultsFormatter.saveEAExportFile(outputDoc, env.getParamatersManager());
			}
		}
	}




	private static void saveConfigExportWriter(EAEIEnvironmentData env) {
		BufferedWriter configfigExportWriter = null;
		
		try {
			File gConfigfigExportFile = new File(env.getConfigurationOutputFileName());
			configfigExportWriter = new BufferedWriter(new FileWriter(gConfigfigExportFile));
			Iterator itr = env.getExportFullConfiguration().iterator();
			while(itr.hasNext()) {
				String line = (String) itr.next();
				configfigExportWriter.write(line.toLowerCase());
			}

			configfigExportWriter.close();

		} catch ( IOException e ) {
			e.printStackTrace();
			configfigExportWriter = null;
		}

	}





	private static void importDocInfo(EAEIEnvironmentData env) {
		
		Element elementElement = null;
	
		EAEIXMLDOMLoaderInterface serviceProviderDOMLoader =  env.getImportDocumentSP();
		
		if (serviceProviderDOMLoader == null) {
			env.logger.log(Level.SEVERE, "Cannot proccess the import request");
			return;
		}

		
		Document documentToImport = serviceProviderDOMLoader.getDocument(env.getParamatersManager());
		
		if (documentToImport == null) {
			env.logger.log(Level.SEVERE, "Cannot proccess the import request");
			return;
		}

		Enumeration <Object> keys = env.getPropertiesKeys();
		String key;

		while  (keys.hasMoreElements()) {
			key = (String)keys.nextElement();
			env.logger.log(Level.FINEST, "Object to import (key): " + key);
			env.logger.log(Level.FINEST, key + " = " + env.getProperty(key, "NOT FOUND"));

			String[] objectsToImport = env.getProperty(key, "").split(",", 0);

			for (int i = 0; i < objectsToImport.length; i++) {


				NodeList elements = documentToImport.getElementsByTagName(objectsToImport[i].trim());
				env.logger.log(Level.INFO, "Object elements to import: " + objectsToImport[i] + ", number of elements: " + elements.getLength());

				for (int j = 0; j < elements.getLength(); j++) {
					elementElement = (Element)elements.item(j);
					if (key.equals("element")) {


						updateElement(elementElement, env);

						env.logger.log(Level.INFO, "Update Element: " + elementElement.getAttributeNode("name").getNodeValue());

					}
					else if (key.equals("diagram")) {


						updateDiagram(elementElement, env);

						if (elementElement.getAttributeNode("name") != null) { 
							env.logger.log(Level.INFO, "Update Diagram: " + elementElement.getAttributeNode("name").getNodeValue());
						}
					}

					else if (key.equals("connector")) {


						updateConnector(elementElement, env);

						if (elementElement.getAttributeNode("name") != null) { 
							env.logger.log(Level.INFO, "Update Connector: " + elementElement.getAttributeNode("name").getNodeValue());
						}
					}

					else if (key.equals("tagged_value")) {


						updateTaggedValue(elementElement, env);

						if (elementElement.getAttributeNode("name") != null) { 
							env.logger.log(Level.INFO, "Update TaggedValue: " + elementElement.getAttributeNode("name").getNodeValue());
						}
					}

				}

			}



		}

		env.logger.log(Level.FINEST, "Out importDocInfo"); 
	}

	private static void updateElement(Element element, EAEIEnvironmentData env) {


		org.sparx.Element currentElement = env.getRepository().GetElementByGuid(element.getAttribute("GUID"));


		//set package attribute
		if (!element.getAttribute("abstract").isEmpty()) 			currentElement.SetAbstract(element.getAttribute("abstract"));
		if (!element.getAttribute("action_flags").isEmpty()) 		currentElement.SetActionFlags(element.getAttribute("action_flags"));
		if (!element.getAttribute("alias").isEmpty()) 				currentElement.SetAlias(element.getAttribute("alias"));
		if (!element.getAttribute("author").isEmpty()) 				currentElement.SetAuthor(element.getAttribute("author"));
		if (!element.getAttribute("classifier_name").isEmpty()) 	currentElement.SetClassifierName(element.getAttribute("classifier_name"));
		if (!element.getAttribute("classifier_type").isEmpty()) {
			env.logger.log(Level.INFO, element.getAttribute("GUID" + " " + element.getAttribute("name") + " classifier_type is read only and will not be udated")); 
		}
		if (!element.getAttribute("complexity").isEmpty()) 			currentElement.SetComplexity(element.getAttribute("complexity"));
		if (!element.getAttribute("difficulty").isEmpty())	 		currentElement.SetDifficulty(element.getAttribute("difficulty"));
		if (!element.getAttribute("element_id").isEmpty()) {
			env.logger.log(Level.WARNING, element.getAttribute("GUID" + " " + element.getAttribute("name") + " element_id is read only and will not be updated")); 
		}
		if (!element.getAttribute("event_flags").isEmpty()) 		currentElement.SetEventFlags(element.getAttribute("event_flags"));
		if (!element.getAttribute("extension_points").isEmpty()) 	currentElement.SetExtensionPoints(element.getAttribute("extension_points"));
		if (!element.getAttribute("gen_file").isEmpty()) 			currentElement.SetGenfile(element.getAttribute("gen_file"));
		if (!element.getAttribute("gen_links").isEmpty()) 			currentElement.SetGenlinks(element.getAttribute("gen_links"));
		if (!element.getAttribute("gen_type").isEmpty()) 			currentElement.SetGentype(element.getAttribute("gen_type"));
		if (!element.getAttribute("header1").isEmpty()) 			currentElement.SetHeader1(element.getAttribute("header1"));
		if (!element.getAttribute("header2").isEmpty()) 			currentElement.SetHeader2(element.getAttribute("header2"));
		if (!element.getAttribute("meta_type").isEmpty()) 			currentElement.SetMetaType(element.getAttribute("meta_type"));
		if (!element.getAttribute("multiplicity").isEmpty()) 		currentElement.SetMultiplicity(element.getAttribute("multiplicity"));
		if (!element.getAttribute("name").isEmpty()) 				currentElement.SetName(element.getAttribute("name"));
		if (!element.getAttribute("persistence").isEmpty()) 		currentElement.SetPersistence(element.getAttribute("persistence"));
		if (!element.getAttribute("phase").isEmpty()) 				currentElement.SetPhase(element.getAttribute("phase"));
		if (!element.getAttribute("priority").isEmpty()) 			currentElement.SetPriority(element.getAttribute("priority"));
		if (!element.getAttribute("state").isEmpty()) 				currentElement.SetRunState(element.getAttribute("state"));
		if (!element.getAttribute("status").isEmpty()) 				currentElement.SetStatus(element.getAttribute("status"));
		if (!element.getAttribute("stereotype").isEmpty()) 			currentElement.SetStereotype(element.getAttribute("stereotype"));
		if (!element.getAttribute("stereotypeex").isEmpty()) 		currentElement.SetStereotypeEx(element.getAttribute("stereotypeex"));
		if (!element.getAttribute("styleex").isEmpty()) 			currentElement.SetStyleEx(element.getAttribute("styleex"));
		if (!element.getAttribute("tablespace").isEmpty()) 			currentElement.SetTablespace(element.getAttribute("tablespace"));
		if (!element.getAttribute("tag").isEmpty()) 				currentElement.SetTag(element.getAttribute("tag"));
		if (!element.getAttribute("type").isEmpty()) 				currentElement.SetType(element.getAttribute("type"));
		if (!element.getAttribute("version").isEmpty()) 			currentElement.SetVersion(element.getAttribute("version"));
		if (!element.getAttribute("visibility").isEmpty()) 			currentElement.SetVisibility(element.getAttribute("visibility"));


		// Import the notes (child element)
		NodeList notes = element.getElementsByTagName(NOTES);
		if (notes.getLength() >= 1) {
			currentElement.SetNotes(notes.item(0).getTextContent());
		} else {
			env.logger.log(Level.INFO, element.getAttribute("GUID") + " " + element.getAttribute("name") + " Element must have exactly one <" + NOTES + "> element - first (if was exsts) note was imported!"); 
		}


		if (!element.getAttribute("linked_document").isEmpty()) {
			env.logger.log(Level.FINER, element.getAttribute("GUID") + " " + element.getAttribute("name") + " linked_document not empty!");
			if (element.getAttribute("linked_document").equalsIgnoreCase("~delete")) {
				currentElement.DeleteLinkedDocument();
			} else {
				boolean wasLoaded = currentElement.LoadLinkedDocument(env.getInputDir() + "\\" + element.getAttribute("linked_document"));
				if (!wasLoaded) {
					env.logger.log(Level.SEVERE, element.getAttribute("GUID") + " " + element.getAttribute("name") + 
							" - Fail with loading: " +  env.getInputDir() + "\\" + element.getAttribute("linked_document"));
				}
			}
		} else {
			env.logger.log(Level.INFO, element.getAttribute("GUID") + " " + element.getAttribute("name") + " linked_document is empty and not updated!"); 

		}

		boolean updated = currentElement.Update();
		env.logger.log(Level.INFO, "Element: " + currentElement.GetName() + " updated: " + updated);



	}


	private static void updateDiagram(Element element, EAEIEnvironmentData env) {


		org.sparx.Diagram currentDiagram = (Diagram) env.getRepository().GetDiagramByGuid((element.getAttribute("GUID")));

		if (!element.getAttribute("author").isEmpty()) 			currentDiagram.SetAuthor(element.getAttribute("author"));
		if (!element.getAttribute("extended_style").isEmpty()) 	currentDiagram.SetExtendedStyle(element.getAttribute("extended_style"));
		if (!element.getAttribute("meta_type").isEmpty()) 		env.logger.log(Level.WARNING, "Diagram: " + currentDiagram.GetName() + " includes meta_type - the meta_type is read only and will not be updated!");
		if (!element.getAttribute("name").isEmpty()) 			currentDiagram.SetName(element.getAttribute("name"));
		if (!element.getAttribute("drientation").isEmpty()) 	env.logger.log(Level.WARNING, "Diagram: " + currentDiagram.GetName() + " includes drientation - the drientation is read only and will not be updated!");
		if (!element.getAttribute("stereotype").isEmpty()) 		currentDiagram.SetStereotype(element.getAttribute("stereotype"));
		if (!element.getAttribute("stereotypeex").isEmpty())	currentDiagram.SetStyleEx(element.getAttribute("stereotypeex"));
		if (!element.getAttribute("styleex").isEmpty()) 		currentDiagram.SetStyleEx(element.getAttribute("styleex"));
		if (!element.getAttribute("swimlanes").isEmpty()) 		currentDiagram.SetSwimlanes(element.getAttribute("swimlanes"));
		if (!element.getAttribute("type").isEmpty()) 			env.logger.log(Level.WARNING, "Diagram: " + currentDiagram.GetName() + " includes type - the type is read only and will not be updated!");;
		if (!element.getAttribute("version").isEmpty()) 		currentDiagram.SetVersion(element.getAttribute("version"));

		// Import the notes (child element)
		NodeList notes = element.getElementsByTagName(NOTES);
		if (notes.getLength() >= 1) {
			currentDiagram.SetNotes(notes.item(0).getTextContent());
		} else {
			env.logger.log(Level.INFO, element.getAttribute("GUID") + " " + element.getAttribute("name") + " Element must have exactly one <" + NOTES + "> element - first (if was exsts) note was imported!"); 
		}


		boolean updated = currentDiagram.Update();
		env.logger.log(Level.INFO, "Diagram: " + currentDiagram.GetName() + " updated: " + updated);



	}


	private static void updateConnector(Element element, EAEIEnvironmentData env) {


		org.sparx.Connector currentConnector = (Connector) env.getRepository().GetConnectorByGuid((element.getAttribute("GUID")));

		if (!element.getAttribute("alias").isEmpty()) 			currentConnector.SetAlias(element.getAttribute("alias"));
		if (!element.getAttribute("name").isEmpty()) 			currentConnector.SetName(element.getAttribute("name"));

		// Import the notes (child element)
		NodeList notes = element.getElementsByTagName(NOTES);
		if (notes.getLength() >= 1) {
			currentConnector.SetNotes(notes.item(0).getTextContent());
		} else {
			env.logger.log(Level.INFO, element.getAttribute("GUID") + " " + element.getAttribute("name") + " Element must have exactly one <" + NOTES + "> element - first (if was exsts) note was imported!"); 
		}


		boolean updated = currentConnector.Update();
		env.logger.log(Level.INFO, "Connector: " + currentConnector.GetName() + " updated: " + updated);



	}

	private static void updateTaggedValue(Element element, EAEIEnvironmentData env) {


		org.sparx.Element parentCurrentTaggedValue = (org.sparx.Element) env.getRepository().GetElementByID((Integer.valueOf(element.getAttribute("parent_id"))));
		Collection<TaggedValue> taggedValues = parentCurrentTaggedValue.GetTaggedValues();
		short index = 0;
		TaggedValue taggedValue = null;
		Iterator<TaggedValue> itr = taggedValues.iterator();
		TaggedValue tv = null;
		for (index = 0; index < taggedValues.GetCount(); index++) {
			tv = taggedValues.GetAt(index);
			if (tv.GetPropertyGUID().equalsIgnoreCase(element.getAttribute("GUID"))) {
				taggedValue = tv;
				break;
			}
		}

		if (taggedValue == null) {
			taggedValue = taggedValues.AddNew(element.getAttribute("name"), "");
		} else if (!element.getAttribute("_delete").isEmpty()) {
			if (element.getAttribute("_delete").equalsIgnoreCase("yes")) {
				taggedValues.Delete(index);
				return;
			}

		}



		if (!element.getAttribute("value").isEmpty()) {
			taggedValue.SetValue(element.getAttribute("value"));
		}
		NodeList nodeList = element.getElementsByTagName("notes");
		String notes = "";
		for (int i = 0; i < nodeList.getLength(); i++) {
			notes += nodeList.item(0).getTextContent();
			taggedValue.SetNotes(element.getAttribute("notes"));
		}

		taggedValue.SetNotes(notes);



		boolean updated = taggedValue.Update();
		env.logger.log(Level.INFO, "TaggedValue: " + taggedValue.GetName() + " updated: " + updated);



	}





	/**
	 * 
	 * @param packageToExtractGUID - If the package is type of <<master document>> it extracts all the packages that are referred by the inner <<model document>> elements
	 * @param  - The repository file name 
	 * @return Document - DOM object with the exported information
	 * @throws Exception 
	 */

	private static Document exportDocInfo(EAEIEnvironmentData env) throws Exception {
		String packageToExtractGUID = env.getGUID();

		List<org.sparx.Package> packagesToExtractList = new ArrayList<org.sparx.Package>(); 

		env.logger.log(Level.FINEST, "In extractDocInfo"); 

		//
		// Prepare the output Document
		//
		Document outputDoc 		= getOutputDOMDocument();
		Element rootElement 	= appendRootEAEIElement(outputDoc);
		Element outModelElement = outputDoc.createElement(MODEL.toLowerCase());
		Element outReferencesElement = outputDoc.createElement(REFERENCES.toLowerCase());


		Package packageToExtract = getPackageToExtract(packageToExtractGUID, env);

		if (packageToExtract.GetStereotypeEx().indexOf("master document") >= 0) { // If the input is <<master documet>>
			extractPackagesFromMasterDocument(packageToExtract, packagesToExtractList, env);

			extractMasterDocPackageInfo (packageToExtract, outputDoc, outModelElement, env);
			extractPackagesInfo (packagesToExtractList, outputDoc, outModelElement, env);
		} else {
			addPackage(getElementTypeName(PACKAGE, env), packageToExtract, outputDoc, outModelElement, 1, env);
		}


		if (env.shouldReferencesBeAdded()){
			for (org.sparx.Element element : env.getReferenceElementsTable().values()){
				addElement(new String("ref-element"), element, outputDoc, outReferencesElement, env);
			}
			rootElement.appendChild(outReferencesElement);
		}

		rootElement.appendChild(outModelElement);

		env.logger.log(Level.FINEST, "Out extractDocInfo"); 
		return outputDoc;

	}

	/**
	 * 
	 * @param masterDocPackage
	 * @param outputDoc
	 * @param env 
	 * @param outModelElement
	 * @throws Exception 
	 */
	private static void extractMasterDocPackageInfo(Package currentPackage,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) throws Exception {
		env.logger.log(Level.FINEST, "In extractMasterDocPackageInfo");

		if (exportAtt("package.master", env))  addPackage(getElementTypeName("package.master", env), currentPackage, outputDoc, rootElement, 0, env);
		//addElement ("package.element.master", currentPackage.GetElement(), outputDoc, rootElement);

		env.logger.log(Level.FINEST, "Out extractMasterDocPackageInfo"); 

	}


	private static org.sparx.Package getPackageToExtract(
			String packageGUID, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In getPackageToExtract"); 

		org.sparx.Package packageToExtract = null;

		packageToExtract = env.getRepository().GetPackageByGuid(packageGUID);

		if (packageToExtract == null) {
			env.logger.log(Level.SEVERE, "Package was not found GUID: " + packageToExtract); 
		}

		env.logger.log(Level.FINEST, "Out getPackageToExtract"); 

		return 	packageToExtract;

	}



	private static void extractPackagesFromMasterDocument(org.sparx.Package currentMasterDocPackage, List<org.sparx.Package> retPackages, EAEIEnvironmentData env) {

		org.sparx.Attribute att;
		org.sparx.Element classifierElement;
		org.sparx.Package referencedPackage = null;

		env.logger.log(Level.FINEST, "In extractPackagesFromMasterDocument"); 

		env.logger.log(Level.INFO, "Extract the list of packages that should be extracted..."); 
		org.sparx.Element currentElement;
		Collection<org.sparx.Element> elements = currentMasterDocPackage.GetElements();
		if (elements.GetCount() > 0) {
			Iterator<org.sparx.Element> itrElements = elements.iterator();

			while (itrElements.hasNext()) {
				currentElement = itrElements.next();
				if (currentElement.GetStereotypeEx().indexOf("model document") >= 0) {
					for (short i=0; i < currentElement.GetAttributes().GetCount(); i++)
					{
						att = currentElement.GetAttributes().GetAt(i);
						if (att.GetClassifierID() != 0)
						{
							classifierElement = env.getRepository().GetElementByID( att.GetClassifierID() );
							if (classifierElement.GetType().compareTo("Package") == 0)
							{
								//An Element with type == 'Package' will have the same GUID as it's corresponding Package object
								referencedPackage = env.getRepository().GetPackageByGuid( classifierElement.GetElementGUID() );
								env.logger.log(Level.INFO, currentElement.GetName() + " Package[" + i + "] " + referencedPackage.GetName() + " " + referencedPackage.GetPackageGUID());
								retPackages.add(referencedPackage);
								env.getPackageToMetaData().put(referencedPackage.GetPackageGUID(), currentElement.GetTaggedValues());
							}
						}
					}

				}
			}
		}

		env.logger.log(Level.FINEST, "Out extractPackagesFromMasterDocument"); 

	}




	/**
	 * 
	 * @param packages
	 * @param outputDoc
	 * @param rootElement
	 * @param env 
	 * @throws Exception 
	 */
	private static void extractPackagesInfo(List<org.sparx.Package> packages, Document outputDoc, Element rootElement, EAEIEnvironmentData env) throws Exception {

		env.logger.log(Level.FINEST, "In extractPackagesInfo"); 

		org.sparx.Package currentPackage = null;
		for (int i = 0; i < packages.size(); i++) {
			currentPackage = packages.get(i);


			String elementType = getPackageSpecificType (PACKAGE, currentPackage.GetPackageGUID(), env);
			env.logger.log(Level.FINEST, "extractPackagesInfo elementType: " + elementType + " will be exported: " + exportAtt(elementType, env)); 
			if (exportAtt(elementType, env)) {
				addPackage(getElementTypeName(PACKAGE, env), currentPackage, outputDoc, rootElement, 1, env);
			}


		}
		env.logger.log(Level.FINEST, "Out extractPackagesInfo"); 

	}

	private static void addPackage(String elementType, Package currentPackage, Document outputDoc, Element rootElement, int level, EAEIEnvironmentData env) throws Exception {

		Element packageElement 	= outputDoc.createElement(getTagName(elementType, env));

		//set package attribute 
		packageElement.setAttribute("GUID", currentPackage.GetPackageGUID());
		addPackageMetaDataAttributes(currentPackage.GetPackageGUID(), Integer.toString(level), packageElement, env);

		env.logger.log(Level.FINEST, "In addPackage: " + elementType); 


		if (exportAtt(elementType + "." + "package_element", env)) { 			 
			org.sparx.Element elment = currentPackage.GetElement();
			addElement(getElementTypeName(elementType + "." + "package_element", env), elment, outputDoc, packageElement, env);
		}


		if (exportAtt(elementType + "." + "diagrams", env)) { 			 
			Collection<Diagram> diagrams = currentPackage.GetDiagrams();
			if (diagrams.GetCount() > 0) {
				addDiagrams(getElementTypeName(elementType + "." + "diagrams", env), diagrams, outputDoc, packageElement, env);
			}
		}

		if (exportAtt(elementType + "." + "elements", env)) { 			 
			Collection<org.sparx.Element> elements = currentPackage.GetElements();
			if (elements.GetCount() > 0) {
				addElements(getElementTypeName(elementType + "." + "elements", env), elements, outputDoc, packageElement, env);
			}
		}


		if (exportAtt(elementType + "." + "connectors", env)) { 			 
			Collection<Connector> connectors = currentPackage.GetConnectors();
			if (connectors.GetCount() > 0) {
				addConnectors(getElementTypeName(elementType + "." + "connectors", env), connectors, outputDoc, packageElement, env);
			}
		}

		if (exportAtt(elementType + "." + "pakages", env)) { 			 
			Collection<Package> childPackages = currentPackage.GetPackages();
			if (childPackages.GetCount() > 0) {
				addPackages(elementType, childPackages, outputDoc, packageElement, level + 1, env);
			}
		}



		rootElement.appendChild(packageElement);
	}

	private static String getPackageSpecificType(String elementType,
			String getPackageGUID, EAEIEnvironmentData env) {

		Collection<TaggedValue> metaData = env.getPackageToMetaData().get(getPackageGUID);

		String filter = null;
		if (metaData == null ||
				metaData.GetByName(FILTER) == null ||
				(filter = metaData.GetByName(FILTER).GetValue()).isEmpty()) {
			return elementType.trim();
		}
		return elementType + "." + filter;
	}



	private static void addPackageMetaDataAttributes(String packageGUID,
			String level, Element packageElement, EAEIEnvironmentData env) {

		Collection<TaggedValue> metaData = env.getPackageToMetaData().get(packageGUID);

		if (metaData == null) {
			packageElement.setAttribute("level", level);
			return;
		}

		String lLevel; 
		if (metaData.GetByName("level") ==null || 
				(lLevel = metaData.GetByName("level").GetValue()) == null || 
				lLevel.isEmpty()) {
			packageElement.setAttribute("level", level);
		} else {
			packageElement.setAttribute("level", lLevel);
		}

		for (short i = 0; i < metaData.GetCount(); i++) {
			TaggedValue tagValue = metaData.GetAt(i);
			String tag = tagValue.GetName();
			if (tag.startsWith("metadata.")){
				packageElement.setAttribute(tag, tagValue.GetValue());

			}
		}


	}



	private static void addPackages(String elementType, Collection<Package> childPackages,
			Document outputDoc, Element packageElement, int level, EAEIEnvironmentData env) {
		org.sparx.Package currentPackage = null;
		env.logger.log(Level.FINEST, "In addDiagrams elementType:" + elementType); 
		/******************
		 * 

		Element spackageElement	= outputDoc.createElement(getTagName(elementType));

		Iterator<Package> itrDiagrams = childPackages.iterator();

		while (itrDiagrams.hasNext()) {
			currentPackage = itrDiagrams.next();
			if (exportAtt(getElementTypeName(elementType + ".package"))) {
				env.logger.log(Level.FINEST, "addDiagrams element name: " + elementType + "." + currentDiagram.GetType()); 
				addDiagram(getElementTypeName(getElementTypeName(elementType + "." + currentDiagram.GetType())), currentDiagram, outputDoc, diagramsElement, env);
			}
		}
		rootElement.appendChild(diagramsElement);

		env.logger.log(Level.FINEST, "Out addDiagrams"); 
		 *********************/
	}

	private static void addConnectors(String elementType, Collection<Connector> connectors,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		org.sparx.Connector currentConnector = null;
		env.logger.log(Level.FINEST, "In addConnectors elementType:" + elementType);


		Element connectorsElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<Connector> itrConnectors = connectors.iterator();

		while (itrConnectors.hasNext()) {
			currentConnector = itrConnectors.next();

			if (exportAtt(getElementTypeName(elementType + ".connector", env), env)) {
				addConnector(getElementTypeName(getElementTypeName(elementType + ".connector", env), env), currentConnector, outputDoc, connectorsElement, env);
			}
		}

		rootElement.appendChild(connectorsElement);

		env.logger.log(Level.FINEST, "Out addConnectors"); 


	}

	private static void addDiagrams(String elementType, Collection<Diagram> diagrams,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		org.sparx.Diagram currentDiagram = null;
		env.logger.log(Level.FINEST, "In addDiagrams elementType:" + elementType); 

		Element diagramsElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<Diagram> itrDiagrams = diagrams.iterator();

		while (itrDiagrams.hasNext()) {
			currentDiagram = itrDiagrams.next();
			env.logger.log(Level.FINEST, "Attempt to add diagram type: " + getElementTypeName(elementType + "." + currentDiagram.GetType(), env));
			if (exportAtt(getElementTypeName(elementType + "." + currentDiagram.GetType(), env), env)) {
				env.logger.log(Level.FINEST, "addDiagrams element name: " + elementType + "." + currentDiagram.GetType()); 
				addDiagram(getElementTypeName(getElementTypeName(elementType + "." + currentDiagram.GetType(), env), env), currentDiagram, outputDoc, diagramsElement, env);
			}
		}
		rootElement.appendChild(diagramsElement);

		env.logger.log(Level.FINEST, "Out addDiagrams"); 
	}





	private static void addDiagram(String elementType, Diagram currentDiagram, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.INFO, "addDiagram elementType: " + elementType);

		Element diagramElement	= outputDoc.createElement(getTagName(elementType, env));
		Element notesElement 	= outputDoc.createElement(NOTES);

		//set diagram attribute

		diagramElement.setAttribute("GUID", currentDiagram.GetDiagramGUID());

		if (exportAtt(elementType + ".author", env)) 			diagramElement.setAttribute("author", currentDiagram.GetAuthor());
		if (exportAtt(elementType + ".diagram_id", env))		diagramElement.setAttribute("diagram_id", Integer.toString(currentDiagram.GetDiagramID()));
		if (exportAtt(elementType + ".extended_style", env)) 	diagramElement.setAttribute("extended_style", currentDiagram.GetExtendedStyle());
		if (exportAtt(elementType + ".meta_type", env)) 		diagramElement.setAttribute("meta_type", currentDiagram.GetMetaType());
		if (exportAtt(elementType + ".name", env)) 				diagramElement.setAttribute("name", currentDiagram.GetName());
		if (exportAtt(elementType + ".drientation", env)) 		diagramElement.setAttribute("drientation", currentDiagram.GetOrientation());
		if (exportAtt(elementType + ".stereotype", env)) 		diagramElement.setAttribute("stereotype", currentDiagram.GetStereotype());
		if (exportAtt(elementType + ".stereotypeex", env))		diagramElement.setAttribute("stereotypeex", currentDiagram.GetStereotypeEx());
		if (exportAtt(elementType + ".styleex", env)) 			diagramElement.setAttribute("styleex", currentDiagram.GetStyleEx());
		if (exportAtt(elementType + ".swimlanes", env)) 		diagramElement.setAttribute("swimlanes", currentDiagram.GetSwimlanes());
		if (exportAtt(elementType + ".type", env)) 				diagramElement.setAttribute("type", currentDiagram.GetType());
		if (exportAtt(elementType + ".version", env)) 			diagramElement.setAttribute("version", currentDiagram.GetVersion());

		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) { 			
			notesElement.setTextContent(currentDiagram.GetNotes());
			diagramElement.appendChild(notesElement);
		}

		if (exportAtt(elementType + ".diagram_picture", env)) {
			try {
				env.getRepository().GetProjectInterface().PutDiagramImageToFile(currentDiagram.GetDiagramGUID(), addDirIfRequired(DIAGRAMS_DIR, env) + "\\" + currentDiagram.GetDiagramGUID() + ".jpg", 1);
				diagramElement.setAttribute("diagram_location", DIAGRAMS_DIR + "\\" + currentDiagram.GetDiagramGUID() + ".jpg");
			} catch (Exception e) {
				env.logger.log(Level.SEVERE, e.toString());
				e.printStackTrace();
			}
		}


		if (exportAtt(elementType + ".diagram_objects", env)) {
			Collection<DiagramObject> diagramObjects = currentDiagram.GetDiagramObjects();
			if (diagramObjects.GetCount() > 0) {
				addDiagramObjects(getElementTypeName(elementType + "." + DIAGRAM_OBJECTS, env), diagramObjects, outputDoc, diagramElement, env);
			}
		}

		if (exportAtt(elementType + ".diagram_links", env)) {
			Collection<DiagramLink> links = currentDiagram.GetDiagramLinks();

			if (links.GetCount() > 0) {
				addLinks(elementType + ".diagram_links", links, outputDoc, diagramElement, env);
			}
		}

		rootElement.appendChild(diagramElement);

	}


	private static String addDirIfRequired(String dir, EAEIEnvironmentData env) throws Exception {
		File fDir = new File(env.getOutputDir() + "\\" + dir);



		// if the directory does not exist, create it
		if (!fDir.exists()) {
			env.logger.log(Level.INFO, "Creating directory: " + env.getOutputDir() + "\\" + dir); 
			try{
				fDir.mkdir();
			} catch(SecurityException se){
				env.logger.log(Level.WARNING, "Fail to create directory: " + env.getOutputDir() + "\\" + dir);
				throw new Exception("Fail to create directory: " + env.getOutputDir() + "\\" + dir);
			}   
		}

		return env.getOutputDir() + "\\" + dir;

	}


	private static void addLinks(String elementType, Collection<DiagramLink> diagramLinks,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		org.sparx.DiagramLink currentDiagramLink = null;
		Element elementsElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.DiagramLink> itrObjects = diagramLinks.iterator();

		NumberedConnectorGenerator numbersConnectorGenerator = new NumberedConnectorGenerator();


		while (itrObjects.hasNext()) {
			currentDiagramLink = itrObjects.next();
			numbersConnectorGenerator.add(env.getRepository().GetConnectorByID (currentDiagramLink.GetConnectorID()));
		}

		numbersConnectorGenerator.generateNumbering();

		Iterator<NumberedConnector> itr = numbersConnectorGenerator.getIterator();

		while (itr.hasNext()) {
			NumberedConnector con = itr.next();

			addDiagramLink(getElementTypeName(elementType + ".link", env), con, outputDoc, elementsElement, env);

		}

		rootElement.appendChild(elementsElement);

	}


	private static void addDiagramLink(String elementType,
			NumberedConnector con, Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		if (env.getRepository().GetConnectorByID (con.getConnector().GetConnectorID()).GetSequenceNo() > 0 || 
				env.getProperty(elementType.toLowerCase(), "no").equalsIgnoreCase("all")) {


			addConnector (getElementTypeName(elementType, env), con, outputDoc, rootElement, env);
		}

	}





	private static void addConnector(String elementType,
			NumberedConnector con, Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		//set object attribute
		Element objectElement 	= outputDoc.createElement(getTagName(elementType, env));


		if (exportAtt(elementType + ".sequence_no_ea", env)) 				objectElement.setAttribute("sequence_no_ea", con.getEANumbering());
		if (exportAtt(elementType + ".sequence_no_uml", env)) 				objectElement.setAttribute("sequence_no_uml", con.getUMLNumbering());

		populateObjectElement (elementType, con.getConnector(), outputDoc, objectElement, env);

		rootElement.appendChild(objectElement);


	}




	private static void addConnector(String elementType, Connector currentConnector,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		//set object attribute
		Element objectElement 	= outputDoc.createElement(getTagName(elementType, env));


		populateObjectElement (elementType, currentConnector, outputDoc, objectElement, env);

		rootElement.appendChild(objectElement);


	}



	private static void populateObjectElement(String elementType, Connector currentConnector,
			Document outputDoc, Element objectElement, EAEIEnvironmentData env) {

		Element notesElement 	= outputDoc.createElement(NOTES.toLowerCase());
		objectElement.setAttribute("GUID", currentConnector.GetConnectorGUID());

		if (exportAtt(elementType + ".connector_id", env)) 			objectElement.setAttribute("connector_id", Integer.toString(currentConnector.GetConnectorID()));
		if (exportAtt(elementType + ".sequence_no", env)) 			objectElement.setAttribute("sequence_no", Integer.toString((currentConnector.GetSequenceNo())));

		if (exportAtt(elementType + ".direction", env)) 			objectElement.setAttribute("direction", currentConnector.GetDirection());
		if (exportAtt(elementType + ".event_flags", env)) 			objectElement.setAttribute("event_flags", currentConnector.GetEventFlags());
		if (exportAtt(elementType + ".meta_type", env)) 			objectElement.setAttribute("meta_type", currentConnector.GetMetaType());
		if (exportAtt(elementType + ".name", env)) 					objectElement.setAttribute("name", currentConnector.GetName());
		if (exportAtt(elementType + ".state_flags", env)) 			objectElement.setAttribute("state_flags", currentConnector.GetStateFlags());
		if (exportAtt(elementType + ".stereotype", env)) 			objectElement.setAttribute("stereotype", currentConnector.GetStereotype());
		if (exportAtt(elementType + ".stereotypeex", env)) 			objectElement.setAttribute("stereotypeex", currentConnector.GetStereotypeEx());
		if (exportAtt(elementType + ".styleex", env)) 				objectElement.setAttribute("styleex", currentConnector.GetStyleEx());
		if (exportAtt(elementType + ".subtype", env)) 				objectElement.setAttribute("subtype", currentConnector.GetSubtype());
		if (exportAtt(elementType + ".transition_action", env)) 	objectElement.setAttribute("transition_action", currentConnector.GetTransitionAction());
		if (exportAtt(elementType + ".transition_event", env)) 		objectElement.setAttribute("transition_event", currentConnector.GetTransitionEvent());
		if (exportAtt(elementType + ".transition_guard", env)) 		objectElement.setAttribute("transition_guard", currentConnector.GetTransitionGuard());
		if (exportAtt(elementType + ".type", env)) 					objectElement.setAttribute("type", currentConnector.GetType());
		if (exportAtt(elementType + ".virtual_inheritance", env)) 	objectElement.setAttribute("virtual_inheritance", currentConnector.GetVirtualInheritance());
		if (exportAtt(elementType + ".is_leaf", env)) 				objectElement.setAttribute("is_leaf", (currentConnector.GetIsLeaf()?"yes":"no"));
		if (exportAtt(elementType + ".is_root", env)) 				objectElement.setAttribute("is_root", (currentConnector.GetIsRoot()?"yes":"no"));
		if (exportAtt(elementType + ".is_spec", env)) 				objectElement.setAttribute("is_spec", (currentConnector.GetIsSpec()?"yes":"no"));


		if (exportAtt(elementType + ".client_id", env)) 				objectElement.setAttribute("client_id", Integer.toString(currentConnector.GetClientID()));
		if (exportAtt(elementType + ".client_name", env)) 			objectElement.setAttribute("client_name", env.getRepository().GetElementByID(currentConnector.GetClientID()).GetName());
		if (exportAtt(elementType + ".supplier_id", env)) 			objectElement.setAttribute("supplier_id", Integer.toString(currentConnector.GetSupplierID()));
		if (exportAtt(elementType + ".supplier_name", env)) 			objectElement.setAttribute("supplier_name", env.getRepository().GetElementByID(currentConnector.GetSupplierID()).GetName());


		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) { 			
			notesElement.setTextContent(currentConnector.GetNotes());
			objectElement.appendChild(notesElement);
		}


		if (exportAtt(elementType + ".connector_constraints", env)) {
			Collection<ConnectorConstraint> constraintObjects = currentConnector.GetConstraints();
			if (constraintObjects.GetCount() > 0) {
				addConnectorConstraintObjects(getElementTypeName(elementType + ".connector_constraints", env), constraintObjects, outputDoc, objectElement);
			}
		}

		if (exportAtt(elementType + ".custom_properties", env)) {
			Collection<CustomProperty> customPropertiesObjects = currentConnector.GetCustomProperties();
			if (customPropertiesObjects.GetCount() > 0) {
				addCustomPropertiesObjectsObjects(getElementTypeName(elementType + ".custom_properties", env), customPropertiesObjects, outputDoc, objectElement, env);
			}
		}

		if (exportAtt(elementType + ".cCustom_properties", env)) {
			Collection<ConnectorTag> connectorTagObjects = currentConnector.GetTaggedValues();
			if (connectorTagObjects.GetCount() > 0) {
				addConnectorTagObjects(getElementTypeName(elementType + ".connector_tag", env), connectorTagObjects, outputDoc, objectElement);
			}
		}



		if (env.shouldReferencesBeAdded()) {
			addReferenceElement (currentConnector.GetClientID(), env);
			addReferenceElement (currentConnector.GetSupplierID(), env);
		}

	}


	private static void addReferenceElement(int elementID, EAEIEnvironmentData env) {
		org.sparx.Element element;

		if (!env.getReferenceElementsTable().containsKey(elementID)) {
			element 	= env.getRepository().GetElementByID(elementID);
			env.getReferenceElementsTable().put(elementID, element);
		}
	}


	private static void addConnectorTagObjects(String elementTypeName,
			Collection<ConnectorTag> connectorTagObjects, Document outputDoc,
			Element objectElement) {
		// TODO Auto-generated method stub

	}




	private static void addCustomPropertiesObjectsObjects(
			String elementTypeName,
			Collection<CustomProperty> customPropertiesObjects,
			Document outputDoc, Element objectElement, EAEIEnvironmentData env) {
		// TODO Auto-generated method stub

	}




	private static void addConnectorConstraintObjects(String elementTypeName,
			Collection<ConnectorConstraint> constraintObjects,
			Document outputDoc, Element objectElement) {
		// TODO Auto-generated method stub

	}




	private static void addDiagramObjects(String elementType, 
			Collection<DiagramObject> diagramObjects, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {
		org.sparx.DiagramObject currentDiagramObject = null;
		Element elementsElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.DiagramObject> itrObjects = diagramObjects.iterator();

		while (itrObjects.hasNext()) {
			currentDiagramObject = itrObjects.next();
			addElement(getElementTypeName(elementType + ".diagram_object", env), env.getRepository().GetElementByID (currentDiagramObject.GetElementID()),
					outputDoc, elementsElement, env);

		}


		rootElement.appendChild(elementsElement);


	}


	private static void addElements(String elementType, Collection<org.sparx.Element> elements,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		org.sparx.Element currentElement = null;
		Element elementsElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Element> itrElements = elements.iterator();

		while (itrElements.hasNext()) {
			currentElement = itrElements.next();
			addElement(getElementTypeName(elementType + "." + ELEMENT, env), currentElement, outputDoc, elementsElement, env);
		}


		rootElement.appendChild(elementsElement);

	}

	private static void addElement(String elementType, org.sparx.Element currentElement,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		Element elementElement 	= outputDoc.createElement(getTagName(elementType, env));
		Element notesElement 	= outputDoc.createElement(NOTES.toLowerCase());

		//set package attribute

		elementElement.setAttribute("GUID", currentElement.GetElementGUID());

		if (exportAtt(elementType + ".abstract", env)) 			elementElement.setAttribute("abstract", currentElement.GetAbstract());
		if (exportAtt(elementType + ".action_flags", env)) 		elementElement.setAttribute("action_flags", currentElement.GetActionFlags());
		if (exportAtt(elementType + ".alias", env)) 				elementElement.setAttribute("alias", currentElement.GetAlias());
		if (exportAtt(elementType + ".author", env)) 			elementElement.setAttribute("author", currentElement.GetAuthor());
		if (exportAtt(elementType + ".classifier_name", env)) 	elementElement.setAttribute("classifier_name", currentElement.GetClassifierName());
		if (exportAtt(elementType + ".classifier_type", env)) 	elementElement.setAttribute("classifier_type", currentElement.GetClassifierType());
		if (exportAtt(elementType + ".complexity", env)) 		elementElement.setAttribute("complexity", currentElement.GetComplexity());
		if (exportAtt(elementType + ".difficulty", env)) 		elementElement.setAttribute("difficulty", currentElement.GetDifficulty());
		if (exportAtt(elementType + ".element_id", env)) 		elementElement.setAttribute("element_id", Integer.toString(currentElement.GetElementID()));
		if (exportAtt(elementType + ".event_flags", env)) 		elementElement.setAttribute("event_flags", currentElement.GetEventFlags());
		if (exportAtt(elementType + ".extension_points", env)) 	elementElement.setAttribute("extension_points", currentElement.GetExtensionPoints());
		if (exportAtt(elementType + ".gen_file", env)) 			elementElement.setAttribute("gen_file", currentElement.GetGenfile());
		if (exportAtt(elementType + ".gen_links", env)) 			elementElement.setAttribute("gen_links", currentElement.GetGenlinks());
		if (exportAtt(elementType + ".gen_type", env)) 			elementElement.setAttribute("gen_type", currentElement.GetGentype());
		if (exportAtt(elementType + ".header1", env)) 			elementElement.setAttribute("header1", currentElement.GetHeader1());
		if (exportAtt(elementType + ".header2", env)) 			elementElement.setAttribute("header2", currentElement.GetHeader2());
		if (exportAtt(elementType + ".meta_type", env)) 			elementElement.setAttribute("meta_type", currentElement.GetMetaType());
		if (exportAtt(elementType + ".multiplicity", env)) 		elementElement.setAttribute("multiplicity", currentElement.GetMultiplicity());
		if (exportAtt(elementType + ".name", env)) 				elementElement.setAttribute("name", currentElement.GetName());
		if (exportAtt(elementType + ".persistence", env)) 		elementElement.setAttribute("persistence", currentElement.GetPersistence());
		if (exportAtt(elementType + ".phase", env)) 				elementElement.setAttribute("phase", currentElement.GetPhase());
		if (exportAtt(elementType + ".priority", env)) 			elementElement.setAttribute("priority", currentElement.GetPriority());
		if (exportAtt(elementType + ".state", env)) 				elementElement.setAttribute("state", currentElement.GetRunState());
		if (exportAtt(elementType + ".status", env)) 			elementElement.setAttribute("status", currentElement.GetStatus());
		if (exportAtt(elementType + ".stereotype", env)) 		elementElement.setAttribute("stereotype", currentElement.GetStereotype());
		if (exportAtt(elementType + ".stereotypeex", env)) 		elementElement.setAttribute("stereotypeex", currentElement.GetStereotypeEx());
		if (exportAtt(elementType + ".styleex", env)) 			elementElement.setAttribute("styleex", currentElement.GetStyleEx());
		if (exportAtt(elementType + ".tablespace", env)) 		elementElement.setAttribute("tablespace", currentElement.GetTablespace());
		if (exportAtt(elementType + ".tag", env)) 				elementElement.setAttribute("tag", currentElement.GetTag());
		if (exportAtt(elementType + ".type", env)) 				elementElement.setAttribute("type", currentElement.GetType());
		if (exportAtt(elementType + ".version", env)) 			elementElement.setAttribute("version", currentElement.GetVersion());
		if (exportAtt(elementType + ".visibility", env)) 		elementElement.setAttribute("visibility", currentElement.GetVisibility());


		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) { 		
			notesElement.setTextContent(currentElement.GetNotes());
			elementElement.appendChild(notesElement);
		}


		if (exportAtt(elementType + ".linked_document", env)) {
			if (!currentElement.GetLinkedDocument().isEmpty()) {
				try {
					elementElement.setAttribute("linked_document", saveLinkedDocument(currentElement, env));
				} catch (DOMException e) {
					env.logger.log(Level.SEVERE, e.toString());
				} catch (Exception e) {
					env.logger.log(Level.SEVERE, e.toString());
				}
			}
		}

		// checking....

		if (exportAtt(elementType + ".connectors", env)) { 			 
			Collection<Connector> connectors = currentElement.GetConnectors();
			if (connectors.GetCount() > 0) {
				addConnectors(getElementTypeName(elementType + ".connectors", env), connectors, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".attributes", env)) { 			 
			Collection<Attribute> attributes = currentElement.GetAttributes();
			if (attributes.GetCount() > 0) {
				addAttributes(getElementTypeName(elementType + ".attributes", env), attributes, outputDoc, elementElement, env);
			}
		}


		if (exportAtt(elementType + ".attributesexs", env)) { 			 
			Collection<Attribute> attributesExs = currentElement.GetAttributesEx();
			if (attributesExs.GetCount() > 0) {
				addAttributesExs(getElementTypeName(elementType + ".attributesExs", env), attributesExs, outputDoc, elementElement, env);
			}
		}


		if (exportAtt(elementType + ".baseclasses", env)) { 			 
			Collection<org.sparx.Element> baseClasses = currentElement.GetBaseClasses();
			if (baseClasses.GetCount() > 0) {
				addBaseClasses(elementType + ".baseClasses", baseClasses, outputDoc, elementElement);
			}
		}

		if (exportAtt(elementType + ".constraints", env)) { 			 
			Collection<Constraint> constraints = currentElement.GetConstraints();
			if (constraints.GetCount() > 0) {
				addConstraints(constraints, outputDoc, elementElement);
			}
		}


		if (exportAtt(elementType + ".constraintsexs", env)) { 			 
			Collection<Constraint> constraintsExs = currentElement.GetConstraintsEx();
			if (constraintsExs.GetCount() > 0) {
				addConstraintsExs(constraintsExs, outputDoc, elementElement);
			}
		}

		if (exportAtt(elementType + ".customProperties", env)) { 			 
			Collection<CustomProperty> customProperties = currentElement.GetCustomProperties();
			if (customProperties.GetCount() > 0) {
				addCustomProperties(elementType + ".customProperties", customProperties, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".diagrams", env)) { 			 
			Collection<Diagram> diagrams = currentElement.GetDiagrams();
			if (diagrams.GetCount() > 0) {
				addDiagrams(getElementTypeName(elementType + ".diagrams", env), diagrams, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".baseclasses", env)) { 			 
			Collection<Effort> efforts = currentElement.GetEfforts();
			if (efforts.GetCount() > 0) {
				addEfforts(elementType + ".baseClasses", efforts, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".elements", env)) { 			 
			Collection<org.sparx.Element> elements = currentElement.GetElements();
			if (elements.GetCount() > 0) {
				addElements(getElementTypeName(elementType + ".elements", env), elements, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".embeddedelements", env)) { 			 
			Collection<org.sparx.Element> embeddedElements = currentElement.GetEmbeddedElements();
			if (embeddedElements.GetCount() > 0) {
				addElements(getElementTypeName(elementType + ".embeddedElements", env), embeddedElements, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".baseClasses", env)) { 			 
			Collection<org.sparx.File> files = currentElement.GetFiles();
			if (files.GetCount() > 0) {
				addFiles(elementType + ".baseClasses", files, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".issues", env)) { 			 
			Collection<Issue> issues = currentElement.GetIssues();
			if (issues.GetCount() > 0) {
				addIssues(getElementTypeName(elementType + ".issues", env), issues, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".methods", env)) { 			 
			Collection<Method> methods = currentElement.GetMethods();
			if (methods.GetCount() > 0) {
				addMethods(getElementTypeName(elementType + ".methods", env), methods, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".methodsExs", env)) { 			 
			Collection<Method> methodsExs = currentElement.GetMethodsEx();
			if (methodsExs.GetCount() > 0) {
				addMethodsExs(getElementTypeName(elementType + ".methodsExs", env), methodsExs, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".metrics", env)) { 			 
			Collection<Metric> metrics = currentElement.GetMetrics();
			if (metrics.GetCount() > 0) {
				addMetrics(getElementTypeName(elementType + ".metrics", env), metrics, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".partisions", env)) { 			 
			Collection<Partition> partisions = currentElement.GetPartitions();
			if (partisions.GetCount() > 0) {
				addPartisions(elementType + ".partisions", partisions, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".realizes", env)) { 			 
			Collection<org.sparx.Element> realizes = currentElement.GetRealizes();
			if (realizes.GetCount() > 0) {
				addRealizes(elementType + ".realizes", realizes, outputDoc, elementElement, env);
			}
		}


		if (exportAtt(elementType + ".tagged_values", env)) { 			 
			Collection<TaggedValue> taggedValues = currentElement.GetTaggedValues();
			if (taggedValues.GetCount() > 0) {
				addTaggedValues(elementType + ".tagged_values", taggedValues, outputDoc, elementElement, env);
			}
		}

		if (exportAtt(elementType + ".tagged_valuesexs", env)) { 			 
			Collection<TaggedValue> taggedValuesEx = currentElement.GetTaggedValuesEx();
			if (taggedValuesEx.GetCount() > 0) {
				addTaggedValues(elementType + ".tagged_valuesexs", taggedValuesEx, outputDoc, elementElement, env);
			}
		}


		rootElement.appendChild(elementElement);
	}

	private static String saveLinkedDocument(org.sparx.Element currentElement, EAEIEnvironmentData env) throws Exception {
		File linkedDocDir = new File(env.getOutputDir() + "\\" + LINK_DOCUMENTS);


		env.logger.log(Level.INFO, "Save linked document file: " + env.getOutputDir() + "\\" + LINK_DOCUMENTS + "\\" + currentElement.GetElementGUID() + LINKED_DOC_POSTFIX);


		boolean b = currentElement.SaveLinkedDocument(addDirIfRequired(LINK_DOCUMENTS, env) + "\\" + currentElement.GetElementGUID() + LINKED_DOC_POSTFIX);

		return  ".\\" + LINK_DOCUMENTS + "\\" + currentElement.GetElementGUID() + LINKED_DOC_POSTFIX;

	}



	private static boolean exportAtt(String key, EAEIEnvironmentData env) {

		boolean ret = env.getProperty(key.toLowerCase(), "no").trim().equalsIgnoreCase("yes");
		env.logger.log(Level.FINEST, "exportAtt - key  " + key.toLowerCase() + ": " + ret + ": " + env.getProperty(key, "no"));


		env.getExportFullConfiguration().add((Object)(key + " = " + (ret?"yes":"no") + "\r\n"));

		return ret;
	}

	private static void addTaggedValues(String elementType, Collection<TaggedValue> taggedValues,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		TaggedValue currentTaggedValue = null;
		Element taggedValuesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<TaggedValue> itrElements = taggedValues.iterator();

		while (itrElements.hasNext()) {
			currentTaggedValue = itrElements.next();
			addTaggedValue(elementType + "." + TAGGED_VALUE, currentTaggedValue, outputDoc, taggedValuesElement, env);
		}


		rootElement.appendChild(taggedValuesElement);

	}


	private static void addAttributeTaggedValues(String elementType, Collection<AttributeTag> taggedValues,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		AttributeTag currentTaggedValue = null;
		Element taggedValuesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<AttributeTag> itrElements = taggedValues.iterator();

		while (itrElements.hasNext()) {
			currentTaggedValue = itrElements.next();
			//			addAttributeTaggedValue(elementType + "." + TAGGED_VALUE, currentTaggedValue, outputDoc, taggedValuesElement);
		}


		rootElement.appendChild(taggedValuesElement);

	}

	private static void addTaggedValue(String elementType,
			TaggedValue currentTaggedValue, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {

		Element taggedValueElement 	= outputDoc.createElement(getTagName(elementType, env));
		Element notesElement 	= outputDoc.createElement(NOTES.toLowerCase());

		//set object attribute

		taggedValueElement.setAttribute("GUID", currentTaggedValue.GetPropertyGUID());


		if (exportAtt(elementType + ".parent_id", env)) 				taggedValueElement.setAttribute("parent_id", Integer.toString(currentTaggedValue.GetParentID()));
		if (exportAtt(elementType + ".name", env)) 				taggedValueElement.setAttribute("name", currentTaggedValue.GetName());
		if (exportAtt(elementType + ".element_id", env)) 		taggedValueElement.setAttribute("element_id", Integer.toString(currentTaggedValue.GetElementID()));
		if (exportAtt(elementType + ".property_id", env)) 		taggedValueElement.setAttribute("property_id", Integer.toString(currentTaggedValue.GetPropertyID()));
		if (exportAtt(elementType + ".value", env)) 			taggedValueElement.setAttribute("value", currentTaggedValue.GetValue());

		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) { 			
			notesElement.setTextContent(currentTaggedValue.GetNotes());
			taggedValueElement.appendChild(notesElement);
		}


		rootElement.appendChild(taggedValueElement);

	}

	private static void addRealizes(String elementType, Collection<org.sparx.Element> realizes,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addRealizes");

		org.sparx.Element currentRealize = null;
		Element realizesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Element> itrRealizes = realizes.iterator();

		while (itrRealizes.hasNext()) {
			currentRealize = itrRealizes.next();
			env.logger.log(Level.FINEST, "Add realize element: " + currentRealize.GetName()); 

			addElement(getElementTypeName(REALIZ_ELEMENT, env), currentRealize, outputDoc, realizesElement, env);

		}
		rootElement.appendChild(realizesElement);

		env.logger.log(Level.FINEST, "Out addRealizes"); 



	}

	private static void addPartisions(String elementType, Collection<Partition> partitions,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addPartisions", env);

		org.sparx.Partition currentPartition = null;
		Element partitionElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Partition> itrPartitions = partitions.iterator();

		while (itrPartitions.hasNext()) {
			currentPartition = itrPartitions.next();
			env.logger.log(Level.FINEST, "Add partision element: " + currentPartition.GetName()); 

			addPartition(PARTITION, currentPartition, outputDoc, partitionElement);

		}
		rootElement.appendChild(partitionElement);

		env.logger.log(Level.FINEST, "Out addPartisions"); 


	}

	private static void addPartition(String partition2, Partition currentPartition,
			Document outputDoc, Element partitionElement) {
		// TODO Auto-generated method stub

	}



	private static void addMetrics(String elementType, Collection<Metric> metrics,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addMetrics");

		org.sparx.Metric currentMetric = null;
		Element metricElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Metric> itrPartitions = metrics.iterator();

		while (itrPartitions.hasNext()) {
			currentMetric = itrPartitions.next();
			env.logger.log(Level.FINEST, "Add metric element: " + currentMetric.GetName()); 

			addMetric(getElementTypeName(elementType + METRIC, env), currentMetric, outputDoc, metricElement);

		}
		rootElement.appendChild(metricElement);

		env.logger.log(Level.FINEST, "Out addMetrics"); 

	}

	private static void addMetric(String metric2, Metric currentMetric,
			Document outputDoc, Element metricElement) {
		// TODO Auto-generated method stub

	}



	private static void addMethodsExs(String elementType, Collection<Method> methodsExs,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addMethodsExs");

		org.sparx.Method currentMethod = null;
		Element methodsExElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Method> itrMethodsExs = methodsExs.iterator();

		while (itrMethodsExs.hasNext()) {
			currentMethod = itrMethodsExs.next();
			env.logger.log(Level.FINEST, "Add partisions element: " + currentMethod.GetName()); 

			addMethodsEx(getElementTypeName(elementType + METHODEX, env), currentMethod, outputDoc, methodsExElement, env);

		}
		rootElement.appendChild(methodsExElement);

		env.logger.log(Level.FINEST, "Out addMethodsExs"); 

	}


	private static void addMethodsEx(String elementType, Method currentMethod,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		Element methodElement	= outputDoc.createElement(getTagName(elementType, env));
		Element notesElement 	= outputDoc.createElement(NOTES.toLowerCase());

		//set attribute
		methodElement.setAttribute("GUID", currentMethod.GetMethodGUID());

		if (exportAtt(elementType + ".classifier_id", env)) 	methodElement.setAttribute("classifier_id", currentMethod.GetClassifierID());

		if (exportAtt(elementType + ".methof_id", env))			methodElement.setAttribute("methof_id", Integer.toString(currentMethod.GetMethodID()));
		if (exportAtt(elementType + ".name", env))				methodElement.setAttribute("name", currentMethod.GetName());
		if (exportAtt(elementType + ".behavior", env))			methodElement.setAttribute("behavior", currentMethod.GetBehavior());
		if (exportAtt(elementType + ".behaviour", env))			methodElement.setAttribute("behaviour", currentMethod. GetBehaviour());
		if (exportAtt(elementType + ".code", env))				methodElement.setAttribute("code", currentMethod.GetCode());
		if (exportAtt(elementType + ".concurrency", env))		methodElement.setAttribute("concurrency", currentMethod.GetConcurrency());
		if (exportAtt(elementType + ".return_type", env))		methodElement.setAttribute("return_type", currentMethod.GetReturnType());
		if (exportAtt(elementType + ".state_flags", env))		methodElement.setAttribute("state_flags", currentMethod.GetStateFlags());
		if (exportAtt(elementType + ".stereotype", env))			methodElement.setAttribute("stereotype", currentMethod.GetStereotype());
		if (exportAtt(elementType + ".stereotypeex", env))		methodElement.setAttribute("stereotypeex", currentMethod.GetStereotypeEx());
		if (exportAtt(elementType + ".style", env))				methodElement.setAttribute("style", currentMethod.GetStyle());
		if (exportAtt(elementType + ".styleex", env))			methodElement.setAttribute("styleex", currentMethod.GetStyleEx());
		if (exportAtt(elementType + ".throws", env))				methodElement.setAttribute("throws", currentMethod.GetThrows());
		if (exportAtt(elementType + ".visibility", env))			methodElement.setAttribute("visibility", currentMethod.GetVisibility());
		if (exportAtt(elementType + ".abstract", env))			methodElement.setAttribute("abstract", (currentMethod.GetAbstract()?"yes":"no"));
		if (exportAtt(elementType + ".is_const", env))			methodElement.setAttribute("is_const", (currentMethod.GetIsConst()?"yes":"no"));
		if (exportAtt(elementType + ".is_leaf", env))			methodElement.setAttribute("is_leaf", (currentMethod.GetIsLeaf()?"yes":"no"));
		if (exportAtt(elementType + ".is_pure", env))			methodElement.setAttribute("is_pure", (currentMethod.GetIsPure()?"yes":"no"));
		if (exportAtt(elementType + ".is_query", env))			methodElement.setAttribute("is_query", (currentMethod.GetIsQuery()?"yes":"no"));
		if (exportAtt(elementType + ".is_root", env))			methodElement.setAttribute("is_root", (currentMethod.GetIsRoot()?"yes":"no"));
		if (exportAtt(elementType + ".is_static", env))			methodElement.setAttribute("is_static", (currentMethod.GetIsStatic()?"yes":"no"));
		if (exportAtt(elementType + ".is_synchronized", env))	methodElement.setAttribute("is_synchronized", (currentMethod.GetIsSynchronized()?"yes":"no"));
		if (exportAtt(elementType + ".is_array", env))			methodElement.setAttribute("is_array", (currentMethod.GetReturnIsArray()?"yes":"no"));


		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) {
			notesElement.setTextContent(currentMethod.GetNotes());
			methodElement.appendChild(notesElement);
		}


		if (exportAtt(elementType + ".tagged_values", env)) {
			Collection<MethodTag> tagValueObjects = currentMethod.GetTaggedValues();
			if (tagValueObjects.GetCount() > 0) {
				addMethodTag(elementType + ".tagged_values", tagValueObjects, outputDoc, methodElement);
			}
		}

		if (exportAtt(elementType + ".parameters", env)) {
			Collection<Parameter> parametersObjects = currentMethod.GetParameters();
			if (parametersObjects.GetCount() > 0) {
				addParameters(elementType + ".parameters", parametersObjects, outputDoc, methodElement);
			}
		}

		if (exportAtt(elementType + ".post_conditions", env)) {
			Collection<MethodConstraint> postConditionsObjects = currentMethod.GetPostConditions();
			if (postConditionsObjects.GetCount() > 0) {
				addPostConstraints(elementType + ".post_conditions", postConditionsObjects, outputDoc, methodElement);
			}
		}

		if (exportAtt(elementType + ".pre_conditions", env)) {
			Collection<MethodConstraint> preConditionsObjects = currentMethod.GetPreConditions();
			if (preConditionsObjects.GetCount() > 0) {
				addPostConstraints(elementType + ".pre_conditions", preConditionsObjects, outputDoc, methodElement);
			}
		}


		rootElement.appendChild(methodElement);

	}



	private static void addPostConstraints(String string,
			Collection<MethodConstraint> postConditionsObjects,
			Document outputDoc, Element methodElement) {
		// TODO Auto-generated method stub


	}



	private static void addParameters(String string,
			Collection<Parameter> parametersObjects, Document outputDoc,
			Element methodElement) {
		// TODO Auto-generated method stub

	}



	private static void addMethodTag(String string,
			Collection<MethodTag> tagValueObjects, Document outputDoc,
			Element methodElement) {
		// TODO Auto-generated method stub

	}



	private static void addMethods(String elementType, Collection<org.sparx.Method> methods,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addMethods");

		org.sparx.Method currentMethod = null;
		Element methodElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Method> itrMethods = methods.iterator();

		while (itrMethods.hasNext()) {
			currentMethod = itrMethods.next();
			env.logger.log(Level.FINEST, "Add method element: " + currentMethod.GetName()); 

			addMethod(getElementTypeName(elementType + METHOD, env), currentMethod, outputDoc, methodElement);

		}
		rootElement.appendChild(methodElement);

		env.logger.log(Level.FINEST, "Out addMethods"); 


	}

	private static void addMethod(String method2, Method currentMethod,
			Document outputDoc, Element methodElement) {
		// TODO Auto-generated method stub

	}



	private static void addIssues(String elementType, Collection<Issue> issues, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addIssues");

		org.sparx.Issue currentIssue = null;
		Element issueElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Issue> itrIssues = issues.iterator();

		while (itrIssues.hasNext()) {
			currentIssue = itrIssues.next();
			env.logger.log(Level.FINEST, "Add issue element: " + currentIssue.GetName()); 

			addIssue(getElementTypeName(elementType + ISSUE, env), currentIssue, outputDoc, issueElement);

		}
		rootElement.appendChild(issueElement);

		env.logger.log(Level.FINEST, "Out addIssues"); 

	}

	private static void addIssue(String issue2, Issue currentIssue,
			Document outputDoc, Element issueElement) {
		// TODO Auto-generated method stub

	}



	private static void addFiles(String elementType, Collection<org.sparx.File> files,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addFiles");

		org.sparx.File currentFile = null;
		Element fileElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.File> itrPartitions = files.iterator();

		while (itrPartitions.hasNext()) {
			currentFile = itrPartitions.next();
			env.logger.log(Level.FINEST, "Add file element: " + currentFile.GetName()); 

			addEPartition(FILE, currentFile, outputDoc, fileElement, env);

		}
		rootElement.appendChild(fileElement);

		env.logger.log(Level.FINEST, "Out addFiles"); 

	}

	private static void addEPartition(String file2, org.sparx.File currentFile,
			Document outputDoc, Element fileElement, EAEIEnvironmentData env) {
		// TODO Auto-generated method stub

	}



	private static void addEfforts(String elementType, Collection<Effort> efforts,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {

		env.logger.log(Level.FINEST, "In addEfforts");

		org.sparx.Effort currentEffort = null;
		Element effortElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<org.sparx.Effort> itrEfforts = efforts.iterator();

		while (itrEfforts.hasNext()) {
			currentEffort = itrEfforts.next();
			env.logger.log(Level.FINEST, "Add effort element: " + currentEffort.GetName()); 

			addEffort(EFFORT, currentEffort, outputDoc, effortElement);

		}
		rootElement.appendChild(effortElement);

		env.logger.log(Level.FINEST, "Out addEfforts"); 

	}

	private static void addEffort(String effort2, Effort currentEffort,
			Document outputDoc, Element effortElement) {
		// TODO Auto-generated method stub

	}



	private static void addCustomProperties(String elementType, 
			Collection<CustomProperty> customProperties, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {
		CustomProperty currentCustomProperty = null;
		Element customPropertiesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<CustomProperty> itrElements = customProperties.iterator();

		while (itrElements.hasNext()) {
			currentCustomProperty = itrElements.next();
			addCustomProperty(elementType + "." + CUSTOM_PROPERTY, currentCustomProperty, outputDoc, customPropertiesElement, env);
		}


		rootElement.appendChild(customPropertiesElement);

	}

	private static void addCustomProperty(String elementType,
			CustomProperty currentCustomProperty, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {

		Element customPropertyElement 	= outputDoc.createElement(getTagName(elementType, env));

		//set object attribute
		if (exportAtt(elementType + ".name", null)) 			customPropertyElement.setAttribute("name", currentCustomProperty.GetName());
		if (exportAtt(elementType + ".value", null)) 			customPropertyElement.setAttribute("value", currentCustomProperty.GetValue());

		rootElement.appendChild(customPropertyElement);

	}

	private static void addConstraintsExs(
			Collection<Constraint> constraintsExs, Document outputDoc,
			Element rootElement) {
		// TODO Auto-generated method stub

	}

	private static void addConstraints(Collection<Constraint> constraints,
			Document outputDoc, Element rootElement) {
		// TODO Auto-generated method stub

	}

	private static void addBaseClasses(String elementType, 
			Collection<org.sparx.Element> baseClasses, Document outputDoc,
			Element rootElement) {
		// TODO Auto-generated method stub

	}

	private static void addAttributesExs(String elementType, Collection<Attribute> attributesExs,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		Attribute currentAttributee = null;
		Element attributesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<Attribute> itrElements = attributesExs.iterator();

		while (itrElements.hasNext()) {
			currentAttributee = itrElements.next();
			addAttribureEx(getElementTypeName(elementType + "." + ATTRIBUTEEX, env), currentAttributee, outputDoc, attributesElement, env);
		}


		rootElement.appendChild(attributesElement);

	}

	private static void addAttribureEx(String elementType,
			Attribute currentAttribute, Document outputDoc,
			Element rootElement, EAEIEnvironmentData env) {

		Element attributeElement	= outputDoc.createElement(getTagName(elementType, env));
		Element notesElement 	= outputDoc.createElement(NOTES.toLowerCase());

		//set attribute
		attributeElement.setAttribute("GUID", currentAttribute.GetAttributeGUID());

		if (exportAtt(elementType + ".classifier_id", null)) 	attributeElement.setAttribute("classifier_id", Integer.toString(currentAttribute.GetClassifierID()));

		if (exportAtt(elementType + ".attribute_id", env))		attributeElement.setAttribute("attribute_id", Integer.toString(currentAttribute.GetAttributeID()));
		if (exportAtt(elementType + ".name", env))				attributeElement.setAttribute("name", currentAttribute.GetName());
		if (exportAtt(elementType + ".stereotype", env))		attributeElement.setAttribute("stereotype", currentAttribute.GetStereotype());
		if (exportAtt(elementType + ".stereotypeex", env))		attributeElement.setAttribute("stereotypeex", currentAttribute.GetStereotypeEx());
		if (exportAtt(elementType + ".styleex", env))			attributeElement.setAttribute("styleex", currentAttribute.GetStyleEx());
		if (exportAtt(elementType + ".type", env))				attributeElement.setAttribute("type", currentAttribute.GetType());
		if (exportAtt(elementType + ".containment", env))		attributeElement.setAttribute("containment", currentAttribute.GetContainment());
		if (exportAtt(elementType + ".default", env))			attributeElement.setAttribute("default", currentAttribute.GetDefault());
		if (exportAtt(elementType + ".precision", env))			attributeElement.setAttribute("precision", currentAttribute.GetPrecision());




		// Add the notes as child element
		if (exportAtt(elementType + ".notes", env)) {
			notesElement.setTextContent(currentAttribute.GetNotes());
			attributeElement.appendChild(notesElement);
		}


		if (exportAtt(elementType + ".tagged_values", null)) {
			Collection<AttributeTag> tagValueObjects = currentAttribute.GetTaggedValuesEx();
			if (tagValueObjects.GetCount() > 0) {
				addAttributeTaggedValues(elementType + ".tagged_values", tagValueObjects, outputDoc, attributeElement, env);
			}
		}
		rootElement.appendChild(attributeElement);


	}

	private static void addAttributes(String elementType, Collection<Attribute> attributes,
			Document outputDoc, Element rootElement, EAEIEnvironmentData env) {
		Attribute currentAttributee = null;
		Element attributesElement	= outputDoc.createElement(getTagName(elementType, env));

		Iterator<Attribute> itrElements = attributes.iterator();

		while (itrElements.hasNext()) {
			currentAttributee = itrElements.next();
			addAttribureEx (getElementTypeName(elementType + "." + ATTRIBUTE, env), currentAttributee, outputDoc, attributesElement, env);
		}


		rootElement.appendChild(attributesElement);

	}


	private static String getTagName(String elementType, EAEIEnvironmentData env) {
		String key = elementType.toLowerCase().trim() + ".swap";
		String ret = env.getProperty(key, elementType.toLowerCase().trim());
		env.logger.log(Level.FINEST, "Swap - key  " + key.toLowerCase() + ": " + ret);


		env.getExportFullConfiguration().add((Object)(key + " = " + ret) + "\r\n");

		return ret;

	}




	private static Document getOutputDOMDocument() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dBuilder.newDocument();
	}


	/**
	 * 
	 * @param doc - an empty document
	 * @return Element - the EAEI root element 
	 */
	private static Element appendRootEAEIElement(Document doc) {
		org.w3c.dom.Element rootElement = doc.createElement("EAEI".toLowerCase());		

		Attr attr = null;

		// set the root element attributes

		attr = doc.createAttribute("type");
		attr.setValue("output");
		rootElement.setAttributeNode(attr);

		//append root element to document
		doc.appendChild(rootElement);

		return rootElement;
	}
	/*
	 * Adding all the custom keys to support YED and specificaly YED for YOTAM 
	 */
	private static void addKeysToRootElement(Element rootElement, Document outputDoc) {
		Element keyElement = null;

		addYFilesKeyElement("graphml", "d0", "resources", rootElement, outputDoc);
		addYFilesKeyElement("port", "d1", "portgraphics", rootElement, outputDoc);
		addYFilesKeyElement("port", "d2", "portgeometry", rootElement, outputDoc);
		addYFilesKeyElement("port", "d3", "portuserdata", rootElement, outputDoc);

		addAttributeKeyElement("node", "d4", "shortname", "string", rootElement, outputDoc);
		addAttributeKeyElement("node", "d5", "longname", "string", rootElement, outputDoc);
		addAttributeKeyElement("node", "d6", "video", "string", rootElement, outputDoc);
		addAttributeKeyElement("node", "d7", "url", "string", rootElement, outputDoc);
		addAttributeKeyElement("node", "d8", "description", "string", rootElement, outputDoc);

		addYFilesKeyElement("node", "d9", "nodegraphics", rootElement, outputDoc);

		addAttributeKeyElement("graph", "d10", "Description", "string", rootElement, outputDoc);
		addAttributeKeyElement("edge", "d11", "url", "string", rootElement, outputDoc);
		addAttributeKeyElement("edge", "d12", "description", "string", rootElement, outputDoc);

		addYFilesKeyElement("edge", "d13", "portuserdata", rootElement, outputDoc);


	}

	private static void addYFilesKeyElement (String sFor, String sId, String sType, Element rootElement, Document outputDoc) {
		Element keyElement = outputDoc.createElement("key");
		keyElement.setAttribute("for", sFor);
		keyElement.setAttribute("id", sId);
		keyElement.setAttribute("yfiles.type", sType);
		rootElement.appendChild(keyElement);
	}

	private static void addAttributeKeyElement (String sFor, String sId, String sName, String sType, Element rootElement, Document outputDoc) {
		Element keyElement = outputDoc.createElement("key");
		keyElement.setAttribute("for", sFor);
		keyElement.setAttribute("id", sId);
		keyElement.setAttribute("attr.sName",sName);
		keyElement.setAttribute("attr.type",sType);
		rootElement.appendChild(keyElement);
	}

	private static void printOpenMessage() {
		System.out.println("Enterprise Architect Import and Export utility (EAEI)");
		System.out.println("Version 0.01");
		System.out.println("Auther: Shai Gotlib (shai.gotlib@gmail.com)");
		System.out.println("");
	}




	private static String getElementTypeName(String name, EAEIEnvironmentData env) {
		return name.toLowerCase().replaceAll(" ", "").trim();
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


	

	public static void printUsageHelpMessage(EAEIEnvironmentData env) {
		System.out.println("");
		System.out.println("");
		System.out.println("Enterprise Architect Export Import");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("");
		System.out.println("Expor usage:");
		System.out.println("java –d32 - -Djava.ext.dirs=<extensions directory> -jar eaei.jar " + EAEIEnvironmentData.TASK + " " + EAEIEnvironmentData.TASK_EXPORT + " " + EAEIEnvironmentData.FLAG_GUID + " <package GUID> " + EAEIEnvironmentData.FLAG_REPOSITORY_FILE_NAME + " <repository file name>  [options]");
		System.out.println("");
		System.out.println("Options:");
		System.out.println(SaveResultsFormatterInterface.FLAG_OUTPUT_DIR + " <output directory name> (Defalt: " + SaveResultsFormatterInterface.DEFAULT_OUTPUT_DIR + ")"); 
		System.out.println(EAEIEnvironmentData.FLAG_EXPORT_PROPERTIES_FILE_NAME + " <input properties file name> (Default: no - based on: " + EAEIEnvironmentData.DEFAULT_EXPORT_PROPERTIES_FILE_NAME + ")");
		System.out.println(EAEIEnvironmentData.FLAG_EXPORT_REFERENCE_ELEMENTS + " <yes | no> export reference objects (Default: " + EAEIEnvironmentData.DEFAULT_EXPORT_REFERENCE_ELEMENTS + ")");
		System.out.println(EAEIEnvironmentData.FLAG_SAVE_FULL_CONFIGURATION + " <yes | no> save full configuration properties file (Default: " + EAEIEnvironmentData.DEFAULT_SAVE_FULL_CONFIGURATION + ")");
		System.out.println(EAEIEnvironmentData.FLAG_CONFIG_OUTPUT_FILE_NAME + " <configuration properties output file name> (Default: " + EAEIEnvironmentData.DEFAULT_CONFIG_OUTPUT_FILE_NAME + ")");
		

		Iterator<SaveResultsFormatterInterface> serviceProviders =  env.getSaveResultsServiceProviders();
		
		SaveResultsFormatterInterface serviceProvider = null;
		while (serviceProviders.hasNext()) {
			serviceProvider = (SaveResultsFormatterInterface)serviceProviders.next();
			System.out.println(serviceProvider.getCLParamaters());
		}

		
		System.out.println("");
		System.out.println("");
		System.out.println("Import usage:");
		System.out.println("java –d32 - -Djava.ext.dirs=<extensions directory> -jar eaei.jar " + EAEIEnvironmentData.TASK + " " + EAEIEnvironmentData.TASK_IMPORT + " " + EAEIEnvironmentData.FLAG_REPOSITORY_FILE_NAME + " <repository file name> [options] "); 
		System.out.println("");
		System.out.println("Options:");
		System.out.println(EAEIXMLDOMLoaderInterface.FLAG_INPUT_DIR + " <output directory name> (Defalt: " + EAEIXMLDOMLoaderInterface.DEFAULT_INPUT_DIR + ")"); 
		System.out.println(EAEIEnvironmentData.FLAG_EXPORT_PROPERTIES_FILE_NAME + " <input properties file name> (Default: no - based on: " + EAEIEnvironmentData.DEFAULT_IMPORT_PROPERTIES_FILE_NAME + ")");

		Iterator<EAEIXMLDOMLoaderInterface> serviceProvidersImport =  env.getDOMLoaderServiceProviders();
		
		EAEIXMLDOMLoaderInterface serviceProviderImport = null;
		while (serviceProvidersImport.hasNext()) {
			serviceProviderImport = (EAEIXMLDOMLoaderInterface)serviceProvidersImport.next();
			System.out.println(serviceProviderImport.getCLParamaters());
		}

		
	}
	

	
	

}










