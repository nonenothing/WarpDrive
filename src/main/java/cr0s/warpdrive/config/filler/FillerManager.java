package cr0s.warpdrive.config.filler;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.XmlRandomCollection;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlFileManager;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class FillerManager extends XmlFileManager {
	
	private static FillerManager INSTANCE = new FillerManager();
	
	// all fillerSets
	private static HashMap<String, XmlRandomCollection<FillerSet>> fillerSetsByGroup;
	
	public static void load(File dir) {
		fillerSetsByGroup = new HashMap<>();
		INSTANCE.load(dir, "filler", "fillerSet");
		
		propagateFillerSets();
	}
	
	@Override
	protected void parseRootElement(final String location, Element elementFillerSet) throws InvalidXmlException, SAXException, IOException {
		String group = elementFillerSet.getAttribute("group");
		if (group.isEmpty()) {
			throw new InvalidXmlException("FillerSet " + location + " is missing a group attribute!");
		}
		
		String name = elementFillerSet.getAttribute("name");
		if (name.isEmpty()) {
			throw new InvalidXmlException("FillerSet " + location + " is missing a name attribute!");
		}
		
		if (WarpDriveConfig.LOGGING_WORLD_GENERATION) {
			WarpDrive.logger.info("- found FillerSet " + group + ":" + name);
		}
		
		XmlRandomCollection<FillerSet> xmlRandomCollection = fillerSetsByGroup.computeIfAbsent(group, k -> new XmlRandomCollection<>());
		
		FillerSet fillerSet = xmlRandomCollection.getNamedEntry(name);
		if (fillerSet == null) {
			fillerSet = new FillerSet(group, name);
		}
		xmlRandomCollection.loadFromXML(fillerSet, elementFillerSet);
	}
	
	@SuppressWarnings("Convert2Diamond")
	private static void propagateFillerSets() {
		HashMap<FillerSet, ArrayList<String>> fillerSetsDependencies = new HashMap<>();
		
		// scan for static import dependencies
		for (XmlRandomCollection<FillerSet> fillerSets : fillerSetsByGroup.values()) {
			for (FillerSet fillerSet : fillerSets.elements()) {
				ArrayList<String> dependencies = fillerSetsDependencies.computeIfAbsent(fillerSet, k -> new ArrayList<>());
				dependencies.addAll(fillerSet.getImportGroupNames());
			}
		}
		
		// resolve
		int iterationCount = 0;
		while (!fillerSetsDependencies.isEmpty() && iterationCount++ < 10) {
			HashMap<FillerSet, ArrayList<String>> fillerSetsLeftToImport = new HashMap<>();
			
			for (Entry<FillerSet, ArrayList<String>> entry : fillerSetsDependencies.entrySet()) {
				ArrayList<String> newDependencies = new ArrayList<>();
				for (String dependency : entry.getValue()) {
					FillerSet fillerSet = getFillerSet(dependency);
					if (fillerSet == null) {
						WarpDrive.logger.error("Ignoring missing FillerSet " + dependency + " dependency in FillerSet " + entry.getKey());
						
					} else if (fillerSetsDependencies.containsKey(fillerSet)) {
						// skip until it is loaded
						newDependencies.add(dependency);
						
					} else {
						try {
							if (WarpDriveConfig.LOGGING_WORLD_GENERATION) {
								WarpDrive.logger.info("Importing FillerSet " + fillerSet.getFullName() + " in " + entry.getKey().getFullName());
							}
							entry.getKey().loadFrom(fillerSet);
						} catch (InvalidXmlException exception) {
							exception.printStackTrace();
							WarpDrive.logger.error("While importing " + dependency + " into FillerSet " + entry.getKey().getFullName());
						}
					}
				}
				if (!newDependencies.isEmpty()) {
					fillerSetsLeftToImport.put(entry.getKey(), newDependencies);
				}
			}
			
			fillerSetsDependencies = fillerSetsLeftToImport;
		}
		
		// recursion has reach the limit?
		if (!fillerSetsDependencies.isEmpty()) {
			WarpDrive.logger.error("Too many import recursions, ignoring the remaining ones:");
			for (Entry<FillerSet, ArrayList<String>> entry : fillerSetsDependencies.entrySet()) {
				WarpDrive.logger.warn("- FillerSet " + entry.getKey() + " is pending:");
				for (String dependency : entry.getValue()) {
					WarpDrive.logger.warn(" + " + dependency);
				}
			}
		}
	}
	
	public static boolean doesGroupExist(final String groupName) {
		return fillerSetsByGroup.get(groupName) != null;
	}
	
	public static FillerSet getRandomFillerSetFromGroup(Random random, final String groupName) {
		XmlRandomCollection<FillerSet> group = fillerSetsByGroup.get(groupName);
		if (group == null) {
			return null;
		}
		return group.getRandomEntry(random);
	}
	
	public static FillerSet getFillerSet(final String groupAndName) {
		String[] parts = groupAndName.split(":");
		if (parts.length != 2) {
			WarpDrive.logger.error("Invalid FillerSet '" + groupAndName + "'. Expecting '{group}:{name}'");
			return null;
		}
		XmlRandomCollection<FillerSet> group = fillerSetsByGroup.get(parts[0]);
		if (group == null) {
			return null;
		}
		return group.getNamedEntry(parts[1]);
	}
}
