package cr0s.warpdrive.config.filler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.RandomCollection;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlPreprocessor;

public class FillerManager {
	
	private static TreeMap<String, FillerSet> fillerSetsByName = new TreeMap<String, FillerSet>();
	private static TreeMap<String, RandomCollection<FillerSet>> fillerSetsByGroup = new TreeMap<String, RandomCollection<FillerSet>>();
	
	// Stores extra dependency information
	static TreeMap<FillerSet, ArrayList<String>> fillerSetsDependencies = new TreeMap<FillerSet, ArrayList<String>>();
	
	/* TODO dead code?
	// FillerSets that are guaranteed to exist
	public static final String COMMON_ORES = "commonOres";
	public static final String UNCOMMON_ORES = "uncommonOres";
	public static final String RARE_ORES = "rareOres";
	public static final String OVERWORLD = "overworld";
	public static final String NETHER = "nether";
	public static final String END = "end";
	/**/
	
	public static void loadOres(File dir) {
		// directory is created by caller, so it can copy default files if any
		
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File path " + dir.getName() + " must be a directory!");
		}
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file_notUsed, String name) {
				return name.startsWith("filler") && name.endsWith(".xml");
			}
		});
		
		for(File file : files) {
			try {
				loadXmlFillerFile(file);
			} catch (Exception exception) {
				WarpDrive.logger.error("Error loading filler data file " + file.getName() + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}
		WarpDrive.logger.info("Loading filler data files done");
	}
	
	private static void loadXmlFillerFile(File file) throws InvalidXmlException, SAXException, IOException {
		WarpDrive.logger.info("Loading filler data file " + file.getName());
		Document document = WarpDriveConfig.getXmlDocumentBuilder().parse(file);
		
		// pre-process the file
		String result = XmlPreprocessor.checkModRequirements(document.getDocumentElement());
		if (!result.isEmpty()) {
			WarpDrive.logger.info("Skipping filler data file " + file.getName() + " due to " + result);
			return;
		}
		
		XmlPreprocessor.doModReqSanitation(document);
		XmlPreprocessor.doLogicPreprocessing(document);
		
		// only add FillerSets
		NodeList nodesFillerSet = document.getElementsByTagName("FillerSet");
		for (int fillerSetIndex = 0; fillerSetIndex < nodesFillerSet.getLength(); fillerSetIndex++) {
			
			Element elementFillerSet = (Element) nodesFillerSet.item(fillerSetIndex);
			
			String group = elementFillerSet.getAttribute("group");
			if (group.isEmpty()) {
				throw new InvalidXmlException("FillerSet " + (fillerSetIndex + 1) + "/" + nodesFillerSet.getLength() + " is missing a group attribute!");
			}
			
			String name = elementFillerSet.getAttribute("name");
			if (name.isEmpty()) {
				throw new InvalidXmlException("FillerSet " + (fillerSetIndex + 1) + "/" + nodesFillerSet.getLength() + " is missing a name attribute!");
			}
			
			WarpDrive.logger.info("- found FillerSet " + group + ":" + name);
			
			FillerSet fillerSet = fillerSetsByName.get(name);
			if (fillerSet == null) {
				fillerSet = new FillerSet(group, name);
				fillerSetsByName.put(name, fillerSet);
			}
			
			RandomCollection randomCollection = fillerSetsByGroup.get(group);
			if (randomCollection == null) {
				randomCollection = new RandomCollection<FillerSet>();
				fillerSetsByGroup.put(group, randomCollection);
			}
			randomCollection.loadFromXML(fillerSet, elementFillerSet);
			
			if (elementFillerSet.hasAttribute("fillerSets")) {
				ArrayList<String> dependencies = fillerSetsDependencies.get(fillerSet);
				if (dependencies == null) {
					dependencies = new ArrayList<String>();
					fillerSetsDependencies.put(fillerSet, dependencies);
				}
				dependencies.addAll(Arrays.asList(elementFillerSet.getAttribute("fillerSets").split(",")));
			}
			
			fillerSet.loadFromXmlElement(elementFillerSet);
		}
	}
	
	public static void finishLoading() {
		// import fillerSets into each others
		propagateFillerSets();
		
		// compute fillerSets randomization tables
		for (FillerSet fillerSet : fillerSetsByName.values()) {
			fillerSet.finishContruction();
		}
		
		// compute groups randomization tables
		for (RandomCollection randomCollection : fillerSetsByGroup.values()) {
			// randomCollection.finishContruction();
		}
	}
	
	private static void propagateFillerSets() {
		while (!fillerSetsDependencies.isEmpty()) {
			TreeMap<FillerSet, ArrayList<String>> fillerSetsLeftToImport = new TreeMap<FillerSet, ArrayList<String>>();
			
			for (Entry<FillerSet, ArrayList<String>> entry : fillerSetsDependencies.entrySet()) {
				ArrayList<String> newDependencies = new ArrayList();
				for (String dependency : entry.getValue()) {
					if (!fillerSetsByName.containsKey(dependency)) {
						WarpDrive.logger.error("Ignoring FillerSet " + dependency + " dependency in FillerSet " + entry.getKey());
						
					} else if (fillerSetsDependencies.containsKey(fillerSetsByName.get(dependency))) {
						// skip until it is loaded
						newDependencies.add(dependency);
						
					} else {
						entry.getKey().loadFrom(fillerSetsByName.get(dependency));
					}
				}
				if (!newDependencies.isEmpty()) {
					fillerSetsLeftToImport.put(entry.getKey(), newDependencies);
				}
			}
			
			fillerSetsDependencies = fillerSetsLeftToImport;
		}
	}
	
	public static boolean doesFillerSetExist(String groupOrName) {
		return fillerSetsByName.containsKey(groupOrName) || fillerSetsByName.containsKey(groupOrName);
	}
}
