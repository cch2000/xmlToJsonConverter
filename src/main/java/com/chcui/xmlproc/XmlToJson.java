/*
 * This class implements the main functionality of xml to json converter
 * @author	Chrissie Cui
 * @version	1.0
 * @date	2013/12/19
 */
package com.chcui.xmlproc;

import java.util.ArrayDeque;
import java.util.Queue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlToJson implements Runnable {

	private String filePath = null;
	private PrintWriter output = null;
	/**
	 * @param args
	 */
	//public methods
	/*
	 * Class constructor
	 */
	public XmlToJson (String dir) {
		filePath = dir;
	}
	public void setFilePath(String dir) {
		filePath = dir;
	}
	 public synchronized void run() {
		 try {
				convert();
		 } catch (IOException e) {
				e.printStackTrace();
		}
		
	}
	/*
	 * This function operate converting a xml file to a json file
	 * The new json file will use the same file name as the xml file
	 * except using json as an extension name
	 */
	public void convert() throws IOException {
		File outFile = new File(filePath.replace("xml", "json"));
	    try {
	    	File file = new File(filePath);
	    	output = new PrintWriter(
	    					new BufferedWriter(new FileWriter(outFile.getCanonicalPath()))
	    				);
	    	
	    	DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
	                                 .newDocumentBuilder();
	     
	    	Document doc = dBuilder.parse(file);
	    	output.println("{");
	    	printAttributes(doc.getDocumentElement(), 1);
	    	printNode(doc.getDocumentElement().getChildNodes(), 1);
	    	output.println("}");
	    	output.close();
	        } catch (Exception e) {
	    	System.out.println(e.getMessage());
	    	output.close();
	    	outFile.deleteOnExit();
	        };
	    
	}
	// private methods
	/*
	 * @param	indentLevel	the indentation level for each line of 
	 * 						the elements to be printed
	 */
	private void printIndent(int indentLevel) {
		String indentation="";
		for (int i = 0; i < indentLevel; i++){
			indentation += "    ";
		}
		output.print(indentation);
	} 
	/*
	 * @param	nodeList 	the list of element nodes to be printed
	 * @param	indentLevel the indentation level for each line of 
	 * 						the elements to be printed
	 */
	private void printNode(NodeList nodeList, int indentLevel){
		if (nodeList.getLength() == 1)
			printSingle(nodeList.item(0), false, indentLevel);
		else
			printMultiple(nodeList, indentLevel);
	}
	/*
	 * @param	input		the node for which the attributes to be printed
	 * @param	indentLevel	the indentation level for each line of
	 * 						the elements to be printed
	 */
	private void printAttributes(Node input, int indentLevel){
		if (input.hasAttributes()) {
			 
			// get attributes names and values
			NamedNodeMap nodeMap = input.getAttributes();
 
			for (int i = 0; i < nodeMap.getLength(); i++) {
 
				Node node = nodeMap.item(i);
				// skip name space property
				if ("xmlns".equals(node.getNodeName()))
					continue;
				printIndent(indentLevel);
				output.println("\"" + 
								node.getNodeName() + 
								"\": \"" + 
								node.getNodeValue() 
								+ "\"");
			}
 
		}	
	}
	/*
	 * @param	input		the node to be printed
	 * @param	isInArray	If the node to be printed is an element 
	 * 						in an array
	 * @param	indentLevel	the indentation level for each line of
	 * 						the elements to be printed
	 */
	private void printSingle(Node input, boolean isInArray, int indentLevel){
		if (input.getNodeType() != Node.ELEMENT_NODE)
			return;
		if (!isInArray) {
			printIndent(indentLevel);
			output.println("\""+ input.getNodeName() + "\": {");
		}
		
		if (input.getChildNodes().getLength() == 1 &&
				input.getChildNodes().item(0).getNodeType() != Node.ELEMENT_NODE){
			printIndent(indentLevel + 1);
			output.println(
					"\"_tag\": \"" + 
					input.getNodeName() + "\",");
			printIndent(indentLevel + 1);
			output.print(
					"\"_text\": \"" + 
					input.getTextContent().replace("\n", "").trim());
			if (input.hasAttributes()) {
					output.println("\",");
					printAttributes(input, indentLevel + 1);
			} else {
				output.println("\"");
			}
		}
		else
		{
			NodeList childNodes = input.getChildNodes();
			printNode(childNodes, indentLevel + 1);				
		}
		if (!isInArray) {
			Node secondLast =
					input.getParentNode().getLastChild().getPreviousSibling();
			if (input != secondLast) {
				printIndent(indentLevel);
				output.println("},");
			}
			else {
				printIndent(indentLevel);
				output.println("}");
			}
		}
	}
	/*
	 * @param	repElements	the queue of elements to be printed
	 * @param	indentLevel	the indentation level for each line of
	 * 						the elements to be printed
	 */
	private void printArray(Queue<Node> repElements, int indentLevel) {
		printIndent(indentLevel);
		output.println("\"" + repElements.peek().getNodeName() + "\": [{");
		int index = 0;
		boolean islast = false;
		for(Node repElement : repElements){
			printSingle(repElement, true, indentLevel);
			printIndent(indentLevel);
			output.print("}");
			++ index;
			if (index < repElements.size()){
				output.println(",{");
			} else {
				Node secondLast =
						repElement.getParentNode().getLastChild().getPreviousSibling();
				if (repElement == secondLast)
					islast = true;
			}
		}
		if (!islast)
			output.println("],");
		else
			output.println("]");
		
	}
	/*
	 * @param	nodeList	the list of nodes to be printed
	 * @param	indentLevel	the indentation level for each line of
	 * 						the elements to be printed
	 */
	 private void printMultiple(NodeList nodeList, int indentLevel) {
		 Queue<Node> repElements = new ArrayDeque<Node>();
		    for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
	
				if (repElements.isEmpty()) {
		    		repElements.add(tempNode);
		    	} else {
		    		if (repElements.peek().getNodeName().equals(tempNode.getNodeName())){
		    			repElements.add(tempNode);
		    		} else {
		    			if( repElements.size() > 1) {
		    				printArray(repElements, indentLevel);
		    			} else {
		    				printSingle(repElements.peek(), false, indentLevel);
		    			}
		    			repElements.clear();
		    			repElements.add(tempNode);
		    		}
		    	}
			}
		}
			if (!repElements.isEmpty()) {
    			if( repElements.size() > 1) {
    				printArray(repElements, indentLevel);
    			} else {
    				printSingle(repElements.peek(), false, indentLevel);
    			}
    			repElements.clear();
			}
		 
		  }

}
