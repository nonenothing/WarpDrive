package cr0s.warpdrive.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IXmlRepresentable {
	String getName();
	
	// Load the XML element, return true if successful
	boolean loadFromXmlElement(Element element) throws InvalidXmlException;
	
	void saveToXmlElement(Element element, Document document) throws InvalidXmlException;
}
