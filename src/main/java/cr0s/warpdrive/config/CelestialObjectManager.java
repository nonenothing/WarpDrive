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
	private static HashMap<String, CelestialObject> celestialObjectsById = new HashMap<>();
	public static CelestialObject[] celestialObjects = null;
	
	public static void clearForReload() {
		// create a new object instead of clearing, in case another thread is iterating through it
		celestialObjectsById = new HashMap<>();
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
		final CelestialObject celestialObjectExisting = celestialObjectsById.get(celestialObject.id);
		if (celestialObjectExisting == null || isUpdating) {
			celestialObjectsById.put(celestialObject.id, celestialObject);
		} else {
			WarpDrive.logger.warn(String.format("Celestial object %s is already defined, keeping original definition", celestialObject.id));
		}
	}
	
	private void rebuildAndValidate() {
		// optimize execution speed by flattening the data structure
		final int count = celestialObjectsById.size();
		celestialObjects = new CelestialObject[count];
		int index = 0;
		for (CelestialObject celestialObject : celestialObjectsById.values()) {
			celestialObjects[index++] = celestialObject;
			celestialObject.resolveParent();
		}
		
		// check overlapping regions
		int countErrors = 0;
		for (int indexCelestialObject1 = 0; indexCelestialObject1 < count; indexCelestialObject1++) {
			final CelestialObject celestialObject1 = celestialObjects[indexCelestialObject1];
			celestialObject1.lateUpdate();
			
			// validate coordinates
			if (!celestialObject1.isVirtual) {
				if (celestialObject1.parentDimensionId != celestialObject1.dimensionId) {// not hyperspace
					final CelestialObject celestialObjectParent = get(celestialObject1.parentId);
					if (celestialObjectParent == null) {
						countErrors++;
						WarpDrive.logger.error(String.format("Validation error #%d\nCelestial object %s refers to unknown parent %s",
						                                     countErrors,
						                                     celestialObject1.id,
						                                     celestialObject1.parentId ));
					} else if ( celestialObject1.parentCenterX - celestialObject1.borderRadiusX < celestialObjectParent.dimensionCenterX - celestialObjectParent.borderRadiusX 
					         || celestialObject1.parentCenterZ - celestialObject1.borderRadiusZ < celestialObjectParent.dimensionCenterZ - celestialObjectParent.borderRadiusZ
					         || celestialObject1.parentCenterX + celestialObject1.borderRadiusX > celestialObjectParent.dimensionCenterX + celestialObjectParent.borderRadiusX
					         || celestialObject1.parentCenterZ + celestialObject1.borderRadiusZ > celestialObjectParent.dimensionCenterZ + celestialObjectParent.borderRadiusZ ) {
						countErrors++;
						WarpDrive.logger.error(String.format("Validation error #%d\nCelestial object %s is outside its parent border.\n%s\n%s\n%s's area in parent %s is outside %s's border %s",
						                                     countErrors,
						                                     celestialObject1.id,
						                                     celestialObject1,
						                                     celestialObjectParent,
						                                     celestialObject1.id,
						                                     celestialObject1.getAreaInParent(),
						                                     celestialObjectParent.id,
						                                     celestialObjectParent.getWorldBorderArea() ));
					}
				}
				if ( celestialObject1.dimensionCenterX - celestialObject1.borderRadiusX < -30000000
				  || celestialObject1.dimensionCenterZ - celestialObject1.borderRadiusZ < -30000000
				  || celestialObject1.dimensionCenterX + celestialObject1.borderRadiusX >= 30000000
				  || celestialObject1.dimensionCenterZ + celestialObject1.borderRadiusZ >= 30000000 ) {
					countErrors++;
					WarpDrive.logger.error(String.format("Validation error #%d\nCelestial object %s is outside the game border +/-30000000.\n%s\n%s border is %s",
					                                     countErrors,
					                                     celestialObject1.id,
					                                     celestialObject1,
					                                     celestialObject1.id,
					                                     celestialObject1.getWorldBorderArea() ));
				}
			}
			
			// validate against other celestial objects
			for (int indexCelestialObject2 = indexCelestialObject1 + 1; indexCelestialObject2 < count; indexCelestialObject2++) {
				final CelestialObject celestialObject2 = celestialObjects[indexCelestialObject2];
				// are they overlapping in a common parent dimension?
				if ( !celestialObject1.isHyperspace()
				  && !celestialObject2.isHyperspace()
				  && celestialObject1.parentDimensionId == celestialObject2.parentDimensionId ) {
					final AxisAlignedBB areaInParent1 = celestialObject1.getAreaInParent();
					final AxisAlignedBB areaInParent2 = celestialObject2.getAreaInParent();
					if (areaInParent1.intersectsWith(areaInParent2)) {
						countErrors++;
						WarpDrive.logger.error(String.format("Validation error #%d\nOverlapping parent areas detected in dimension %d between %s and %s\nArea1 %s from %s\nArea2 %s from %s", 
						                                     countErrors, 
						                                     celestialObject1.parentDimensionId, 
						                                     celestialObject1.id, 
						                                     celestialObject2.id,
						                                     areaInParent1,
						                                     celestialObject1,
						                                     areaInParent2,
						                                     celestialObject2 ));
					}
				}
				// are they in the same dimension?
				if ( !celestialObject1.isVirtual
				  && !celestialObject2.isVirtual
				  && celestialObject1.dimensionId == celestialObject2.dimensionId ) {
					final AxisAlignedBB worldBorderArea1 = celestialObject1.getWorldBorderArea();
					final AxisAlignedBB worldBorderArea2 = celestialObject2.getWorldBorderArea();
					if (worldBorderArea1.intersectsWith(worldBorderArea2)) {
						countErrors++;
						WarpDrive.logger.error(String.format("Validation error #%d\nOverlapping areas detected in dimension %d between %s and %s\nArea1 %s from %s\nArea2 %s from %s",
						                                     countErrors,
						                                     celestialObject1.dimensionId,
						                                     celestialObject1.id,
						                                     celestialObject2.id,
						                                     worldBorderArea1,
						                                     celestialObject1,
						                                     worldBorderArea2,
						                                     celestialObject2 ));
					}
				}
			}
		}
		if (countErrors == 1) {
			throw new RuntimeException("Invalid celestial objects definition: update your configuration to fix this validation error, see logs for details.");
		} else if (countErrors > 0) {
			throw new RuntimeException(String.format(
				"Invalid celestial objects definition: update your configuration to fix those %d validation errors, see logs for details.",
				countErrors));
		}
		
		
		// We're not checking invalid dimension id, so they can be pre-allocated (see MystCraft)
	}
	
	@Override
	protected void parseRootElement(final String location, final Element elementCelestialObject) throws InvalidXmlException, SAXException, IOException {
		parseCelestiaObjectElement(location, elementCelestialObject, "");
	}
	
	private void parseCelestiaObjectElement(final String location, final Element elementCelestialObject, final String parentId) throws InvalidXmlException, SAXException, IOException {
		final CelestialObject celestialObjectRead = new CelestialObject(location, parentId, elementCelestialObject);
		
		addOrUpdateInRegistry(celestialObjectRead, false);
		
		// look for optional child element(s)
		final List<Element> listChildren = XmlFileManager.getChildrenElementByTagName(elementCelestialObject, "celestialObject");
		if (!listChildren.isEmpty()) {
			for (int indexElement = 0; indexElement < listChildren.size(); indexElement++) {
				final Element elementChild = listChildren.get(indexElement);
				final String locationChild = String.format("%s Celestial object %s > child %d/%d",
				                                           location, celestialObjectRead.id, indexElement + 1, listChildren.size());
				parseCelestiaObjectElement(locationChild, elementChild, celestialObjectRead.id);
			}
		}
	}
	
	public static CelestialObject get(final String id) {
		return celestialObjectsById.get(id);
	}	
}
