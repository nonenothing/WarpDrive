package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class RadarEcho extends Vector3 {
	
	public final String type;
	public int mass;
	public String name = "default";
	
	public RadarEcho(
			final String type,
			final double x, final double y, final double z,
			final int mass,
			final String name) {
		super(x, y, z);
		this.type = type;
		this.mass = mass;
		this.name = name;
	}
	
	public RadarEcho(
	                final String type,
	                final Vector3 vectorCoordinates,
	                final int mass,
	                final String name) {
		super(vectorCoordinates.x, vectorCoordinates.y, vectorCoordinates.z);
		this.type = type;
		this.mass = mass;
		this.name = name;
	}
	
	public RadarEcho(final StarMapRegistryItem starMapRegistryItem) {
		this(starMapRegistryItem.type.getName(),
		     starMapRegistryItem.getUniversalCoordinates(),
		     starMapRegistryItem.mass,
		     starMapRegistryItem.name );
	}
	
	// public RadarEcho(final EntityLivingBase entityLivingBase) {
	// 	this("entity",
	// 	     entityLivingBase.worldObj.provider.dimensionId,
	// 	     starMapRegistryItem.getUniversalCoordinates(),
	// 	     entityLivingBase.width * entityLivingBase.height * 4,
	// 	     entityLivingBase.getCommandSenderName() );
	// }
	
	@Override
	public String toString() {
		return String.format("%s %s @ (%d %d %d) %s %s", 
		                     getClass().getSimpleName(),
		                     type,
		                     x, y, z,
		                     mass,
		                     name);
	}
}