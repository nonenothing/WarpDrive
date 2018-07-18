package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityShipController extends TileEntityAbstractShipController {
	
	// persistent properties
	private boolean isSynchronized = false;
	
	// computed properties
	private final int updateInterval_ticks = 20 * WarpDriveConfig.SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS;
	private int updateTicks = updateInterval_ticks;
	private int bootTicks = 20;
	
	private WeakReference<TileEntityShipCore> tileEntityShipCoreWeakReference = null;
	
	public TileEntityShipController(final EnumTier enumTier) {
		super(enumTier);
		
		peripheralName = "warpdriveShipController";
		// addMethods(new String[] {});
		CC_scripts = Collections.singletonList("startup");
	}
    
    @Override
    public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		// accelerate update ticks during boot
		if (bootTicks > 0) {
			bootTicks--;
			if (tileEntityShipCoreWeakReference == null) {
				updateTicks = 1;
			}
		}
		updateTicks--;
		if (updateTicks <= 0) {
			updateTicks = updateInterval_ticks;
			
			final TileEntityShipCore tileEntityShipCore = findCoreBlock();
			if (tileEntityShipCore != null) {
				if ( tileEntityShipCoreWeakReference == null
				  || tileEntityShipCore != tileEntityShipCoreWeakReference.get() ) {
					tileEntityShipCoreWeakReference = new WeakReference<>(tileEntityShipCore);
				}
				
				if ( !isSynchronized
				  && tileEntityShipCore.refreshLink(this) ) {
					isSynchronized = true;
					synchronizeFrom(tileEntityShipCore);
					if ( !tileEntityShipCore.isEnabled
					  && isEnabled ) {
						tileEntityShipCore.command(new Object[] { command.getName() });
						tileEntityShipCore.enable(new Object[] { true });
					}
				}
			}
			
			updateBlockState(null, BlockShipController.COMMAND, command);
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		
		return tagCompound;
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeItemDropNBT(tagCompound);
		
		return tagCompound;
	}
	
	private TileEntityShipCore findCoreBlock() {
		TileEntity tileEntity;
		
		tileEntity = world.getTileEntity(pos.add(1, 0, 0));
		if (tileEntity instanceof TileEntityShipCore) {
			return (TileEntityShipCore) tileEntity;
		}
		
		tileEntity = world.getTileEntity(pos.add(-1, 0, 0));
		if (tileEntity instanceof TileEntityShipCore) {
			return (TileEntityShipCore) tileEntity;
		}
		
		tileEntity = world.getTileEntity(pos.add(0, 0, 1));
		if (tileEntity instanceof TileEntityShipCore) {
			return (TileEntityShipCore) tileEntity;
		}
		
		tileEntity = world.getTileEntity(pos.add(0, 0, -1));
		if (tileEntity instanceof TileEntityShipCore) {
			return (TileEntityShipCore) tileEntity;
		}
		
		return null;
	}
	
	@Override
	public String getAllPlayersInArea() {
		final AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos).grow(10.0D);
		final List list = world.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		final StringBuilder stringBuilderResult = new StringBuilder();
		
		boolean isFirst = true;
		for (final Object object : list) {
			if (!(object instanceof EntityPlayer)) {
				continue;
			}
			if (isFirst) {
				isFirst = false;
			} else {
				stringBuilderResult.append(", ");
			}
			stringBuilderResult.append(((EntityPlayer) object).getName());
		}
		return stringBuilderResult.toString();
	}
	
	// Common OC/CC methods
	@Override
	public Object[] position() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return null;
		}
		return tileEntityShipCore.position();
	}
	
	@Override
	public Object[] isAssemblyValid() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return new Object[] { false, "No core detected" };
		}
		return tileEntityShipCore.isAssemblyValid();
	}
	
	@Override
	public Object[] getOrientation() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return null;
		}
		return tileEntityShipCore.getOrientation();
	}
	
	@Override
	public Object[] isInSpace() {
		return new Boolean[] { CelestialObjectManager.isInSpace(world, pos.getX(), pos.getZ()) };
	}
	
	@Override
	public Object[] isInHyperspace() {
		return new Boolean[] { CelestialObjectManager.isInHyperspace(world, pos.getX(), pos.getZ()) };
	}
	
	@Override
	public Object[] shipName(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.shipName(null); // return current local values
		}
		return new Object[] { tileEntityShipCore.shipName(arguments) };
	}
	
	@Override
	public Object[] dim_positive(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.dim_positive(null); // return current local values
		}
		return new Object[] { tileEntityShipCore.dim_positive(arguments) };
	}
	
	@Override
	public Object[] dim_negative(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.dim_negative(null); // return current local values
		}
		return new Object[] { tileEntityShipCore.dim_negative(arguments) };
	}
	
	// public Object[] command(final Object[] arguments);
	
	// public Object[] enable(final Object[] arguments);
	
	@Override
	public Object[] energy() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return null;
		}
		return tileEntityShipCore.energy();
	}
	
	@Override
	public Object[] getShipSize() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return null;
		}
		return tileEntityShipCore.getShipSize();
	}
	
	@Override
	public Object[] movement(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.movement(arguments); // return current local values
		}
		return tileEntityShipCore.movement(arguments);
	}
	
	@Override
	public Object[] getMaxJumpDistance() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return new Object[] { false, "No ship core detected" };
		}
		return tileEntityShipCore.getMaxJumpDistance();
	}
	
	@Override
	public Object[] rotationSteps(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.rotationSteps(arguments); // return current local values
		}
		return tileEntityShipCore.rotationSteps(arguments);
	}
	
	@Override
	public Object[] targetName(final Object[] arguments) {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return super.targetName(arguments); // return current local values
		}
		return tileEntityShipCore.targetName(arguments);
	}
	
	@Override
	public Object[] getEnergyRequired() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		if (tileEntityShipCore == null) {
			return new Object[] { false, "No ship core detected" };
		}
		return tileEntityShipCore.getEnergyRequired();
	}
	
	@Override
	public String toString() {
		final TileEntityShipCore tileEntityShipCore = tileEntityShipCoreWeakReference == null ? null : tileEntityShipCoreWeakReference.get();
		return String.format("%s \'%s\' %s",
		                     getClass().getSimpleName(),
		                     tileEntityShipCore == null ? "-NULL-" : tileEntityShipCore.shipName,
		                     Commons.format(world, pos.getX(), pos.getY(), pos.getZ()));
	}
}
