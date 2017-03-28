package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.CelestialObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;

public class CelestialObjectManager extends XmlFileManager {
	
	private static CelestialObjectManager INSTANCE = new CelestialObjectManager();
	private static HashMap<String, HashMap<String, CelestialObject>> celestialObjectsByGroup = new HashMap<>();
	public static CelestialObject[] celestialObjects = null;
	
	public static void clearForReload() {
		// create a new object instead of clearing, in case another thread is iterating through it
		celestialObjectsByGroup = new HashMap<>();
	}
	
	public static void load(File dir) {
		INSTANCE.load(dir, "celestialObjects", "celestialObject");
		
		// optimize execution speed by flattening the data structure
		int count = 0;
		for(HashMap<String, CelestialObject> mapCelestialObjectByName : celestialObjectsByGroup.values()) {
			count += mapCelestialObjectByName.size();
		}
		celestialObjects = new CelestialObject[count];
		int index = 0;
		for(HashMap<String, CelestialObject> mapCelestialObjectByName : celestialObjectsByGroup.values()) {
			for (CelestialObject celestialObject : mapCelestialObjectByName.values()) {
				celestialObjects[index++] = celestialObject;
				celestialObject.resolveParent();
			}
		}
		
		// check overlapping regions
		for (CelestialObject celestialObject1 : celestialObjects) {
			for (CelestialObject celestialObject2 : celestialObjects) {
				if (celestialObject1 == celestialObject2) {
					continue;
				}
				// are they overlapping in a common parent dimension?
				if (!celestialObject1.isHyperspace() && !celestialObject2.isHyperspace() && celestialObject1.parentDimensionId == celestialObject2.parentDimensionId) {
					AxisAlignedBB areaInParent1 = celestialObject1.getAreaInParent();
					AxisAlignedBB areaInParent2 = celestialObject2.getAreaInParent();
					if (areaInParent1.intersectsWith(areaInParent2)) {
						WarpDrive.logger.error("Overlapping parent areas detected " + celestialObject1.parentDimensionId);
						WarpDrive.logger.error("Celestial object 1 is " + celestialObject1 + " with area " + areaInParent1);
						WarpDrive.logger.error("Celestial object 2 is " + celestialObject2 + " with area " + areaInParent2);
						throw new RuntimeException(String.format(
							"Invalid celestial objects definition:\n %s\nand\n %s\nare overlapping each others. Update your configuration to fix it.",
							celestialObject1.toString(), celestialObject2.toString()));
					}
				}
				// are they in the same dimension?
				if (!celestialObject1.isVirtual && !celestialObject2.isVirtual && celestialObject1.dimensionId == celestialObject2.dimensionId) {
					AxisAlignedBB areaToReachParent1 = celestialObject1.getAreaToReachParent();
					AxisAlignedBB areaToReachParent2 = celestialObject2.getAreaToReachParent();
					if (areaToReachParent1.intersectsWith(areaToReachParent2)) {
						WarpDrive.logger.error("Overlapping areas detected in dimension " + celestialObject1.dimensionId);
						WarpDrive.logger.error("Celestial object 1 is " + celestialObject1 + " with area " + areaToReachParent1);
						WarpDrive.logger.error("Celestial object 2 is " + celestialObject2 + " with area " + areaToReachParent2);
						throw new RuntimeException(String.format(
							"Invalid celestial objects definition:\n %s\nand\n %s\nare overlapping each others. Update your configuration to fix it.",
							celestialObject1.toString(), celestialObject2.toString()));
					}
				}
			}
		}
		
		// We're not checking invalid dimension id, so they can be pre-allocated (see MystCraft)
	}
	
	@Override
	protected void parseRootElement(final String location, final Element elementCelestialObject) throws InvalidXmlException, SAXException, IOException {
		parseCelestiaObjectElement(location, elementCelestialObject, "", "");
	}
	
	protected void parseCelestiaObjectElement(final String location, final Element elementCelestialObject, final String groupParent, final String nameParent) throws InvalidXmlException, SAXException, IOException {
		CelestialObject celestialObjectRead = new CelestialObject(location, groupParent, nameParent, elementCelestialObject);
		
		HashMap<String, CelestialObject> celestialObjectByName = celestialObjectsByGroup.computeIfAbsent(celestialObjectRead.group, k -> new HashMap<>());
		
		CelestialObject celestialObjectExisting = celestialObjectByName.get(celestialObjectRead.name);
		if (celestialObjectExisting == null) {
			celestialObjectByName.put(celestialObjectRead.name, celestialObjectRead);
		} else {
			WarpDrive.logger.warn(String.format("Celestial object %s is already defined, keeping original definition", celestialObjectRead.getFullName()));
		}
		
		// look for optional child element(s)
		List<Element> listChildren = XmlFileManager.getChildrenElementByTagName(elementCelestialObject, "celestialObject");
		if (!listChildren.isEmpty()) {
			for (int indexElement = 0; indexElement < listChildren.size(); indexElement++) {
				Element elementChild = listChildren.get(indexElement);
				String locationChild = String.format("%s Celestial object %s > child %d/%d", location, celestialObjectRead.getFullName(), indexElement + 1, listChildren.size());
				parseCelestiaObjectElement(locationChild, elementChild, celestialObjectRead.group, celestialObjectRead.name);
			}
		}
	}
	
	public static CelestialObject get(final String group, final String name) {
		HashMap<String, CelestialObject> celestialObjectByName = celestialObjectsByGroup.get(group);
		if (celestialObjectByName == null) {
			return null;
		}
		return celestialObjectByName.get(name);
	}	
}
