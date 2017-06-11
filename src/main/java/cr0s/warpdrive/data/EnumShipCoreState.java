package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IStringSerializable;

import javax.annotation.Nonnull;

public enum EnumShipCoreState implements IStringSerializable {
	
	DISCONNECTED  (0, "disconnected"),   // Not connected to controller
	IDLE          (1, "idle"),           // Ready for next command
	SCANNING      (2, "scanning"),       // Ready for next command
	ONLINE        (3, "online"),         // Computing parameters
	WARMING_UP    (4, "warming_up"),     // Warmup phase
	COOLING_DOWN  (5, "coolding_down");  // Pending cooldown
	
	private final int metadata;
	private final String unlocalizedName;
	
	EnumShipCoreState(final int metadata, final String unlocalizedName) {
		this.metadata = metadata;
		this.unlocalizedName = unlocalizedName;
	}
	
	public int getMetadata() {
		return metadata;
	}
	
	@Nonnull
	@Override
	public String getName() { return unlocalizedName; }
}
