package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IForceFieldShape;

import java.util.HashMap;
import java.util.Map;

public enum EnumForceFieldShape implements IForceFieldShape {
	NONE               ("none"),
	SPHERE             ("sphere"),
	CYLINDER_H         ("cylinder_h"),
	CYLINDER_V         ("cylinder_v"),
	CUBE               ("cube"),
	PLANE              ("plane"),
	TUBE               ("tube"),
	TUNNEL             ("tunnel");
	
	public final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumForceFieldShape> ID_MAP = new HashMap<>();
	
	static {
		length = EnumForceFieldShape.values().length;
		for (EnumForceFieldShape enumForceFieldShape : values()) {
			ID_MAP.put(enumForceFieldShape.ordinal(), enumForceFieldShape);
		}
	}
	
	EnumForceFieldShape(String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public static EnumForceFieldShape get(final int damage) {
		return ID_MAP.get(damage);
	}
	
	@Override
	public Map<VectorI, Boolean> getVertexes(ForceFieldSetup forceFieldSetup) {
		VectorI vScale = forceFieldSetup.vMax.clone().translateBack(forceFieldSetup.vMin);
		Map<VectorI, Boolean> mapVertexes = new HashMap<>(vScale.x * vScale.y * vScale.z);
		int radius;
		int thickness;
		int radiusInterior2;
		int radiusPerimeter2;
		VectorI vCenter;
		switch(this) {
		case SPHERE:
			radius = forceFieldSetup.vMax.y;
			radiusInterior2 = Math.round((radius - forceFieldSetup.thickness) * (radius - forceFieldSetup.thickness));
			radiusPerimeter2 = Math.round((radius + forceFieldSetup.thickness) * (radius + forceFieldSetup.thickness));
			vCenter = new VectorI(0, 0, 0);
			for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
				int y2 = (y - vCenter.y) * (y - vCenter.y);
				for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
					int x2 = (x - vCenter.x) * (x - vCenter.x);
					for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
						int z2 = (z - vCenter.z) * (z - vCenter.z);
						if (x2 + y2 + z2 <= radiusPerimeter2) {
							mapVertexes.put(new VectorI(x, y, z), x2 + y2 + z2 >= radiusInterior2);
						}
					}
				}
			}
			break;
		
		case CYLINDER_H:
			radius = Math.round((forceFieldSetup.vMax.y + forceFieldSetup.vMax.z) / 2);
			radiusInterior2 = Math.round((radius - forceFieldSetup.thickness) * (radius - forceFieldSetup.thickness));
			radiusPerimeter2 = Math.round((radius + forceFieldSetup.thickness) * (radius + forceFieldSetup.thickness));
			vCenter = new VectorI(0, 0, 0);
			for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
				int y2 = (y - vCenter.y) * (y - vCenter.y);
				for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
					int z2 = (z - vCenter.z) * (z - vCenter.z);
					if (y2 + z2 <= radiusPerimeter2) {
						boolean isPerimeter = y2 + z2 >= radiusInterior2;
						for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
							mapVertexes.put(new VectorI(x, y, z), isPerimeter);
						}
					}
				}
			}
			break;
		
		case CYLINDER_V:
			radius = Math.round((forceFieldSetup.vMax.x + forceFieldSetup.vMax.y) / 2);
			radiusInterior2 = Math.round((radius - forceFieldSetup.thickness) * (radius - forceFieldSetup.thickness));
			radiusPerimeter2 = Math.round((radius + forceFieldSetup.thickness) * (radius + forceFieldSetup.thickness));
			vCenter = new VectorI(0, 0, 0);
			for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
				int x2 = (x - vCenter.x) * (x - vCenter.x);
				for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
					int y2 = (y - vCenter.y) * (y - vCenter.y);
					if (x2 + y2 <= radiusPerimeter2) {
						boolean isPerimeter = x2 + y2 >= radiusInterior2;
						for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
							mapVertexes.put(new VectorI(x, y, z), isPerimeter);
						}
					}
				}
			}
			break;
		
		case TUBE:
			radius = Math.round((forceFieldSetup.vMax.x + forceFieldSetup.vMax.z) / 2);
			radiusInterior2 = Math.round((radius - forceFieldSetup.thickness) * (radius - forceFieldSetup.thickness));
			radiusPerimeter2 = Math.round((radius + forceFieldSetup.thickness) * (radius + forceFieldSetup.thickness));
			vCenter = new VectorI(0, 0, 0);
			for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
				int x2 = (x - vCenter.x) * (x - vCenter.x);
				for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
					int z2 = (z - vCenter.z) * (z - vCenter.z);
					if (x2 + z2 <= radiusPerimeter2) {
						boolean isPerimeter = x2 + z2 >= radiusInterior2;
						for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
							mapVertexes.put(new VectorI(x, y, z), isPerimeter);
						}
					}
				}
			}
			break;
		
		case CUBE:
			thickness = Math.round(forceFieldSetup.thickness);
			for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
				boolean yFace = Math.abs(y - forceFieldSetup.vMin.y) <= thickness
					                || Math.abs(y - forceFieldSetup.vMax.y) <= thickness;
				for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
					boolean xFace = Math.abs(x - forceFieldSetup.vMin.x) <= thickness
						                || Math.abs(x - forceFieldSetup.vMax.x) <= thickness;
					for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
						boolean zFace = Math.abs(z - forceFieldSetup.vMin.z) <= thickness
							                || Math.abs(z - forceFieldSetup.vMax.z) <= thickness;
						mapVertexes.put(new VectorI(x, y, z), xFace || yFace || zFace);
					}
				}
			}
			break;
		
		case PLANE:
			thickness = Math.round(forceFieldSetup.thickness);
			for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
				boolean yFace = Math.abs(y - forceFieldSetup.vMin.y) <= thickness
					                || Math.abs(y - forceFieldSetup.vMax.y) <= thickness;
				for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
					for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
						mapVertexes.put(new VectorI(x, y, z), yFace);
					}
				}
			}
			break;
		
		case TUNNEL:
			thickness = Math.round(forceFieldSetup.thickness);
			for (int y = forceFieldSetup.vMin.y; y <= forceFieldSetup.vMax.y; y++) {
				for (int x = forceFieldSetup.vMin.x; x <= forceFieldSetup.vMax.x; x++) {
					boolean xFace = Math.abs(x - forceFieldSetup.vMin.x) <= thickness
						                || Math.abs(x - forceFieldSetup.vMax.x) <= thickness;
					for (int z = forceFieldSetup.vMin.z; z <= forceFieldSetup.vMax.z; z++) {
						boolean isPerimeter = xFace
							                      || Math.abs(z - forceFieldSetup.vMin.z) <= thickness
							                      || Math.abs(z - forceFieldSetup.vMax.z) <= thickness;
						mapVertexes.put(new VectorI(x, y, z), isPerimeter);
					}
				}
			}
			break;
		
		default:
			break;
			
		}
		
		return mapVertexes;
	}
}
