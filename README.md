# Eanterprise-Architect-Export-Import
Export and import of EA package/s as easy to understand and manipulate XML 

# Introduction
The project is compound of an engien (the eaei-app) and two types of plugins:
* Plugin that based on the EAEIXMLDOMLoaderInterface - Load and construct the EAEI DOM object for feeding the eaei application. The eaei uses this plugin for updating an EA UML model - **import** (e.g. the LoadXMLToDOMObject plugin load XML file into DOM object).
* Plugin that based on the SaveResultsFormatterInterface - this plugin get as input DOM object and save it - **export** (e.g. the DOMtoXMLFormatter plugin save the DOM object as eaei XML while the DOMtoJSONFormatter plugin save the DOM object as JSON stream.

# Launching the eaei:
## Pre-requisites:
For launching the eaei application and export packages from the Enterprise Architect (EA) to XML or JSON proprietary formats and to import back changes in texts and descriptions you should have:
* JRE 1.8 or higher that supports 32-bit JVM
* EA license and application
* The compiled runnable eaei.jar – the EAEI engine
* The compiled DOMToJSONFormatter.jar – The XML formatter - This jar should be in the "extensions directory"
* The compiled DOMToXMLFormatter.jar – The JSON formatter - This jar should be in the "extensions directory"

## The eaei command line:
### Expor usage:
java –d32 - -Djava.ext.dirs=<extensions directory> -jar eaei.jar -task export -guid <package GUID> -rfn <repository file name>  [options]

Options:
-od <output directory name> (Defalt: out)
-epfn <input properties file name> (Default: no - based on: /defaultExport.properties)
-ere <yes | no> export reference objects (Default: yes)
-sfc <yes | no> save full configuration properties file (Default: yes)
-cpfn <configuration properties output file name> (Default: configfExportFile.properties)
-jfn <output JASON file name> (Defalt: eaeiExportJSONFile)
-xfn <output XML file name> (Defalt: eaeiExportXMLFile.xml)


Import usage:
java –d32 - -Djava.ext.dirs=<extensions directory> -jar eaei.jar -task import -rfn <repository file name> [options] 

Options:
-id <output directory name> (Defalt: in)
-epfn <input properties file name> (Default: no - based on: /defaultImport.properties)
-xfn <output XML file name> (Defalt: eaeiImportXMLFile.xml)

The exporting is controlled by the <input properties file name> file. It determines what will be exported and what will be the names of the type of the entities.
Exporting “regular” package will export the package attributes and its content according to the <input properties file name> file. 
Exporting <<master document>> package will export all the packages that are included in the <<model document>> objects in the <<master document>> package. Each package (that includes in the <<model document>> objects) is exported as “regular” package. The prefix of package that includes in  <<model document>> objects that has TAGGED VALUE with TAG =  “metadata.filter” and value = “value” (for example) will be “package.value”. This mechanism enable to export different set of attributes to different type of packages. 




