package cr0s.warpdrive.data;

import java.util.HashMap;

public enum EnumHullPlainType {
	PLAIN               ("plain"),
	TILED               ("tiled"),
	;
	
	private final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumHullPlainType> ID_MAP = new HashMap<>();
	
	static {
		length = EnumHullPlainType.values().length;
		for (final EnumHullPlainType hullPlainType : values()) {
			ID_MAP.put(hullPlainType.ordinal(), hullPlainType);
		}
	}
	
	EnumHullPlainType(final String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public static EnumHullPlainType get(final int index) {
		return ID_MAP.get(index);
	}
	
	public String getName() { return unlocalizedName; }
}
