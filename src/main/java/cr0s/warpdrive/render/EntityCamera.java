package cr0s.warpdrive.render;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.network.PacketHandler;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class EntityCamera extends EntityLivingBase {
	// entity coordinates (x, y, z) are dynamically changed by player
	
	// camera block coordinates are fixed
	public int cameraX;
	public int cameraY;
	public int cameraZ;
	
	private EntityPlayer player;
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private int dx = 0, dy = 0, dz = 0;
	
	private int closeWaitTicks = 0;
	private int zoomWaitTicks = 0;
	private int fireWaitTicks = 0;
	private boolean isActive = true;
	private int bootUpTicks = 20;
	
	private boolean isCentered = true;
	
	public EntityCamera(final World world, final int x, final int y, final int z, final EntityPlayer player) {
		super(world);
		posX = x;
		posY = y;
		posZ = z;
		cameraX = x;
		cameraY = y;
		cameraZ = z;
		this.player = player;
	}
		
	@Override
	protected void entityInit() {
		super.entityInit();
		setInvisible(true);
		// set viewpoint inside camera
		yOffset = 1.62F;
		noClip = true;
	}
	
	// override to skip the block bounding override on client side
	@Override
	public void setPositionAndRotation2(final double x, final double y, final double z, final float yaw, final float pitch, final int p_70056_9_) {
		//	super.setPositionAndRotation2(x, y, z, yaw, pitch, p_70056_9_);
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}
	
	private void closeCamera() {
		if (!isActive) {
			return;
		}
		
		ClientCameraHandler.resetViewpoint();
		worldObj.removeEntity(this);
		isActive = false;
	}
	
	@Override
	public void onEntityUpdate() {
		if (worldObj.isRemote) {
			if (player == null || player.isDead) {
				WarpDrive.logger.error(this + " Player is null or dead, closing camera...");
				closeCamera();
				return;
			}
			if (!ClientCameraHandler.isValidContext(worldObj)) {
				WarpDrive.logger.error(this + " Invalid context, closing camera...");
				closeCamera();
				return;
			}
			
			final Block block = worldObj.getBlock(cameraX, cameraY, cameraZ);
			mc.renderViewEntity.rotationYaw = player.rotationYaw;
			// mc.renderViewEntity.rotationYawHead = player.rotationYawHead;
			mc.renderViewEntity.rotationPitch = player.rotationPitch;
			
			ClientCameraHandler.overlayLoggingMessage = "Mouse " + Mouse.isButtonDown(0) + " " + Mouse.isButtonDown(1) + " " + Mouse.isButtonDown(2) + " " + Mouse.isButtonDown(3) + "\nBackspace "
					+ Keyboard.isKeyDown(Keyboard.KEY_BACKSLASH) + " Space " + Keyboard.isKeyDown(Keyboard.KEY_SPACE) + " Shift " + "";
			// Perform zoom
			if (Mouse.isButtonDown(0)) {
				zoomWaitTicks++;
				if (zoomWaitTicks >= 2) {
					zoomWaitTicks = 0;
					ClientCameraHandler.zoom();
				}
			} else {
				zoomWaitTicks = 0;
			}
			
			if (bootUpTicks > 0) {
				bootUpTicks--;
			} else {
				if (Mouse.isButtonDown(1)) {
					closeWaitTicks++;
					if (closeWaitTicks >= 2) {
						closeWaitTicks = 0;
						closeCamera();
					}
				} else {
					closeWaitTicks = 0;
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				fireWaitTicks++;
				if (fireWaitTicks >= 2) {
					fireWaitTicks = 0;
					
					// Make a shoot with camera-laser
					if (block.isAssociatedBlock(WarpDrive.blockLaserCamera)) {
						PacketHandler.sendLaserTargetingPacket(cameraX, cameraY, cameraZ, mc.renderViewEntity.rotationYaw, mc.renderViewEntity.rotationPitch);
					}
				}
			} else {
				fireWaitTicks = 0;
			}
			
			final GameSettings gamesettings = mc.gameSettings;
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				dy = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				dy = 2;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindLeft.getKeyCode())) {
				dz = -1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindRight.getKeyCode())) {
				dz = 1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindForward.getKeyCode())) {
				dx = 1;
			} else if (Keyboard.isKeyDown(gamesettings.keyBindBack.getKeyCode())) {
				dx = -1;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) { // centering view
				dx = 0;
				dy = 0;
				dz = 0;
				isCentered = !isCentered;
				return;
			}
			
			if (isCentered) {
				setPosition(cameraX + 0.5D, cameraY + 0.5D, cameraZ + 0.5D);
			} else {
				setPosition(cameraX + dx, cameraY + dy, cameraZ + dz);
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		motionX = motionY = motionZ = 0.0D;
	}
	
	@Override
	public boolean shouldRenderInPass(final int pass) {
		return false;
	}
	
	/*
	// Item no clip
	@Override
	protected boolean func_145771_j(double par1, double par3, double par5) {
		// Clipping is fine, don't move me
		return false;
	}
	/**/
	
	@Override
	public AxisAlignedBB getBoundingBox() {
		return null;
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public void moveEntity(final double x, final double y, final double z) {
	}
	
	@Override
	public void readEntityFromNBT(final NBTTagCompound tagCompound) {
		// nothing to save, skip ancestor call
		cameraX = tagCompound.getInteger("x");
		cameraY = tagCompound.getInteger("y");
		cameraZ = tagCompound.getInteger("z");
	}
	
	@Override
	public void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
		// nothing to save, skip ancestor call
		nbttagcompound.setInteger("x", cameraX);
		nbttagcompound.setInteger("y", cameraY);
		nbttagcompound.setInteger("z", cameraZ);
	}
	
	@Override
	public ItemStack getHeldItem() {
		return null;
	}
	
	@Override
	public void setCurrentItemOrArmor(final int i, final ItemStack itemStack) {
	}
	
	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}
	
	@Override
	public ItemStack getEquipmentInSlot(final int i) {
		return null;
	}
}