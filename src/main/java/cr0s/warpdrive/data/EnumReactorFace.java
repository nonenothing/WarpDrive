package cr0s.warpdrive.data;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumReactorFace implements IStringSerializable {
	
	//                tier           inst name          x   y   z  facingLaserProperty
	UNKNOWN          (null          , -1, "unknown"  ,  0,  0,  0, null            ),
	BASIC_SOUTH      (EnumTier.BASIC,  0, "south"    ,  0,  0, -2, EnumFacing.NORTH),
	BASIC_NORTH      (EnumTier.BASIC,  1, "north"    ,  0,  0,  2, EnumFacing.SOUTH),
	BASIC_EAST       (EnumTier.BASIC,  2, "east"     , -2,  0,  0, EnumFacing.WEST ),
	BASIC_WEST       (EnumTier.BASIC,  3, "west"     ,  2,  0,  0, EnumFacing.EAST ),
	BASIC_NORTH_AIR  (EnumTier.BASIC, -1, "north_air",  0,  0, -1, null            ),
	BASIC_SOUTH_AIR  (EnumTier.BASIC, -1, "south_air",  0,  0,  1, null            ),
	BASIC_EAST_AIR   (EnumTier.BASIC, -1, "east_air" , -1,  0,  0, null            ),
	BASIC_WEST_AIR   (EnumTier.BASIC, -1, "west_air" ,  1,  0,  0, null            );
	
	public final EnumTier enumTier;
	public final int indexStability;
	public final String name;
	public final int x;
	public final int y;
	public final int z;
	public final EnumFacing facingLaserProperty;
	
	// cached values
	public static final int length;
	public static final int maxInstabilities;
	private static final HashMap<Integer, EnumReactorFace> ID_MAP = new HashMap<>();
	private static final HashMap<EnumTier, EnumReactorFace[]> TIER_ALL = new HashMap<>(EnumTier.length);
	private static final HashMap<EnumTier, EnumReactorFace[]> TIER_LASERS = new HashMap<>(EnumTier.length);
	
	static {
		length = EnumReactorFace.values().length;
		for (final EnumReactorFace reactorFace : values()) {
			ID_MAP.put(reactorFace.ordinal(), reactorFace);
		}
		
		// pre-build the list of lasers in the structure
		final HashMap<EnumTier, ArrayList<EnumReactorFace>> tierAll = new HashMap<>(EnumTier.length);
		final HashMap<EnumTier, ArrayList<EnumReactorFace>> tierLasers = new HashMap<>(EnumTier.length);
		for (final EnumTier tierLoop : EnumTier.values()) {
			tierAll.put(tierLoop, new ArrayList<>(16));
			tierLasers.put(tierLoop, new ArrayList<>(16));
		}
		for (final EnumReactorFace reactorFace : values()) {
			if (reactorFace.enumTier == null) {
				continue;
			}
			tierAll.get(reactorFace.enumTier).add(reactorFace);
			if (reactorFace.indexStability >= 0) {
				tierLasers.get(reactorFace.enumTier).add(reactorFace);
			}
		}
		for (final Entry<EnumTier, ArrayList<EnumReactorFace>> entry : tierAll.entrySet()) {
			TIER_ALL.put(entry.getKey(), entry.getValue().toArray(new EnumReactorFace[0]));
		}
		int max = 0;
		for (final Entry<EnumTier, ArrayList<EnumReactorFace>> entry : tierLasers.entrySet()) {
			TIER_LASERS.put(entry.getKey(), entry.getValue().toArray(new EnumReactorFace[0]));
			max = Math.max(max, entry.getValue().size());
		}
		maxInstabilities = max;
	}
	
	EnumReactorFace(final EnumTier enumTier, final int indexStability, final String name,
	                final int x, final int y, final int z,
	                final EnumFacing facingLaserProperty) {
		this.enumTier = enumTier;
		this.indexStability = indexStability;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.facingLaserProperty = facingLaserProperty;
	}
	
	public static EnumReactorFace[] get(final EnumTier tier) {
		return TIER_ALL.get(tier);
	}
	
	public static EnumReactorFace[] getLasers(final EnumTier tier) {
		return TIER_LASERS.get(tier);
	}
	
	@Nonnull
	@Override
	public String getName() {
		return name;
	}
	
	public static EnumReactorFace get(final int ordinal) {
		return ID_MAP.get(ordinal);
	}
}
