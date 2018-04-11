package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumTransporterBeaconState implements IStringSerializable {
	
	DISABLED      (0, "disabled"),    // disabled
	IDLE          (1, "idle"),        // enabling, waiting for lock
	ACQUIRING     (2, "acquiring"),   // acquiring lock
	ENERGIZING    (3, "energizing");   // transferring entities
	
	private final int metadata;
	private final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumTransporterBeaconState> ID_MAP = new HashMap<>();
	
	static {
		length = EnumTransporterBeaconState.values().length;
		for (final EnumTransporterBeaconState transporterBeaconState : values()) {
			ID_MAP.put(transporterBeaconState.ordinal(), transporterBeaconState);
		}
	}
	
	EnumTransporterBeaconState(final int metadata, final String unlocalizedName) {
		this.metadata = metadata;
		this.unlocalizedName = unlocalizedName;
	}
	
	public int getMetadata() {
		return metadata;
	}
	
	public static EnumTransporterBeaconState get(final int id) {
		return ID_MAP.get(id);
	}
	
	@Nonnull
	@Override
	public String getName() { return unlocalizedName; }
}
