package com.gotlib.eaei.utils;

public class NumberedConnector implements Comparable<NumberedConnector> {


	
	private static String currentUMLPrefixlNumbering 	= "";
	

	private String thisUMLNumber 	= "";
	private String thisEANumber 	= "";

	public org.sparx.Connector getConnector() {
		return connector;
	}

	public void setUMLNumbering(String currentUMLNumber) {
		thisUMLNumber = currentUMLNumber;
		
	}

	public void setEANumbering(String currentEANumber) {
		thisEANumber = currentEANumber;
		
	}

	public void reset() {
		thisUMLNumber 	= "";
		thisEANumber 	= "";
		
	}

	private org.sparx.Connector connector = null;
	
	public NumberedConnector(org.sparx.Connector connector) {
		super();
		this.connector = connector;
	}

	public String getEANumbering() {
		return thisEANumber;
	}



	public String getUMLNumbering() {
		return thisUMLNumber;
	}
	
	
	@Override
	public int compareTo(NumberedConnector connector) {

		return (new Integer(this.connector.GetSequenceNo())).compareTo(new Integer(connector.getConnector().GetSequenceNo()));
	}

}