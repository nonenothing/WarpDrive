package cr0s.warpdrive.data;

import javax.annotation.Nonnull;
import java.util.HashMap;

import net.minecraft.util.IStringSerializable;

public enum EnumShipCommand implements IStringSerializable {
	
	OFFLINE     ("offline"),      // Offline allows to move sub-ships
	IDLE        ("idle"),         //
	MANUAL      ("manual"),       // Move ship around including take off and landing
	// AUTOPILOT("autopilot"),    // Move ship towards a far destination
	// SUMMON   ("summon"),       // Summoning crew
	HYPERDRIVE  ("hyperdrive"),   // Jump to/from Hyperspace
	GATE        ("gate"),         // Jump via jumpgate
	MAINTENANCE ("maintenance");  // Maintenance mode
	
	private final String name;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumShipCommand> ID_MAP = new HashMap<>();
	
	static {
		length = EnumShipCommand.values().length;
		for (final EnumShipCommand forceFieldShape : values()) {
			ID_MAP.put(forceFieldShape.ordinal(), forceFieldShape);
		}
	}
	
	EnumShipCommand(final String name) {
		this.name = name;
	}
	
	public static EnumShipCommand get(final int damage) {
		return ID_MAP.get(damage);
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
}
