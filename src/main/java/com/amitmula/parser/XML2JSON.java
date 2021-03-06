package com.amitmula.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;



//import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML2JSON {

	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
	private String XMLfilename = "awm-service-metadata.xml"; // should be placed in the resources source folder
	private String JSONfilename = "target/awm-service-metadata.json";
	private URL url = null;
	private InputStream inputStream = null;
	private String xmlString = null;

	private String expression = "//EntityType";

	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;

	public String getXMLfromFile() {
		try {
			url = XML2JSON.class.getClassLoader().getResource(XMLfilename);
			inputStream = url.openStream();

			builder = builderFactory.newDocumentBuilder();

			Document document = builder.parse(inputStream);

			XPath xPath = XPathFactory.newInstance().newXPath();

			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

			Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = newXmlDocument.createElement("root");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Node copyNode = newXmlDocument.importNode(node, true);
				root.appendChild(copyNode);

				// System.out.println(nodeList.item(i).getAttributes().getNamedItem("Name").getNodeValue());
			}

			newXmlDocument.appendChild(root);

			DOMSource domSource = new DOMSource(newXmlDocument);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			// xmlString = IOUtils.toString(inputStream);
			xmlString = writer.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				url = null;
			} catch (IOException ex) {
			}
		}
		return xmlString;
	}

	public void writeJSONFile(String JSONstring) {
		FileWriter output = null;
		try {
			output = new FileWriter(JSONfilename);
			BufferedWriter writer = new BufferedWriter(output);
			writer.write(JSONstring);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// Ignore issues during closing
				}
			}
		}

	}

	public static void main(String[] args) {

		XML2JSON parser = new XML2JSON();
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(parser.getXMLfromFile());
			String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
			parser.writeJSONFile(jsonPrettyPrintString);
			System.out.println(jsonPrettyPrintString);
		} catch (JSONException je) {
			System.out.println(je.toString());
		}
	}

}
