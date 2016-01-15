package cr0s.warpdrive.config;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.WarpDrive;


public class XmlPreprocessor {
	
	/**
	 * Will check the given element for a mod attribute and return a string of all the ones that are not loaded, separated by commas
	 *
	 * @param element
	 *            Element to check
	 * @return A string, which is empty if all the mods are loaded.
	 * @throws InvalidXmlException
	 */
	public static ModCheckResults checkModRequirements(Element element) {
		
		ModCheckResults modErrors = new ModCheckResults();
		
		for (String mod : element.getAttribute("mods").split(",")) {
			
			//TODO: add version check
			
			
			if (mod.isEmpty())
				continue;
			
			if (mod.startsWith("!")) {
				
				if (Loader.isModLoaded(mod.substring(1)))
					modErrors.addMod(mod, "loaded");
				
			} else if (!Loader.isModLoaded(mod))
				modErrors.addMod(mod, "not loaded");
			
		}
		
		return modErrors;
	}
	
	/**
	 * Goes through every child node of the given node, and if it is an element and fails checkModRequirements() it is removed
	 *
	 * @param base
	 * @throws InvalidXmlException
	 */
	public static void doModReqSanitation(Node base) {
		
		NodeList children = base.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			if (child instanceof Element) {
				Element elementChild = (Element) child;
				ModCheckResults res = checkModRequirements(elementChild);
				if (!res.isEmpty()) {
					WarpDrive.logger.info("Skipping " + base.getNodeName() + "/" + elementChild.getNodeName()
							+ " " + elementChild.getAttribute("group") + elementChild.getAttribute("name") + elementChild.getAttribute("block")
							+ " due to " + res);
					base.removeChild(child);
				} else {
					doModReqSanitation(child);
				}
			}
		}
	}
	
	public static void doLogicPreprocessing(Node root) throws InvalidXmlException {
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			doLogicPreprocessing(children.item(i));
		}

		if (root.getNodeType() == Node.ELEMENT_NODE && ((Element) root).getTagName().equalsIgnoreCase("for")) {
			
			Element forTag = (Element) root;

			String varName = forTag.getAttribute("variable");
			if(varName.isEmpty())
				throw new InvalidXmlException("A for tag must include a variable attribute!");

			//In supersedes from
			if (forTag.hasAttribute("in")) {
				String inOptions = forTag.getAttribute("in");

				for(String input : inOptions.split(",")) {
					
					NodeList allChildren = root.getChildNodes();
					for(int chI = 0; chI < allChildren.getLength(); chI ++) {
						
						Node copy = getCopyVarReplace(allChildren.item(chI), varName, input);
						root.getParentNode().appendChild(copy);
						
					}
					
				}

			} else {

				String fromStr = forTag.getAttribute("from");
				String toStr = forTag.getAttribute("to");
				
				if (toStr.isEmpty() || fromStr.isEmpty())
					throw new InvalidXmlException("If a for doesnt have an in attr, it must have a from and to!");

				int from, to;
				try {
					from = Integer.parseInt(fromStr);
					to = Integer.parseInt(toStr);
				} catch (NumberFormatException e) {
					throw new InvalidXmlException(e);
				}

				for (; from <= to; from++) {
					
					NodeList allChildren = root.getChildNodes();
					for (int chI = 0; chI < allChildren.getLength(); chI++) {
						
						Node copy = getCopyVarReplace(allChildren.item(chI), varName, "" + from);
						root.getParentNode().appendChild(copy);
						
					}
					
				}

			}
			
			//Remove the old node
			root.getParentNode().removeChild(root);


		}
		
	}

	private static Node getCopyVarReplace(Node toCopy, String varName, String value) {

		Node copy = toCopy.cloneNode(true);
		replaceVar(copy, varName, value);
		
		return copy;
	}

	private static void replaceVar(Node root, String varName, String value) {

		ArrayList<String> toRemove = new ArrayList<String>();
		ArrayList<Attr> toAdd = new ArrayList<Attr>();

		if (root.getNodeType() == Node.ELEMENT_NODE) {
			
			//First replace attributes
			NamedNodeMap attrs = root.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {

				Attr attr = (Attr) attrs.item(i);
				String name = attr.getName();
				String newName = name.replace("%" + varName + "%", value);

				if (name.equals(newName)) {

					//Easy, just adjust value
					attr.setValue(attr.getValue().replace("%" + varName + "%", value));

				} else {

					//The name changed
					toRemove.add(name);

					Attr newAttr = attr.getOwnerDocument().createAttribute(newName);
					newAttr.setValue(attr.getValue().replace("%" + varName + "%", value));
					toAdd.add(newAttr);

				}

			}

			//Now do the adds and removals
			for (String attr : toRemove)
				attrs.removeNamedItem(attr);
			
			for (Attr attr : toAdd)
				attrs.setNamedItem(attr);
		}
		
		//Now that Attributes are done, go through all of the children

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {
			case Node.ELEMENT_NODE://Recurse on the element
				replaceVar(child, varName, value);
				break;
			case Node.TEXT_NODE:
				child.setTextContent(child.getTextContent().replace("%" + varName + "%", value));
				break;

			}
		}

	}
	
	public static class ModCheckResults {
		
		private TreeMap<String, String> mods;
		
		public ModCheckResults() {
			mods = new TreeMap<String, String>();
		}
		
		public void addMod(String name, String error) {
			mods.put(name, error);
		}
		
		public boolean isEmpty() {
			return mods.isEmpty();
		}
		
		@Override
		public String toString() {
			String string = (mods.size() > 1 ? "{" : "");
			boolean isFirst = true;
			for (Entry<String, String> entry : mods.entrySet()) {
				if (isFirst) {
					isFirst = false;
				} else {
					string += ", ";
				}
				string += entry.getKey() + ": " + entry.getValue();
			}
			
			return string + (mods.size() > 1 ? "}" : "");
		}
	}
}
