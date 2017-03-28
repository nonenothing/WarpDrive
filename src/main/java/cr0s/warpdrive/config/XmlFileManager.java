package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class XmlFileManager {
	
	protected void load(File dir, final String prefixFilename, final String elementName) {
		
		// (directory is created by caller, so it can copy default files if any)
		
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a directory!");
		}
		
		File[] files = dir.listFiles((file_notUsed, name) -> name.startsWith(prefixFilename) && name.endsWith(".xml"));
		if (files == null || files.length == 0) {
			throw new IllegalArgumentException("File path " + dir.getPath() + " contains no " + prefixFilename + "*.xml files!");
		}
		
		for (File file : files) {
			try {
				WarpDrive.logger.info("Loading configuration file " + file.getName());
				Document document = WarpDriveConfig.getXmlDocumentBuilder().parse(file);
				
				// pre-process the file
				String result = XmlPreprocessor.checkModRequirements(document.getDocumentElement());
				if (!result.isEmpty()) {
					WarpDrive.logger.info("Skipping configuration file " + file.getName() + " due to " + result);
					return;
				}
				
				XmlPreprocessor.doModReqSanitation(document);
				XmlPreprocessor.doLogicPreprocessing(document);
				
				// only add selected root elements
				List<Element> listElements = getChildrenElementByTagName(document.getDocumentElement(), elementName);
				for (int indexElement = 0; indexElement < listElements.size(); indexElement++) {
					Element element = listElements.get(indexElement);
					String location = String.format("%d/%d", indexElement + 1, listElements.size());
					parseRootElement(location, element);
				}
			} catch (Exception exception) {
				WarpDrive.logger.error("Error loading file " + file.getName() + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}
	}
	
	protected abstract void parseRootElement(final String location, Element elementCelestialObject) throws InvalidXmlException, SAXException, IOException;
	
	public static List<Element> getChildrenElementByTagName(final Element parent, final String name) {
		List<Element> listElements = new ArrayList<>();
		
		for (Node nodeChild = parent.getFirstChild(); nodeChild != null; nodeChild = nodeChild.getNextSibling()) {
			if ( nodeChild.getNodeType() == Node.ELEMENT_NODE
			     && name.equals(nodeChild.getNodeName()) ) {
				listElements.add((Element) nodeChild);
			}
		}
		
		return listElements;
	}
}
