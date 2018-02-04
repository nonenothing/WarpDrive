package cr0s.warpdrive.config.structures;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.XmlRandomCollection;
import cr0s.warpdrive.config.XmlFileManager;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class StructureManager extends XmlFileManager {
	
	private static StructureManager INSTANCE = new StructureManager();
	
	public static final String GROUP_STARS = "star";
	public static final String GROUP_MOONS = "moon";
	public static final String GROUP_GAS_CLOUDS = "gascloud";
	public static final String GROUP_ASTEROIDS = "asteroid";
	public static final String GROUP_ASTEROIDS_FIELDS = "asteroids_field";
	
	private static HashMap<String, XmlRandomCollection<AbstractStructure>> structuresByGroup;
	
	private static final String[] REQUIRED_GROUPS = { GROUP_STARS, GROUP_MOONS, GROUP_GAS_CLOUDS, GROUP_ASTEROIDS, GROUP_ASTEROIDS_FIELDS };
	
	public static void load(File dir) {
		structuresByGroup = new HashMap<>();
		INSTANCE.load(dir, "structure", "structure");
		
		for (final String group : REQUIRED_GROUPS) {
			if (!structuresByGroup.containsKey(group)) {
				WarpDrive.logger.error("Error: no structure defined for mandatory group " + group);
			}
		}
	}
	
	@Override
	protected void parseRootElement(final String location, Element elementStructure) throws InvalidXmlException, SAXException, IOException {
		String group = elementStructure.getAttribute("group");
		if (group.isEmpty()) {
			throw new InvalidXmlException(String.format("%s is missing a group attribute!", location));
		}
		
		String name = elementStructure.getAttribute("name");
		if (name.isEmpty()) {
			throw new InvalidXmlException(String.format("%s is missing a name attribute!", location));
		}
		
		WarpDrive.logger.info("- found Structure " + group + ":" + name);
		
		XmlRandomCollection<AbstractStructure> xmlRandomCollection = structuresByGroup.computeIfAbsent(group, k -> new XmlRandomCollection<>());
		
		AbstractStructure abstractStructure = xmlRandomCollection.getNamedEntry(name);
		if (abstractStructure == null) {
			switch (group) {
				case GROUP_STARS:
					abstractStructure = new Star(group, name);
					break;
				case GROUP_MOONS:
					abstractStructure = new Orb(group, name);
					break;
				default:
					abstractStructure = new MetaOrb(group, name);
					break;
			}
		}
		xmlRandomCollection.loadFromXML(abstractStructure, elementStructure);
	}
	
	public static AbstractStructure getStructure(Random random, final String group, final String name) {
		if (group == null || group.isEmpty()) {
			return null;
		}
		
		// @TODO XML configuration for Asteroids Fields
		if (group.equals(GROUP_ASTEROIDS_FIELDS)) {
			return new AsteroidField(null, null);
		}
		
		XmlRandomCollection<AbstractStructure> xmlRandomCollection = structuresByGroup.get(group);
		if (xmlRandomCollection == null) {
			return null;
		}
		
		if (name == null || name.isEmpty()) {
			return xmlRandomCollection.getRandomEntry(random);
		} else {
			return xmlRandomCollection.getNamedEntry(name);
		}
	}
	
	public static String getStructureNames(final String group) {
		if (group != null && !group.isEmpty()) {
			XmlRandomCollection<AbstractStructure> xmlRandomCollection = structuresByGroup.get(group);
			if (xmlRandomCollection != null) {
				return xmlRandomCollection.getNames();
			}
		}
		return "Error: group '" + group + "' isn't defined. Try one of: " + StringUtils.join(structuresByGroup.keySet(), ", ");
	}
}
