package cr0s.warpdrive.data;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumForceFieldState implements IStringSerializable {
	NOT_CONNECTED           ("not_connected"),
	CONNECTED_NOT_POWERED   ("connected_not_powered"),
	CONNECTED_OFFLINE       ("connected_offline"),
	CONNECTED_POWERED       ("connected_powered");
	
	private final String name;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumForceFieldState> ID_MAP = new HashMap<>();
	
	static {
		length = EnumForceFieldState.values().length;
		for (EnumForceFieldState enumForceFieldShape : values()) {
			ID_MAP.put(enumForceFieldShape.ordinal(), enumForceFieldShape);
		}
	}
	
	EnumForceFieldState(String name) {
		this.name = name;
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
	
	public static EnumForceFieldState get(final int damage) {
		return ID_MAP.get(damage);
	}
}
