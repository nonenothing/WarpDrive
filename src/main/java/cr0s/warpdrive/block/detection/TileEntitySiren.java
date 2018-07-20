package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.block.TileEntityAbstractBase;
import cr0s.warpdrive.client.SirenSound;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.SoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySiren extends TileEntityAbstractBase {
	
	public enum EnumSirenState {
		STARTING, STARTED, STOPPING, STOPPED
	}
	
	// persistent properties
	// (none)
	
	// computed properties
	private EnumSirenState state = EnumSirenState.STOPPED;
	private boolean isIndustrial = false;
	private float range = 0.0F;
	private int timeToLastUpdate = 0;
	
	@SideOnly(Side.CLIENT)
	private SirenSound sound;
	
	public TileEntitySiren() {
		super();
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		
		range = WarpDriveConfig.SIREN_RANGE_BLOCKS_BY_TIER[enumTier.getIndex()];
		
		final IBlockState blockState = world.getBlockState(pos);
		isIndustrial = ((BlockSiren) blockState.getBlock()).getIsIndustrial();
	}
	
	@Override
	public void update() {
		super.update();
		
		/* Updating the sound too quickly breaks Minecraft's sounds handler.
		 * Therefore, we only update our sound once every 0.5 seconds.
		 * It's less responsive like this, but doesn't completely freak out when
		 * spamming the redstone on and off. */
		
		if (this.timeToLastUpdate <= 0) {
			this.timeToLastUpdate = 10;
		} else {
			this.timeToLastUpdate--;
			return;
		}
        
		if (!hasWorld() || !world.isRemote) {
		    return;
        }
		if (sound == null) {
		    setSound();
        }

		// Siren sound logic.
		switch (this.state) {
			case STOPPED:
				if (this.isPlaying()) {
					this.state = EnumSirenState.STOPPING;
				}
				if (this.isPowered()) {
					this.state = EnumSirenState.STARTING;
				}
				break;
            
			case STARTING:
				if (this.startSound()) {
					this.state = EnumSirenState.STARTED;
				} else {
					this.state = EnumSirenState.STOPPING;
				}
				break;
            
			case STARTED:
				if (!this.isPowered()) {
					this.state = EnumSirenState.STOPPING;
				} else if (!this.isPlaying()) {
					this.state = EnumSirenState.STARTING;
				}
				break;
            
			case STOPPING:
				if (this.isPlaying()) {
					this.stopSound();
				} else {
					this.state = EnumSirenState.STOPPED;
				}
				break;
            
			default:
				if (this.isPlaying()) {
					this.state = EnumSirenState.STOPPING;
				} else {
					this.state = EnumSirenState.STOPPED;
				}
				break;
		}
	}
    
	// Stops the siren when the chunk is unloaded.
	@Override
	public void onChunkUnload() {
		if (world.isRemote && this.isPlaying()) {
		    stopSound();
        }
		super.onChunkUnload();
	}
    
	// Stops the siren when the TileEntity object is invalidated.
	@Override
	public void invalidate() {
		if (world.isRemote && isPlaying()) {
		    stopSound();
        }
		super.invalidate();
	}
    
	// Create a new SirenSound object that the siren will use.
	@SideOnly(Side.CLIENT)
	private void setSound() {
		sound = new SirenSound(isIndustrial ? SoundEvents.SIREN_INDUSTRIAL : SoundEvents.SIREN_RAID, range, pos.getX(), pos.getY(), pos.getZ());
	}
    
	// Forces the siren to start playing its sound;
	@SideOnly(Side.CLIENT)
    private boolean startSound() {
		if (!isPlaying()) {
			try {
				Minecraft.getMinecraft().getSoundHandler().playSound(sound);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		} else {
			return true;
		}
	}
    
	// Forces the siren to stop playing its sound.
	@SideOnly(Side.CLIENT)
	void stopSound() {
		Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
	}
    
	// Checks if the siren is currently playing its sound.
	@SideOnly(Side.CLIENT)
	boolean isPlaying() {
		return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound);
	}
    
	// Checks if the siren is being powered by redstone.
	private boolean isPowered() {
		return world.isBlockIndirectlyGettingPowered(pos) > 0;
	}
}
