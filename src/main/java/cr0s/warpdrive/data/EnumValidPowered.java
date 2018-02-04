package cr0s.warpdrive.data;

import javax.annotation.Nonnull;
import java.util.HashMap;

import net.minecraft.util.IStringSerializable;

public enum EnumValidPowered implements IStringSerializable {
	INVALID("invalid", 6),
	VALID  ("valid"  , 0),
	POWERED("powered", 8);
	
	private final String name;
	private final int index;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumValidPowered> ID_MAP = new HashMap<>();
	
	static {
		length = EnumValidPowered.values().length;
		for (final EnumValidPowered validPowered : values()) {
			ID_MAP.put(validPowered.index, validPowered);
		}
	}
	
	EnumValidPowered(final String name, final int index) {
		this.name = name;
		this.index = index;
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
	
	public static EnumValidPowered get(final int index) {
		return ID_MAP.get(index);
	}
	
	public int getIndex() {
		return index;
	}
}
