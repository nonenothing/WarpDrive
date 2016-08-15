package cr0s.warpdrive.data;

import java.util.HashMap;

public enum EnumComponentType {
	EMERALD_CRYSTAL            ("EmeraldCrystal"),
	ENDER_CRYSTAL              ("EnderCrystal"),
	DIAMOND_CRYSTAL            ("DiamondCrystal"),
	DIFFRACTION_GRATING        ("DiffractionGrating"),
	REACTOR_CORE               ("ReactorCore"),
	COMPUTER_INTERFACE         ("ComputerInterface"),
	POWER_INTERFACE            ("PowerInterface"),
	CAPACITIVE_CRYSTAL         ("CapacitiveCrystal"),
	AIR_CANISTER               ("AirCanisterEmpty"),
	LENS                       ("Lens"),
	ZOOM                       ("Zoom"),
	GLASS_TANK                 ("GlassTank"),
	FLAT_SCREEN                ("FlatScreen"),
	MEMORY_CRYSTAL             ("MemoryCrystal"),
	MOTOR                      ("Motor"),
	BONE_CHARCOAL              ("BoneCharcoal"),
	ACTIVATED_CARBON           ("ActivatedCarbon"),
	LASER_MEDIUM_EMPTY         ("LaserMediumEmpty"),
	COIL_CRYSTAL               ("CoilCrystal"),
	ELECTROMAGNETIC_PROJECTOR  ("ElectromagneticProjector"),
	SUPERCONDUCTOR             ("Superconductor");
	
	public final String unlocalizedName;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumComponentType> ID_MAP = new HashMap<>();
	
	static {
		length = EnumComponentType.values().length;
		for (EnumComponentType enumComponentType : values()) {
			ID_MAP.put(enumComponentType.ordinal(), enumComponentType);
		}
	}
	
	EnumComponentType(String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public static EnumComponentType get(final int damage) {
		return ID_MAP.get(damage);
	}
}
