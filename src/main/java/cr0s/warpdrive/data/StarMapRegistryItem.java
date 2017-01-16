package cr0s.warpdrive.data;

import java.util.HashMap;
import java.util.UUID;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class StarMapRegistryItem extends GlobalPosition {
	public final EnumStarMapEntryType type;
	public final UUID uuid;
	public int maxX, maxY, maxZ;
	public int minX, minY, minZ;
	public int mass;
	public double isolationRate = 0.0D;
	public String name = "default";
	
	public enum EnumStarMapEntryType {
		UNDEFINED(0, "-undefined-"),
		SHIP(1, "ship"),	        	// a ship core
		JUMPGATE(2, "jumpgate"),	    // a jump gate
		PLANET(3, "planet"),		    // a planet (a transition plane allowing to move to another dimension)
		STAR(4, "star"),    		    // a star
		STRUCTURE(5, "structure"),	    // a structure from WorldGeneration (moon, asteroid field, etc.)
		WARP_ECHO(6, "warp_echo"),	    // remains of a warp
		ACCELERATOR(7, "accelerator");	// an accelerator setup
		
		private final int id;
		private final String name;
		
		// cached values
		public static final int length;
		private static final HashMap<String, EnumStarMapEntryType> mapNames = new HashMap<>();
		
		static {
			length = EnumStarMapEntryType.values().length;
			for (EnumStarMapEntryType enumStarMapEntryType : values()) {
				mapNames.put(enumStarMapEntryType.getName(), enumStarMapEntryType);
			}
		}
		
		EnumStarMapEntryType(final int id, final String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public static EnumStarMapEntryType getByName(final String name) {
			return mapNames.get(name);
		}
	}
	
	public StarMapRegistryItem(
	                          final EnumStarMapEntryType type, final UUID uuid,
	                          final int dimensionId, final int x, final int y, final int z,
	                          final AxisAlignedBB aabbArea,
	                          final int mass, final double isolationRate,
	                          final String name) {
		super(dimensionId, x, y, z);
		this.type = type;
		this.uuid = uuid;
		if (aabbArea == null) {
			this.maxX = x;
			this.maxY = y;
			this.maxZ = z;
			this.minX = x;
			this.minY = y;
			this.minZ = z;
		} else {
			this.maxX = (int) aabbArea.maxX;
			this.maxY = (int) aabbArea.maxY;
			this.maxZ = (int) aabbArea.maxZ;
			this.minX = (int) aabbArea.minX;
			this.minY = (int) aabbArea.minY;
			this.minZ = (int) aabbArea.minZ;
		}
		this.mass = mass;
		this.isolationRate = isolationRate;
		this.name = name;
	}
	
	public StarMapRegistryItem(IStarMapRegistryTileEntity tileEntity) {
		this(
			EnumStarMapEntryType.getByName(tileEntity.getStarMapType()), tileEntity.getUUID(),
			((TileEntity) tileEntity).getWorldObj().provider.dimensionId,
			((TileEntity) tileEntity).xCoord, ((TileEntity) tileEntity).yCoord, ((TileEntity) tileEntity).zCoord,
			tileEntity.getStarMapArea(),
			tileEntity.getMass(), tileEntity.getIsolationRate(),
			tileEntity.getStarMapName());
	}
	
	public boolean sameIdOrCoordinates(final IStarMapRegistryTileEntity tileEntity) {
		assert(tileEntity instanceof TileEntity);
		return uuid == tileEntity.getUUID()
			|| dimensionId == ((TileEntity) tileEntity).getWorldObj().provider.dimensionId
			&& x == ((TileEntity) tileEntity).xCoord
			&& y == ((TileEntity) tileEntity).yCoord
			&& z == ((TileEntity) tileEntity).zCoord;
	}
	
	public void update(final IStarMapRegistryTileEntity tileEntity) {
		if (WarpDrive.isDev) {
			assert (tileEntity instanceof TileEntity);
			assert (sameIdOrCoordinates(tileEntity));
		}
		AxisAlignedBB aabbArea = tileEntity.getStarMapArea();
		if (aabbArea != null) {
			maxX = (int) aabbArea.maxX;
			maxY = (int) aabbArea.maxY;
			maxZ = (int) aabbArea.maxZ;
			minX = (int) aabbArea.minX;
			minY = (int) aabbArea.minY;
			minZ = (int) aabbArea.minZ;
		}
		mass = tileEntity.getMass();
		isolationRate = tileEntity.getIsolationRate();
		name = tileEntity.getStarMapName();
	}
	
	public boolean isSameTileEntity(final IStarMapRegistryTileEntity tileEntity) {
		assert(tileEntity instanceof TileEntity);
		return dimensionId == ((TileEntity) tileEntity).getWorldObj().provider.dimensionId
		  && x == ((TileEntity) tileEntity).xCoord
		  && y == ((TileEntity) tileEntity).yCoord
		  && z == ((TileEntity) tileEntity).zCoord;
	}
	
	public boolean contains(final int x, final int y, final int z) {
		return minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ;
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 24 + (x >> 10) << 12 + y << 10 + (z >> 10);
	}
}