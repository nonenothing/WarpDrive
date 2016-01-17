package cr0s.warpdrive.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XmlRepresentable {
	public String getName();
	
	public void loadFromXmlElement(Element element) throws InvalidXmlException;
	
	public void saveToXmlElement(Element element, Document document) throws InvalidXmlException;
}
