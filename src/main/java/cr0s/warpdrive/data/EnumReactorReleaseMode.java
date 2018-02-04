package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumReactorReleaseMode implements IStringSerializable {
	OFF        ("OFF"),
	UNLIMITED  ("MANUAL"),
	ABOVE      ("ABOVE"),
	AT_RATE    ("RATE");
	
	private final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumReactorReleaseMode> ID_MAP = new HashMap<>();
	
	static {
		length = EnumReactorReleaseMode.values().length;
		for (EnumReactorReleaseMode componentType : values()) {
			ID_MAP.put(componentType.ordinal(), componentType);
		}
	}
	
	EnumReactorReleaseMode(String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public static EnumReactorReleaseMode get(final int ordinal) {
		return ID_MAP.get(ordinal);
	}
	
	public String getUnlocalizedName() {
		return unlocalizedName;
	}
	
	@Nonnull
	@Override
	public String getName() { return unlocalizedName; }
}
