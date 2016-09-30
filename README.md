# Eanterprise-Architect-Export-Import
Export and import of EA package/s as easy to understand and manipulate XML 

# Introduction
The project is compound of an engien (the eaei-app) and two types of plugins:
* Plugin that based on the EAEIXMLDOMLoaderInterface - Load and construct the EAEI DOM object for feeding the eaei application. The eaei uses this plugin for updating an EA UML model - **import** (e.g. the LoadXMLToDOMObject plugin load XML file into DOM object).
* Plugin that based on the SaveResultsFormatterInterface - this plugin get as input DOM object and save it - **export** (e.g. the DOMtoXMLFormatter plugin save the DOM object as eaei XML while the DOMtoJSONFormatter plugin save the DOM object as JSON stream.
## 


