package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IControlChannel;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.energy.BlockEnergyBank;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemTuningDriver extends ItemAbstractBase implements IWarpTool {
	
	public static final int MODE_VIDEO_CHANNEL = 0;
	public static final int MODE_BEAM_FREQUENCY = 1;
	public static final int MODE_CONTROL_CHANNEL = 2;
	
	@SideOnly(Side.CLIENT)
	private IIcon icons[];
	
	public ItemTuningDriver() {
		super();
		setMaxDamage(0);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.tool.tuning_driver");
		setFull3D();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(final IIconRegister iconRegister) {
		icons = new IIcon[3];
		
		icons[MODE_VIDEO_CHANNEL  ] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-cyan");
		icons[MODE_BEAM_FREQUENCY ] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-purple");
		icons[MODE_CONTROL_CHANNEL] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-yellow");
	}
	
	@Override
	public IIcon getIconFromDamage(final int damage) {
		if (damage < icons.length) {
			return icons[damage];
		}
		return Blocks.fire.getFireIcon(0);
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		switch (damage) {
		case MODE_VIDEO_CHANNEL  : return getUnlocalizedName() + ".video_channel";
		case MODE_BEAM_FREQUENCY : return getUnlocalizedName() + ".beam_frequency";
		case MODE_CONTROL_CHANNEL: return getUnlocalizedName() + ".control_channel";
		default: return getUnlocalizedName(); 
		}
	}
	
	public static int getVideoChannel(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver)) {
			return -1;
		}
		if (!itemStack.hasTagCompound()) {
			return -1;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound.hasKey(IVideoChannel.VIDEO_CHANNEL_TAG)) {
			return tagCompound.getInteger(IVideoChannel.VIDEO_CHANNEL_TAG);
		}
		return -1;
	}
	
	public static ItemStack setVideoChannel(final ItemStack itemStack, final int videoChannel) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver) || videoChannel == -1) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		tagCompound.setInteger(IVideoChannel.VIDEO_CHANNEL_TAG, videoChannel);
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	public static int getBeamFrequency(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver)) {
			return -1;
		}
		if (!itemStack.hasTagCompound()) {
			return -1;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound.hasKey(IBeamFrequency.BEAM_FREQUENCY_TAG)) {
			return tagCompound.getInteger(IBeamFrequency.BEAM_FREQUENCY_TAG);
		}
		return -1;
	}
	
	public static ItemStack setBeamFrequency(final ItemStack itemStack, final int beamFrequency) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver) || beamFrequency == -1) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		tagCompound.setInteger(IBeamFrequency.BEAM_FREQUENCY_TAG, beamFrequency);
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	public static int getControlChannel(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver)) {
			return -1;
		}
		if (!itemStack.hasTagCompound()) {
			return -1;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound.hasKey(IControlChannel.CONTROL_CHANNEL_TAG)) {
			return tagCompound.getInteger(IControlChannel.CONTROL_CHANNEL_TAG);
		}
		return -1;
	}
	
	public static ItemStack setControlChannel(final ItemStack itemStack, final int controlChannel) {
		if (!(itemStack.getItem() instanceof ItemTuningDriver) || controlChannel == -1) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		tagCompound.setInteger(IControlChannel.CONTROL_CHANNEL_TAG, controlChannel);
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	public static ItemStack setValue(final ItemStack itemStack, final int dye) {
		switch (itemStack.getItemDamage()) {
		case MODE_VIDEO_CHANNEL  : return setVideoChannel(itemStack, dye);
		case MODE_BEAM_FREQUENCY : return setBeamFrequency(itemStack, dye);
		case MODE_CONTROL_CHANNEL: return setControlChannel(itemStack, dye);
		default                  : return itemStack;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(final ItemStack itemStack, final World world, final EntityPlayer entityPlayer) {
		if ( world.isRemote
		  || !(itemStack.getItem() instanceof ItemTuningDriver) ) {
			return itemStack;
		}
		// check if a block is in players reach 
		final MovingObjectPosition movingObjectPosition = Commons.getInteractingBlock(world, entityPlayer);
		if (movingObjectPosition.typeOfHit != MovingObjectType.MISS) {
			return itemStack;
		}
		
		if (entityPlayer.isSneaking() && entityPlayer.capabilities.isCreativeMode) {
			switch (itemStack.getItemDamage()) {
			case MODE_VIDEO_CHANNEL:
				setVideoChannel(itemStack, world.rand.nextInt(IVideoChannel.VIDEO_CHANNEL_MAX));
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.video_channel.get",
					entityPlayer.getCommandSenderName(),
					getVideoChannel(itemStack)));
				return itemStack;
			
			case MODE_BEAM_FREQUENCY:
				setBeamFrequency(itemStack, world.rand.nextInt(IBeamFrequency.BEAM_FREQUENCY_MAX));
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.get",
					entityPlayer.getCommandSenderName(),
					getBeamFrequency(itemStack)));
				return itemStack;
			
			case MODE_CONTROL_CHANNEL:
				setControlChannel(itemStack, world.rand.nextInt(IControlChannel.CONTROL_CHANNEL_MAX));
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.control_channel.get",
					entityPlayer.getCommandSenderName(),
					getControlChannel(itemStack)));
				return itemStack;
			
			default:
				return itemStack;
			}
			
		} else {
			switch (itemStack.getItemDamage()) {
			case MODE_VIDEO_CHANNEL:
				itemStack.setItemDamage(MODE_BEAM_FREQUENCY);
				entityPlayer.setCurrentItemOrArmor(0, itemStack);
				break;
			
			case MODE_BEAM_FREQUENCY:
				itemStack.setItemDamage(MODE_CONTROL_CHANNEL);
				entityPlayer.setCurrentItemOrArmor(0, itemStack);
				break;
			
			case MODE_CONTROL_CHANNEL:
				itemStack.setItemDamage(MODE_VIDEO_CHANNEL);
				entityPlayer.setCurrentItemOrArmor(0, itemStack);
				break;
			
			default:
				itemStack.setItemDamage(MODE_VIDEO_CHANNEL);
				break;
			}
			world.playSoundAtEntity(entityPlayer, "WarpDrive:ding", 0.1F, 1F);
			return itemStack;
		}
	}
	
	@Override
	public boolean onItemUse(final ItemStack itemStack, final EntityPlayer entityPlayer,
	                         final World world, final int x, final int y, final int z,
	                         final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null) {
			return false;
		}
		
		switch (itemStack.getItemDamage()) {
		case MODE_VIDEO_CHANNEL:
			if (tileEntity instanceof IVideoChannel) {
				if (entityPlayer.isSneaking()) {
					setVideoChannel(itemStack, ((IVideoChannel) tileEntity).getVideoChannel());
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.video_channel.get",
							tileEntity.getBlockType().getLocalizedName(),
							getVideoChannel(itemStack)));
				} else {
					((IVideoChannel) tileEntity).setVideoChannel(getVideoChannel(itemStack));
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.video_channel.set",
							tileEntity.getBlockType().getLocalizedName(),
							getVideoChannel(itemStack)));
				}
				return true;
			}
			return false;
			
		case MODE_BEAM_FREQUENCY:
			if (tileEntity instanceof IBeamFrequency) {
				if (entityPlayer.isSneaking()) {
					setBeamFrequency(itemStack, ((IBeamFrequency) tileEntity).getBeamFrequency());
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.get",
							tileEntity.getBlockType().getLocalizedName(),
							getBeamFrequency(itemStack)));
				} else {
					((IBeamFrequency) tileEntity).setBeamFrequency(getBeamFrequency(itemStack));
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.set",
							tileEntity.getBlockType().getLocalizedName(),
							getBeamFrequency(itemStack)));
				}
				return true;
			}
			return false;
		
		case MODE_CONTROL_CHANNEL:
			if (tileEntity instanceof IControlChannel) {
				if (entityPlayer.isSneaking()) {
					setControlChannel(itemStack, ((IControlChannel) tileEntity).getControlChannel());
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.control_channel.get",
							tileEntity.getBlockType().getLocalizedName(),
							getControlChannel(itemStack)));
				} else {
					((IControlChannel) tileEntity).setControlChannel(getControlChannel(itemStack));
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.control_channel.set",
							tileEntity.getBlockType().getLocalizedName(),
							getControlChannel(itemStack)));
				}
				return true;
			}
			return false;
		
		default:
			return false;
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer) {
		final Block block = world.getBlock(x, y, z);
		return block instanceof BlockEnergyBank || super.doesSneakBypassUse(world, x, y, z, entityPlayer);
	}
	
	@Override
	public void addInformation(final ItemStack itemStack, final EntityPlayer entityPlayer, final List list, final boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip;
		switch (itemStack.getItemDamage()) {
		case MODE_VIDEO_CHANNEL:
			tooltip = StatCollector.translateToLocalFormatted("warpdrive.video_channel.tooltip", getVideoChannel(itemStack));
			break;
		case MODE_BEAM_FREQUENCY:
			tooltip = StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.tooltip", getBeamFrequency(itemStack));
			break;
		case MODE_CONTROL_CHANNEL:
			tooltip = StatCollector.translateToLocalFormatted("warpdrive.control_channel.tooltip", getControlChannel(itemStack));
			break;
		default:
			tooltip = "I'm broken :(";
			break;
		}
		
		tooltip += "\n" + StatCollector.translateToLocal("item.warpdrive.tool.tuning_driver.tooltip.usage");
		
		Commons.addTooltip(list, tooltip);
	}
}
