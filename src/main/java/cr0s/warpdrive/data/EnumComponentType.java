package cr0s.warpdrive.data;

import java.util.HashMap;

public enum EnumComponentType {
	EMERALD_CRYSTAL            ("emeraldCrystal"),
	ENDER_CRYSTAL              ("enderCrystal"),
	DIAMOND_CRYSTAL            ("diamondCrystal"),
	DIFFRACTION_GRATING        ("diffractionGrating"),
	REACTOR_CORE               ("reactorCore"),
	COMPUTER_INTERFACE         ("computerInterface"),
	POWER_INTERFACE            ("powerInterface"),
	CAPACITIVE_CRYSTAL         ("capacitiveCrystal"),
	AIR_CANISTER               ("airCanisterEmpty"),
	LENS                       ("lens"),
	ZOOM                       ("zoom"),
	GLASS_TANK                 ("glassTank"),
	FLAT_SCREEN                ("flatScreen"),
	MEMORY_CRYSTAL             ("memoryCrystal"),
	MOTOR                      ("motor"),
	BONE_CHARCOAL              ("boneCharcoal"),
	ACTIVATED_CARBON           ("activatedCarbon"),
	LASER_MEDIUM_EMPTY         ("laserMediumEmpty"),
	COIL_CRYSTAL               ("coilCrystal"),
	ELECTROMAGNETIC_PROJECTOR  ("electromagneticProjector");
	
	private final String unlocalizedName;
	
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

	public String getUnlocalizedName() {
		return unlocalizedName;
	}
}
