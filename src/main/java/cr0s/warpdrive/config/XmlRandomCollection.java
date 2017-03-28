package cr0s.warpdrive.config;

import org.w3c.dom.Element;

/**
 * Collection of elements with ratios and weights. Helps to select element with controlled odds.
 * 
 * @author ncrashed, LemADEC
 *
 * @param <E>
 **/
public class XmlRandomCollection<E extends IXmlRepresentable> extends RandomCollection<E> {
	
	/**
	 * Loads object from given XML element and parses configurations for weighted pick.
	 * 
	 * @param object
	 *            Object to load into
	 * @param element
	 *            Element of an XML file
	 * @throws InvalidXmlException
	 **/
	public void loadFromXML(E object, Element element) throws InvalidXmlException {
		if (!object.loadFromXmlElement(element)) {// skip invalid entries
			return;
		}
		
		String stringRatio = element.getAttribute("ratio");
		String stringWeight = element.getAttribute("weight");
		
		add(object, stringRatio, stringWeight);
	}
}