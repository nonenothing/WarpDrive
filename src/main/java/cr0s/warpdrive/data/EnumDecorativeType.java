package cr0s.warpdrive.data;

import java.util.HashMap;

public enum EnumDecorativeType {
	PLAIN              ("plain"),
	ENERGIZED          ("energized"),
	NETWORK            ("network");
	
	public final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumDecorativeType> ID_MAP = new HashMap<>();
	
	static {
		length = EnumDecorativeType.values().length;
		for (EnumDecorativeType componentType : values()) {
			ID_MAP.put(componentType.ordinal(), componentType);
		}
	}
	
	EnumDecorativeType(String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public static EnumDecorativeType get(final int damage) {
		return ID_MAP.get(damage);
	}
}
