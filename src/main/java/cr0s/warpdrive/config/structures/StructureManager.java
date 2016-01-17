package cr0s.warpdrive.config.structures;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.RandomCollection;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlPreprocessor;


public class StructureManager {
	public static final String GROUP_STARS = "star";
	public static final String GROUP_MOONS = "moon";
	public static final String GROUP_GASCLOUDS = "gascloud";
	public static final String GROUP_ASTEROIDS = "asteroid";
	
	private static RandomCollection<Star> stars = new RandomCollection<Star>();
	private static RandomCollection<Planetoid> moons = new RandomCollection<Planetoid>();
	private static RandomCollection<Asteroid> gasClouds = new RandomCollection<Asteroid>();
	private static RandomCollection<Asteroid> asteroids = new RandomCollection<Asteroid>();
	
	public static void loadStructures(File dir) {
		
		dir.mkdir();
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a directory!");
		}
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file_notUsed, String name) {
				return name.startsWith("structure") && name.endsWith(".xml");
			}
		});
		
		for (File file : files) {
			try {
				loadXmlStructureFile(file);
			} catch (Exception exception) {
				WarpDrive.logger.error("Error loading file " + file.getName() + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}
	}
	
	private static void loadXmlStructureFile(File file) throws SAXException, IOException, InvalidXmlException {
		WarpDrive.logger.info("Loading structure data file " + file.getName());
		Document document = WarpDriveConfig.getXmlDocumentBuilder().parse(file);
		
		// pre-process the file
		String result = XmlPreprocessor.checkModRequirements(document.getDocumentElement());
		if (!result.isEmpty()) {
			WarpDrive.logger.info("Skipping structure " + file.getName() + " due to " + result);
			return;
		}
		
		XmlPreprocessor.doModReqSanitation(document);
		XmlPreprocessor.doLogicPreprocessing(document);
		
		// only add FillerSets
		NodeList nodeListStructures = document.getElementsByTagName("structure");
		for (int structureIndex = 0; structureIndex < nodeListStructures.getLength(); structureIndex++) {
			
			Element elementStructure = (Element) nodeListStructures.item(structureIndex);
			
			String group = elementStructure.getAttribute("group");
			if (group.isEmpty()) {
				throw new InvalidXmlException("Structure " + (structureIndex + 1) + "/" + nodeListStructures.getLength() + " is missing a group attribute!");
			}
			
			String name = elementStructure.getAttribute("name");
			if (name.isEmpty()) {
				throw new InvalidXmlException("Structure " + (structureIndex + 1) + "/" + nodeListStructures.getLength() + " is missing a name attribute!");
			}
			
			WarpDrive.logger.info("- found structure " + group + ":" + name);
			
			switch (group) {
			case GROUP_STARS:
				stars.loadFromXML(new Star(name), elementStructure);
				break;
			case GROUP_MOONS:
				moons.loadFromXML(new Planetoid(name), elementStructure);
				break;
			case GROUP_ASTEROIDS:
				asteroids.loadFromXML(new Asteroid(name), elementStructure);
				break;
			case GROUP_GASCLOUDS:
				gasClouds.loadFromXML(new Asteroid(name), elementStructure);
				break;
			default:
				throw new InvalidXmlException("Structure " + (structureIndex + 1) + "/" + nodeListStructures.getLength() + " has invalid group " + group);
				// break;
			}
		}
	}
	
	public static DeployableStructure getStructure(Random random, final String group, final String name) {
		if (name == null || name.length() == 0) {
			if (group == null || group.isEmpty()) {
				return null;
			} else if (group.equalsIgnoreCase(GROUP_STARS)) {
				return stars.getRandomEntry(random);
			} else if (group.equalsIgnoreCase(GROUP_MOONS)) {
				return moons.getRandomEntry(random);
			} else if (group.equalsIgnoreCase(GROUP_ASTEROIDS)) {
				return asteroids.getRandomEntry(random);
			}
		} else {
			if (group == null || group.isEmpty()) {
				return null;
			} else if (group.equalsIgnoreCase(GROUP_STARS)) {
				return stars.getNamedEntry(name);
			} else if (group.equalsIgnoreCase(GROUP_MOONS)) {
				return moons.getNamedEntry(name);
			} else if (group.equalsIgnoreCase(GROUP_ASTEROIDS)) {
				return asteroids.getNamedEntry(name);
			}
		}
		
		// not found or nothing defined
		return null;
	}
	
	public static DeployableStructure getStar(Random random, final String name) {
		return getStructure(random, GROUP_STARS, name);
	}
	
	public static DeployableStructure getMoon(Random random, final String name) {
		return getStructure(random, GROUP_MOONS, name);
	}
	
	public static DeployableStructure getAsteroid(Random random, final String name) {
		return getStructure(random, GROUP_ASTEROIDS, name);
	}
	
	public static DeployableStructure getGasCloud(Random random, final String name) {
		return getStructure(random, GROUP_GASCLOUDS, name);
	}
}
