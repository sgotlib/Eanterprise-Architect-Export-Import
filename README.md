# Eanterprise-Architect-Export-Import
Export and import of EA package/s as easy to understand and manipulate XML 

> Intoduction
The projet is compond of an engien (the eaei-app) and two types of plugins:
* Plugin that based on the EAEIXMLDOMLoaderInterface - Load and construt the EAEI DOM object for feeding the eaei application. The eaei uses this plugin for updating an EA UML model - **import** (The LoadXMLToDOMObject pluin load XML file into DOM object)
* Plugin that based on the SaveResultsFormatterInterface - this plugins get as input DOM object and save it - ** export ** (Te DOMtoXMLFormatter plugin save the DOM object as eaei XML while the DOMtoJSONFormatter plugin save the DOM object as JSON stream.

