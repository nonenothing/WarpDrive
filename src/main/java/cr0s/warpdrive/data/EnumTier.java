package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumTier implements IStringSerializable {
	CREATIVE ("creative",  0),
	BASIC    ("basic"   ,  1),
	ADVANCED ("advanced",  2),
	SUPERIOR ("superior",  3);
	
	private final String name;
	private final int index;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumTier> ID_MAP = new HashMap<>();
	
	static {
		length = EnumTier.values().length;
		for (EnumTier enumTier : values()) {
			ID_MAP.put(enumTier.index, enumTier);
		}
	}
	
	EnumTier(final String name, final int index) {
		this.name = name;
		this.index = index;
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
	
	public static EnumTier get(final int index) {
		return ID_MAP.get(index);
	}
	
	public int getIndex() {
		return index;
	}
}
