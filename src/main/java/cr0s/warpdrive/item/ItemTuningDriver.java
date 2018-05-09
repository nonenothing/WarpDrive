package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IControlChannel;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.data.SoundEvents;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ItemTuningDriver extends ItemAbstractBase implements IWarpTool {
	
	public static final int MODE_VIDEO_CHANNEL = 0;
	public static final int MODE_BEAM_FREQUENCY = 1;
	public static final int MODE_CONTROL_CHANNEL = 2;
	
	public ItemTuningDriver(final String registryName) {
		super(registryName);
		setMaxDamage(0);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.tool.tuning_driver");
		setFull3D();
	}
	
	// @TODO MC1.10 rendering
	//	icons[MODE_VIDEO_CHANNEL  ] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-cyan");
	//	icons[MODE_BEAM_FREQUENCY ] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-purple");
	//	icons[MODE_CONTROL_CHANNEL] = iconRegister.registerIcon("warpdrive:tool/tuning_driver-yellow");
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
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
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull final ItemStack itemStack, final World world, final EntityPlayer entityPlayer, final EnumHand hand) {
		if ( world.isRemote
		  || !(itemStack.getItem() instanceof ItemTuningDriver) ) {
			return new ActionResult<>(EnumActionResult.PASS, itemStack);
		}
		// check if a block is in players reach 
		final RayTraceResult movingObjectPosition = Commons.getInteractingBlock(world, entityPlayer);
		if (movingObjectPosition.typeOfHit != Type.MISS) {
			return new ActionResult<>(EnumActionResult.PASS, itemStack);
		}
		
		if (entityPlayer.isSneaking() && entityPlayer.capabilities.isCreativeMode) {
			switch (itemStack.getItemDamage()) {
			case MODE_VIDEO_CHANNEL:
				setVideoChannel(itemStack, world.rand.nextInt(IVideoChannel.VIDEO_CHANNEL_MAX));
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.video_channel.get",
					entityPlayer.getName(),
					getVideoChannel(itemStack)));
				return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
			
			case MODE_BEAM_FREQUENCY:
				setBeamFrequency(itemStack, world.rand.nextInt(IBeamFrequency.BEAM_FREQUENCY_MAX));
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.beam_frequency.get",
					entityPlayer.getName(),
					getBeamFrequency(itemStack)));
				return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
			
			case MODE_CONTROL_CHANNEL:
				setControlChannel(itemStack, world.rand.nextInt(IControlChannel.CONTROL_CHANNEL_MAX));
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.control_channel.get",
					entityPlayer.getName(),
					getControlChannel(itemStack)));
				return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
			
			default:
				return new ActionResult<>(EnumActionResult.PASS, itemStack);
			}
			
		} else {
			switch (itemStack.getItemDamage()) {
			case MODE_VIDEO_CHANNEL:
				itemStack.setItemDamage(MODE_BEAM_FREQUENCY);
				entityPlayer.setHeldItem(hand, itemStack);
				break;
			
			case MODE_BEAM_FREQUENCY:
				itemStack.setItemDamage(MODE_CONTROL_CHANNEL);
				entityPlayer.setHeldItem(hand, itemStack);
				break;
			
			case MODE_CONTROL_CHANNEL:
				itemStack.setItemDamage(MODE_VIDEO_CHANNEL);
				entityPlayer.setHeldItem(hand, itemStack);
				break;
			
			default:
				itemStack.setItemDamage(MODE_VIDEO_CHANNEL);
				break;
			}
			world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.DING, SoundCategory.PLAYERS, 0.1F, 1F, false);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
		}
	}
	
	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, BlockPos blockPos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return EnumActionResult.FAIL;
		}
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity == null) {
			return EnumActionResult.FAIL;
		}
		
		switch (itemStack.getItemDamage()) {
		case MODE_VIDEO_CHANNEL:
			if (tileEntity instanceof IVideoChannel) {
				if (entityPlayer.isSneaking()) {
					setVideoChannel(itemStack, ((IVideoChannel) tileEntity).getVideoChannel());
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.video_channel.get",
							tileEntity.getBlockType().getLocalizedName(),
							getVideoChannel(itemStack)));
				} else {
					((IVideoChannel) tileEntity).setVideoChannel(getVideoChannel(itemStack));
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.video_channel.set",
							tileEntity.getBlockType().getLocalizedName(),
							getVideoChannel(itemStack)));
				}
				return EnumActionResult.SUCCESS;
			}
			return EnumActionResult.FAIL;
			
		case MODE_BEAM_FREQUENCY:
			if (tileEntity instanceof IBeamFrequency) {
				if (entityPlayer.isSneaking()) {
					setBeamFrequency(itemStack, ((IBeamFrequency) tileEntity).getBeamFrequency());
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.beam_frequency.get",
							tileEntity.getBlockType().getLocalizedName(),
							getBeamFrequency(itemStack)));
				} else {
					((IBeamFrequency) tileEntity).setBeamFrequency(getBeamFrequency(itemStack));
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.beam_frequency.set",
							tileEntity.getBlockType().getLocalizedName(),
							getBeamFrequency(itemStack)));
				}
				return EnumActionResult.SUCCESS;
			}
			return EnumActionResult.FAIL;
		
		case MODE_CONTROL_CHANNEL:
			if (tileEntity instanceof IControlChannel) {
				if (entityPlayer.isSneaking()) {
					setControlChannel(itemStack, ((IControlChannel) tileEntity).getControlChannel());
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.control_channel.get",
							tileEntity.getBlockType().getLocalizedName(),
							getControlChannel(itemStack)));
				} else {
					((IControlChannel) tileEntity).setControlChannel(getControlChannel(itemStack));
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.control_channel.set",
							tileEntity.getBlockType().getLocalizedName(),
							getControlChannel(itemStack)));
				}
				return EnumActionResult.SUCCESS;
			}
			return EnumActionResult.FAIL;
		
		default:
			return EnumActionResult.FAIL;
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack itemStack, IBlockAccess world, BlockPos blockPos, EntityPlayer player) {
		Block block = world.getBlockState(blockPos).getBlock();
		return block instanceof BlockEnergyBank || super.doesSneakBypassUse(itemStack, world, blockPos, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip;
		switch (itemStack.getItemDamage()) {
		case MODE_VIDEO_CHANNEL:
			tooltip = new TextComponentTranslation("warpdrive.video_channel.tooltip", getVideoChannel(itemStack)).getFormattedText();
			break;
		case MODE_BEAM_FREQUENCY:
			tooltip = new TextComponentTranslation("warpdrive.beam_frequency.tooltip", getBeamFrequency(itemStack)).getFormattedText();
			break;
		case MODE_CONTROL_CHANNEL:
			tooltip = new TextComponentTranslation("warpdrive.control_channel.tooltip", getControlChannel(itemStack)).getFormattedText();
			break;
		default:
			tooltip = "I'm broken :(";
			break;
		}
		
		tooltip += "\n" + new TextComponentTranslation("item.warpdrive.tool.tuning_driver.tooltip.usage");
		
		Commons.addTooltip(list, tooltip);
	}
}
