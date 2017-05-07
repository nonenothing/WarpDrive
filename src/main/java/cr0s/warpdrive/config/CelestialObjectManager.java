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
	
	private static final CelestialObjectManager INSTANCE = new CelestialObjectManager();
	private static HashMap<String, HashMap<String, CelestialObject>> celestialObjectsByGroup = new HashMap<>();
	public static CelestialObject[] celestialObjects = null;
	
	public static void clearForReload() {
		// create a new object instead of clearing, in case another thread is iterating through it
		celestialObjectsByGroup = new HashMap<>();
	}
	
	public static void load(final File dir) {
		INSTANCE.load(dir, "celestialObjects", "celestialObject");
		INSTANCE.rebuildAndValidate();
	}
	
	// @TODO add a proper API
	public static void updateInRegistry(final CelestialObject celestialObject) {
		INSTANCE.addOrUpdateInRegistry(celestialObject, true);
		INSTANCE.rebuildAndValidate();
	}
	
	private void addOrUpdateInRegistry(final CelestialObject celestialObject, final boolean isUpdating) {
		final HashMap<String, CelestialObject> celestialObjectByName = celestialObjectsByGroup.computeIfAbsent(celestialObject.group, k -> new HashMap<>());
		
		final CelestialObject celestialObjectExisting = celestialObjectByName.get(celestialObject.name);
		if (celestialObjectExisting == null || isUpdating) {
			celestialObjectByName.put(celestialObject.name, celestialObject);
		} else {
			WarpDrive.logger.warn(String.format("Celestial object %s is already defined, keeping original definition", celestialObject.getFullName()));
		}
	}
	
	private void rebuildAndValidate() {
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
					final AxisAlignedBB areaInParent1 = celestialObject1.getAreaInParent();
					final AxisAlignedBB areaInParent2 = celestialObject2.getAreaInParent();
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
					final AxisAlignedBB areaToReachParent1 = celestialObject1.getAreaToReachParent();
					final AxisAlignedBB areaToReachParent2 = celestialObject2.getAreaToReachParent();
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
	
	private void parseCelestiaObjectElement(final String location, final Element elementCelestialObject, final String groupParent, final String nameParent) throws InvalidXmlException, SAXException, IOException {
		final CelestialObject celestialObjectRead = new CelestialObject(location, groupParent, nameParent, elementCelestialObject);
		
		addOrUpdateInRegistry(celestialObjectRead, false);
		
		// look for optional child element(s)
		final List<Element> listChildren = XmlFileManager.getChildrenElementByTagName(elementCelestialObject, "celestialObject");
		if (!listChildren.isEmpty()) {
			for (int indexElement = 0; indexElement < listChildren.size(); indexElement++) {
				final Element elementChild = listChildren.get(indexElement);
				final String locationChild = String.format("%s Celestial object %s > child %d/%d", location, celestialObjectRead.getFullName(), indexElement + 1, listChildren.size());
				parseCelestiaObjectElement(locationChild, elementChild, celestialObjectRead.group, celestialObjectRead.name);
			}
		}
	}
	
	public static CelestialObject get(final String group, final String name) {
		final HashMap<String, CelestialObject> celestialObjectByName = celestialObjectsByGroup.get(group);
		if (celestialObjectByName == null) {
			return null;
		}
		return celestialObjectByName.get(name);
	}	
}
