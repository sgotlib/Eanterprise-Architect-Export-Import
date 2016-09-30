package com.gotlib.eaei.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sparx.Collection;
import org.sparx.Repository;
import org.sparx.TaggedValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.gotlib.eaei.app.EAEI;
import com.gotlib.eaei.exceptions.CannotOpenRepositoryException;
import com.gotlib.eaei.spi.EAEIXMLDOMLoaderInterface;
import com.gotlib.eaei.spi.ParamatersManager;
import com.gotlib.eaei.spi.SaveResultsFormatterInterface;

public class EAEIEnvironmentData {
	
	
	public static final String FLAG_GUID 					= 	"-guid";
	public static final String DEFAULT_GUID 				= 	null;
	
	public static final String FLAG_REPOSITORY_FILE_NAME 	= 	"-rfn";
	public static final String DEFAULT_REPOSITORY_FILE_NAME = 	null;


	public static final String FLAG_INPUT_DIR 				= 	"-id";
	public static final String DEFAULT_INPUT_DIR 			=	"eaeiInputDir";
	
	public static final String FLAG_EXPORT_PROPERTIES_FILE_NAME 	= 	"-epfn";
	public static final String DEFAULT_EXPORT_PROPERTIES_FILE_NAME 	=	"/defaultExport.properties";

	public static final String FLAG_IMPORT_PROPERTIES_FILE_NAME 	= 	"-ipfn";
	public static final String DEFAULT_IMPORT_PROPERTIES_FILE_NAME 	=	"/defaultImport.properties";
	
	public static final String FLAG_EXPORT_REFERENCE_ELEMENTS 		= 	"-ere";
	public static final String DEFAULT_EXPORT_REFERENCE_ELEMENTS 	=	"yes";

	public static final String FLAG_SAVE_FULL_CONFIGURATION 		= 	"-sfc";
	public static final String DEFAULT_SAVE_FULL_CONFIGURATION	 	=	"yes";

	
	public static final String FLAG_CONFIG_OUTPUT_FILE_NAME 	= 	"-cpfn";
	public static final String DEFAULT_CONFIG_OUTPUT_FILE_NAME 	=	"configfExportFile.properties";
	
	
	
	public static final String TASK 				= "-task";
	public static final String TASK_EXPORT 		= "export";
	public static final String TASK_IMPORT 		= "import";
	public static final String TASK_UNKNOWN		= "unknown";
	

	
	public java.util.logging.Logger logger = null;
	
	
	

	private LaunchingConfiguration 	cnfg 						= null;
	private Properties 				properties					= null;;
	private Repository 				repository					= null;
	private LinkedHashSet 			exportConfigurationInAction = null;
	private Hashtable <String, Collection<TaggedValue>> packageToMetaData = new Hashtable <String, Collection<TaggedValue>>();
	private Hashtable <Integer, org.sparx.Element> referenceElementsTable = new  Hashtable<>();
	private ServiceLoader<SaveResultsFormatterInterface> serviceProvidersSaveResults = null;
	private ServiceLoader<EAEIXMLDOMLoaderInterface> serviceProvidersDOMLoader = null;
	

	
	public void init(LaunchingConfiguration cnfg) throws CannotOpenRepositoryException {
		
		logger = java.util.logging.Logger.getLogger(EAEI.class.getName());
		logger.log(Level.INFO, "Environment initialization... "); 
		
		this.cnfg  = cnfg; // init launching parameters

		
		exportConfigurationInAction = new LinkedHashSet(); 
		
		serviceProvidersSaveResults = ServiceLoader.load(SaveResultsFormatterInterface.class);
		serviceProvidersDOMLoader 	= ServiceLoader.load(EAEIXMLDOMLoaderInterface.class);

		properties = new Properties();
		
		//
		// Open EA repository
		//
		String repositoryFileName = cnfg.getStringParam(FLAG_REPOSITORY_FILE_NAME, DEFAULT_REPOSITORY_FILE_NAME);
		if (repositoryFileName  == null) {
			logger.log(Level.SEVERE, "Repository file name is mandatory!");
			return;
		}
		
		logger.log(Level.INFO, "Open repository: " + repositoryFileName); 
		repository = getOpenRepository(repositoryFileName);
		
		
		logger.log(Level.INFO, "Environment was initialized.");

	}
	
	/*
	 * Return open EA Repository object
	 */

	private static Repository getOpenRepository(String repositoryFileName) throws CannotOpenRepositoryException {
		Repository rep = null;

		// Create a repository object - This will create a new instance of EA
		rep = new Repository();

		if (rep.OpenFile(repositoryFileName)) return rep;
		throw new CannotOpenRepositoryException ("Cannot open repositiry: " + repositoryFileName);
	}
	
	
	/*
	 * Load the default properties and on top of it the launching input file 
	 */
	
	private Properties loadProperties(String propertiesFileName) {
		InputStream in;

		try {
			in = this.getClass().getResourceAsStream(propertiesFileName);
			if (in == null) {
				Path file = Paths.get(propertiesFileName);
				in = Files.newInputStream(file);
			}
			if (in != null) {
				Properties newProperties = new Properties();
				newProperties.load(in);
				properties.putAll(newProperties);
				in.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return properties;
	}
	
	public void loadExportProperties() {
		
		loadProperties(DEFAULT_EXPORT_PROPERTIES_FILE_NAME);										// Load the default properties file
		String fileName = cnfg.getStringParam(FLAG_EXPORT_PROPERTIES_FILE_NAME, null);
		if (fileName != null) {
			loadProperties(fileName);														// Load the properties file that insert as paramater
		}
	}
	public void loadImportProperties() {

		loadProperties(DEFAULT_IMPORT_PROPERTIES_FILE_NAME);										// Load the default properties file
		String fileName = cnfg.getStringParam(FLAG_IMPORT_PROPERTIES_FILE_NAME, null);
		if (fileName != null) {
			loadProperties(fileName);														// Load the properties file that insert as paramater
		}
	}
		
		


	
	
	public Repository getRepository() {
		return repository;
	}
	public Properties getProperties() {
		return properties;
	}
	public LinkedHashSet getExportFullConfiguration() {
		return exportConfigurationInAction;
	}

	public ParamatersManager getParamatersManager() {
		return cnfg;
	}


	public Enumeration<Object> getPropertiesKeys() {
		return properties.keys();
	}
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	
	
	public String getGUID() {
		return cnfg.getStringParam(FLAG_GUID, DEFAULT_GUID);
	}


	public boolean isExport() {
		return cnfg.is(TASK, TASK_EXPORT);
	}
	public boolean isImport() {
		return cnfg.is(TASK, TASK_IMPORT);
	}	
	public boolean isUnknown() {
		return cnfg.is(TASK, TASK_UNKNOWN);
	}	

	public boolean shouldSaveFullConfig() {
		return cnfg.is(FLAG_SAVE_FULL_CONFIGURATION, DEFAULT_SAVE_FULL_CONFIGURATION);
	}
	
	public boolean shouldReferencesBeAdded() {
		return cnfg.is(FLAG_IMPORT_PROPERTIES_FILE_NAME, DEFAULT_IMPORT_PROPERTIES_FILE_NAME);
	}
	
	public String getTask() {
		return cnfg.getStringParam(TASK, TASK_UNKNOWN);
	}

	public String getInputDir() {
		return cnfg.getStringParam(FLAG_INPUT_DIR, DEFAULT_INPUT_DIR);
	}

	
	public String getOutputDir() {
		return cnfg.getStringParam(SaveResultsFormatterInterface.FLAG_OUTPUT_DIR, SaveResultsFormatterInterface.DEFAULT_OUTPUT_DIR);
	}


	public String getConfigurationOutputFileName() {
		return cnfg.getStringParam(FLAG_CONFIG_OUTPUT_FILE_NAME, DEFAULT_CONFIG_OUTPUT_FILE_NAME);
	}


	public Hashtable <Integer, org.sparx.Element> getReferenceElementsTable() {
		return referenceElementsTable;
	}
	
	public Hashtable <String, Collection<TaggedValue>> getPackageToMetaData() {
		return packageToMetaData;
	}


	public void closeRepository() {
		if ( getRepository() != null )
		{
			// Clean up
			getRepository().CloseFile();
			getRepository().Exit();
			getRepository().destroy();
		}
		
	}

	
	public void destroy () {
		closeRepository();
	}

	public Iterator<SaveResultsFormatterInterface> getSaveResultsServiceProviders() {
		return serviceProvidersSaveResults.iterator();
	}
	public Iterator<EAEIXMLDOMLoaderInterface> getDOMLoaderServiceProviders() {
		return serviceProvidersDOMLoader.iterator();
	}

	public EAEIXMLDOMLoaderInterface getImportDocumentSP() {
		Iterator<EAEIXMLDOMLoaderInterface> it = serviceProvidersDOMLoader.iterator();
		
		if (it.hasNext()) {
			EAEIXMLDOMLoaderInterface ret = it.next(); 
			if (it.hasNext()) {
				logger.log(Level.SEVERE, "More than one Service Providers Found (EAEIXMLDOMLoaderInterface service provider) were found! The Service Provider that will be used: "  + ret.getDOMLoaderName());
			}
			return ret;
		}
		
		
		logger.log(Level.SEVERE, "No EAEIXMLDOMLoaderInterface service provider was found - Cannot process the import request!");
		
		

		return null;
	}

}

