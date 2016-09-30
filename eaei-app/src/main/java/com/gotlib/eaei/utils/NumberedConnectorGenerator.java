package com.gotlib.eaei.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.gotlib.eaei.app.EAEI;

public class NumberedConnectorGenerator extends EAEI {
	private Hashtable<Integer, NumberedConnector> clientToConnectorMapping = null; 
	private Hashtable<Integer, NumberedConnector> supplierToConnectorMapping= null;
	private List<NumberedConnector> theConnectorsList = null;
	
	public NumberedConnectorGenerator() {
		super();
		theConnectorsList = new ArrayList<NumberedConnector>(); 
		clientToConnectorMapping = new Hashtable<Integer, NumberedConnector>(); 
		supplierToConnectorMapping= new Hashtable<Integer, NumberedConnector>();

	}
	
	public Iterator<NumberedConnector> getIterator() {
		return theConnectorsList.listIterator();
	}
	
	public void add (org.sparx.Connector connector) {
		NumberedConnector con = new NumberedConnector(connector);
		clientToConnectorMapping.put(new Integer(con.getConnector().GetClientID()), con);
		supplierToConnectorMapping.put(new Integer(con.getConnector().GetSupplierID()), con);
		theConnectorsList.add(con);
	}
	
	public void generateNumbering() {
		int currentUMLRootNumber 	= 0	;
		int currentEARootNumber 	= 0	;
		int counterEANumber 	= 0	;
		String currentUMLNumber = "";
		String lastUMLNumber 	= "";


		clientToConnectorMapping = new Hashtable<Integer, NumberedConnector>(); 
		supplierToConnectorMapping= new Hashtable<Integer, NumberedConnector>();

		for (int i = 0; i < theConnectorsList.size(); i++) {
			theConnectorsList.get(i).reset();
		}
		
		Collections.sort(theConnectorsList);
		

		for (int i = 0; i < theConnectorsList.size(); i++) {
			NumberedConnector con = theConnectorsList.get(i);
			if (con.getConnector().GetSequenceNo() < 1) continue;
			if (isInitiated(con) || currentEARootNumber == 0) {
				currentEARootNumber++;
				counterEANumber = 0;
				currentUMLRootNumber++;
				lastUMLNumber = currentUMLNumber = Integer.toString(currentUMLRootNumber);
			} else {
				counterEANumber++;
				
				NumberedConnector client 	= clientToConnectorMapping.get(new Integer(con.getConnector().GetClientID()));
				NumberedConnector sup 		= supplierToConnectorMapping.get(new Integer(con.getConnector().GetClientID()));
				
				if (client  == null) {
					if (sup == null) {
						lastUMLNumber = currentUMLNumber = Integer.toString(++currentUMLRootNumber);
					} else {
						lastUMLNumber = currentUMLNumber =  sup.getUMLNumbering() + ".1";
					}
					
				} else { // client != null
					if (sup == null) {
						lastUMLNumber = currentUMLNumber = nextNumber(client.getUMLNumbering());
					} else {
						if (isIncOrder(client.getUMLNumbering(), sup.getUMLNumbering())) {
							lastUMLNumber = currentUMLNumber = nextNumber(sup.getUMLNumbering());
						} else {
							lastUMLNumber = currentUMLNumber = nextNumber(client.getUMLNumbering());
						}
					}
					
				}
			}
			con.setEANumbering(currentEARootNumber  + "." + counterEANumber);
			con.setUMLNumbering(currentUMLNumber);
			
			
			//
			// Add the connection tp be the last in the client and supplier
			//
			clientToConnectorMapping.put(new Integer(con.getConnector().GetClientID()), con);
			supplierToConnectorMapping.put(new Integer(con.getConnector().GetSupplierID()), con);

		}
		
		
	}

	private boolean isIncOrder(String umLlNumbering1, String umLlNumbering2) {
		
		for (int i = 0; i < umLlNumbering1.length(); i++) {
			if (i >= umLlNumbering2.length()) return false;
			if (umLlNumbering1.charAt(i) < umLlNumbering2.charAt(i)) return true;
			if (umLlNumbering1.charAt(i) > umLlNumbering2.charAt(i)) return false; 
		}
		return true;
	}

	private String nextNumber(String number) {
		String ret = "";
		String prefix = "";
		int index = number.lastIndexOf(".");
		
		if (index < 0) {
			ret = Integer.valueOf(number).toString() + ".1";
		} else {
			prefix = number.substring(0, index+1);
			ret = prefix +  ((new Integer (number.substring(index+1)).intValue()+1));
			
		}
		
		return ret;
	}

	private boolean isInitiated(NumberedConnector numberedConnector) {
		
		String state_flags = numberedConnector.getConnector().GetStateFlags();
		
		return state_flags.contains("Initiate=1");
		
	}
	

}