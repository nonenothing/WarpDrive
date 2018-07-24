package cr0s.warpdrive.data;

import javax.annotation.Nonnull;
import java.util.HashMap;

import net.minecraft.util.IStringSerializable;

public enum EnumFrameType implements IStringSerializable {
	
	PLAIN               ("plain"    , 0),
	RADIATION           ("radiation", 1),
	ELECTRIC            ("electric" , 2),
	;
	
	private final String name;
	private final int metadata;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumFrameType> ID_MAP = new HashMap<>();
	
	static {
		length = EnumFrameType.values().length;
		for (final EnumFrameType enumFrameType : values()) {
			ID_MAP.put(enumFrameType.getMetadata(), enumFrameType);
		}
	}
	
	EnumFrameType(final String name, final int metadata) {
		this.name = name;
		this.metadata = metadata;
	}
	
	public static EnumFrameType byMetadata(final int metadata) {
		return ID_MAP.get(metadata);
	}
	
	@Nonnull
	@Override
	public String getName() { return name; }
	
	public int getMetadata() {
		return metadata;
	}
}
