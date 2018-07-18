package cr0s.warpdrive.data;

import javax.annotation.Nonnull;
import java.util.HashMap;

import net.minecraft.item.EnumRarity;
import net.minecraft.util.IStringSerializable;

public enum EnumAirTankTier implements IStringSerializable {
	
	CANISTER ("canister", 0, EnumRarity.COMMON  ),
	BASIC    ("basic"   , 1, EnumRarity.COMMON  ),
	ADVANCED ("advanced", 2, EnumRarity.UNCOMMON),
	SUPERIOR ("superior", 3, EnumRarity.RARE    );
	
	private final String name;
	private final int index;
	private final EnumRarity enumRarity;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumAirTankTier> ID_MAP;
	
	static {
		length = EnumAirTankTier.values().length;
		ID_MAP = new HashMap<>(length);
		for (final EnumAirTankTier enumAirTankTier : values()) {
			ID_MAP.put(enumAirTankTier.index, enumAirTankTier);
		}
	}
	
	EnumAirTankTier(final String name, final int index, final EnumRarity enumRarity) {
		this.name = name;
		this.index = index;
		this.enumRarity = enumRarity;
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
	
	public static EnumAirTankTier get(final int index) {
		return ID_MAP.get(index);
	}
	
	public int getIndex() {
		return index;
	}
	
	public EnumRarity getRarity() {
		return enumRarity;
	}
}
