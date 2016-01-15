package cr0s.warpdrive.config.structures;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlPreprocessor;
import cr0s.warpdrive.config.XmlPreprocessor.ModCheckResults;
import cr0s.warpdrive.config.XmlRepresentable;


public class StructureManager {
	
	private static RandomCollection<Star> stars = new RandomCollection<Star>();
	private static RandomCollection<Planetoid> moons = new RandomCollection<Planetoid>();
	private static RandomCollection<Planetoid> gasClouds = new RandomCollection<Planetoid>();
	private static RandomCollection<Asteroid> asteroids = new RandomCollection<Asteroid>();
	
	public static void loadStructures(String structureConfDir) {
		loadStructures(new File(structureConfDir));
	}
	
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
				
				WarpDrive.logger.info("Loading structure data file " + file.getName());
				
				loadXmlStructureFile(file);
				
				WarpDrive.logger.info("Finished loading structure data file " + file.getName());
				
			} catch (Exception exception) {
				WarpDrive.logger.error("Error loading file " + file.getName() + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}
	}
	
	private static void loadXmlStructureFile(File file) throws SAXException, IOException, InvalidXmlException {
		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(file);
		
		ModCheckResults res = XmlPreprocessor.checkModRequirements(base.getDocumentElement());
		
		if (!res.isEmpty()) {
			WarpDrive.logger.info("Skippping structure " + file.getName() + " due to " + res);
			return;
		}
		
		XmlPreprocessor.doModReqSanitation(base);
		XmlPreprocessor.doLogicPreprocessing(base);
		
		NodeList structures = base.getElementsByTagName("structure");
		for (int i = 0; i < structures.getLength(); i++) {
			
			Element struct = (Element) structures.item(i);
			
			String group = struct.getAttribute("group");
			String name = struct.getAttribute("name");
			
			WarpDrive.logger.info("Loading structure " + name);
			
			if (group.isEmpty())
				throw new InvalidXmlException("Structure must have a group!");
			
			int radius = 0;
			
			if (group.equalsIgnoreCase("star")) {
				stars.loadFromXML(new Star(radius), struct);
			} else if (group.equalsIgnoreCase("moon")) {
				moons.loadFromXML(new Planetoid(radius), struct);
			} else if (group.equalsIgnoreCase("asteroid")) {
				asteroids.loadFromXML(new Asteroid(), struct);
			}
		}
	}
	
	public static DeployableStructure getStructure(Random random, final String name, final String type) {
		if (name == null || name.length() == 0) {
			if (type == null || type.length() == 0) {
				return stars.next(random);
			} else if (type.equalsIgnoreCase("star")) {
				return stars.next(random);
			} else if (type.equalsIgnoreCase("moon")) {
				return moons.next(random);
			} else if (type.equalsIgnoreCase("asteroid")) {
				return asteroids.next(random);
			}
		} else {
			for (Star star : stars.elements()) {
				if (star.getName().equals(name))
					return star;
			}
		}
		
		// not found or nothing defined => return null
		return null;
	}
	
	public static DeployableStructure getStar(Random random, final String name) {
		return getStructure(random, name, "star");
	}
	
	public static DeployableStructure getMoon(Random random, final String name) {
		return getStructure(random, name, "moon");
	}
	
	public static DeployableStructure getAsteroid(Random random, final String name) {
		return getStructure(random, name, "asteroid");
	}
	
	public static DeployableStructure getGasCloud(Random random, final String name) {
		return getStructure(random, name, "cloud");
	}
	
	/**
	 * Collection of elements with weights. Helps to select element with controlled odds.
	 * 
	 * @author ncrashed
	 *
	 * @param <E>
	 */
	private static class RandomCollection<E extends XmlRepresentable> {
	    private final NavigableMap<Double, E> weightMap = new TreeMap<Double, E>();
	    private double totalWeight = 0;
	    private final NavigableMap<Double, E> ratioMap = new TreeMap<Double, E>();
	    private double totalRatio = 0;
	    private final ArrayList<E> list = new ArrayList<E>();
	 
	    
	    /**
	     * Add new object and its weight.
	     * @param weight Used for random pick. The higher the value is relatively to others, the higher odds of choosing the object.
	     * @param obj Object to add 
	     */
	    public void add(double weight, E obj) {
	        if (weight <= 0) {
	        	WarpDrive.logger.warn("Structure weight is negative or zero, skipping");
	        	return;
	        }
	        totalWeight += weight;
	        weightMap.put(totalWeight, obj);
	        list.add(obj);
	    }

	    /**
	     * Add new object and its ratio.
	     * Warning: if total ratio goes higher than 1.0, element won't be added to collection.
	     * @param ratio Chance of random pick in range (0, 1.0]. In contrast to weights, ratio is fixed and chances don't change if you add more elements. 
	     * @param obj Object to add
	     */
	    public void addRatio(double ratio, E obj) {
	    	if (ratio <= 0 || ratio >= 1.0) {
	    		WarpDrive.logger.warn("Structure ratio isn't in (0, 1.0] bounds, skipping");
	    		return;
	    	}
	    	
	    	if (totalRatio + ratio > 1.0) {
	    		WarpDrive.logger.warn("Structures total ratio is greater than 1.0, skipping");
	    		return;
	    	}
	    	totalRatio += ratio;
	    	ratioMap.put(totalRatio, obj);
	    	list.add(obj);
	    }
	    
	    /**
	     * Pick random object according their weights
	     * @param random
	     * @return Random object or null if there is no objects to pick.
	     */
	    public E next(Random random) {
	        double value = random.nextDouble();
	        
	        if (value < totalRatio) { // hit ratio part of values
	        	return ratioMap.ceilingEntry(value).getValue();
	        } else { // hit dynamic part of values, weighted ones
	        	double weight = (value - totalRatio)*totalWeight;
	        	return weightMap.ceilingEntry(weight).getValue();
	        }
	    }
	    
	    /**
	     * @return All registered objects
	     */
	    public ArrayList<E> elements() {
	    	return list;
	    }
	    
	    /**
	     * Loads object from given XML element and parses configurations for weighted pick.
	     * @param obj Object to load from *struct*
	     * @param struct Part of XML config
	     * @throws InvalidXmlException
	     */
	    public void loadFromXML(E obj, Element struct) throws InvalidXmlException {
	    	obj.loadFromXmlElement(struct);
			
	    	try {
	    		String ratioStr = struct.getAttribute("ratio");
	    		if(!ratioStr.isEmpty()) {
	    			double ratio = Double.parseDouble(ratioStr);
	    			this.addRatio(ratio, obj);
	    		} else { // try weight
	    			try {
	    				int weight = 1;
	    				String weightStr = struct.getAttribute("weight");
	    				if(!weightStr.isEmpty()) {
	    					weight = Integer.parseInt(weightStr);
	    					weight = Math.max(1, weight);
	    				}
	    				
	    				this.add(weight, obj);
	    			} catch (NumberFormatException gdbg) {
	    				throw new InvalidXmlException("Weight must be int!");
	    			}
	    		}
	    	} catch (NumberFormatException gdbg) {
				throw new InvalidXmlException("Ratio must be double!");
			}
	    }
	}
}
