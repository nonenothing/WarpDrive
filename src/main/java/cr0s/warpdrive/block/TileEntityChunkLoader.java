package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.UpgradeType;
import cr0s.warpdrive.item.ItemUpgrade;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.fml.common.Optional;

public class TileEntityChunkLoader extends TileEntityAbstractChunkLoading {
	
	private boolean canLoad = false;
	private boolean shouldLoad = false;
	
	private boolean initialised = false;
	private ChunkPos myChunk;
	
	int negDX, posDX, negDZ, posDZ;
	int area = 1;
	
	public TileEntityChunkLoader() {
		super();
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		negDX = 0;
		negDZ = 0;
		posDX = 0;
		posDZ = 0;
		peripheralName = "warpdriveChunkloader";
		addMethods(new String[] {
				"radius",
				"bounds",
				"active",
				"upgrades"
		});
		
		setUpgradeMaxCount(ItemUpgrade.getItemStack(UpgradeType.Energy), 2);
		setUpgradeMaxCount(ItemUpgrade.getItemStack(UpgradeType.Power), 2);
	}
	
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.CL_MAX_ENERGY;
	}
	
	@Override
	public boolean energy_canInput(EnumFacing from) {
		return true;
	}
	
	@Override
	public boolean shouldChunkLoad()
	{
		return shouldLoad && canLoad;
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if(!initialised)
		{
			initialised = true;
			myChunk = worldObj.getChunkFromBlockCoords(pos).getChunkCoordIntPair();
			changedDistance();
		}
		
		if(shouldLoad)
		{
			canLoad = energy_consume(area * WarpDriveConfig.CL_RF_PER_CHUNKTICK, false);
		}
		else
		{
			canLoad = energy_consume(area * WarpDriveConfig.CL_RF_PER_CHUNKTICK, true);
		}
	}
	
	private void changedDistance()
	{
		if(worldObj == null) {
			return;
		}
		if (myChunk == null) {
			Chunk aChunk = worldObj.getChunkFromBlockCoords(pos);
			myChunk = aChunk.getChunkCoordIntPair();
		}
		negDX = - Commons.clamp(0, WarpDriveConfig.CL_MAX_DISTANCE, negDX);
		posDX =   Commons.clamp(0, WarpDriveConfig.CL_MAX_DISTANCE, posDX);
		negDZ = - Commons.clamp(0, WarpDriveConfig.CL_MAX_DISTANCE, negDZ);
		posDZ =   Commons.clamp(0, WarpDriveConfig.CL_MAX_DISTANCE, posDZ);
		minChunk = new ChunkPos(myChunk.chunkXPos + negDX,myChunk.chunkZPos + negDZ);
		maxChunk = new ChunkPos(myChunk.chunkXPos + posDX,myChunk.chunkZPos + posDZ);
		area = (posDX - negDX + 1) * (posDZ - negDZ + 1);
		refreshLoading(true);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		negDX  = nbt.getInteger("negDX");
		negDZ  = nbt.getInteger("negDZ");
		posDX  = nbt.getInteger("posDX");
		posDZ  = nbt.getInteger("posDZ");

		changedDistance();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tag.setInteger("negDX", negDX);
		tag.setInteger("negDZ", negDZ);
		tag.setInteger("posDX", posDX);
		tag.setInteger("posDZ", posDZ);
		return tag;
	}

	// OpenComputer callback methods
	// FIXME: implement OpenComputers...

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		switch (methodName) {
			case "radius":
				if (arguments.length == 1) {
					int dist = Commons.toInt(arguments[0]);
					negDX = dist;
					negDZ = dist;
					posDX = dist;
					posDZ = dist;
					changedDistance();
					return new Object[]{true};
				}
				return new Object[]{false};
			case "bounds":
				if (arguments.length == 4) {
					negDX = Commons.toInt(arguments[0]);
					posDX = Commons.toInt(arguments[1]);
					negDZ = Commons.toInt(arguments[2]);
					posDZ = Commons.toInt(arguments[3]);
					changedDistance();
				}
				return new Object[]{negDX, posDX, negDZ, posDZ};
			case "active":
				if (arguments.length == 1)
					shouldLoad = Commons.toBool(arguments[0]);
				return new Object[]{shouldChunkLoad()};
			case "upgrades":
				return new Object[] { getUpgradesAsString() };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
