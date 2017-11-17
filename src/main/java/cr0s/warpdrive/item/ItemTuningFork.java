package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IControlChannel;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.data.SoundEvents;

import javax.annotation.Nonnull;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTuningFork extends ItemAbstractBase implements IWarpTool {
	
	public ItemTuningFork(final String registryName) {
		super(registryName);
		setMaxDamage(0);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.tool.tuning_fork");
		setFull3D();
		setHasSubtypes(true);
	}
	
	@Override
	public void getSubItems(@Nonnull final Item item, final CreativeTabs creativeTab, List<ItemStack> list) {
		for (int dyeColor = 0; dyeColor < 16; dyeColor++) {
			list.add(new ItemStack(item, 1, dyeColor));
		}
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		ResourceLocation resourceLocation = getRegistryName();
		if (damage >= 0 && damage < 16) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "-" + EnumDyeColor.byDyeDamage(damage).getUnlocalizedName());
		}
		return new ModelResourceLocation(resourceLocation, "inventory");
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < 16) {
			return getUnlocalizedName() + "." + EnumDyeColor.byDyeDamage(damage).getUnlocalizedName();
		}
		return getUnlocalizedName();
	}
	
	public static int getVideoChannel(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return (itemStack.getItemDamage() % 16) + 100;
	}
	
	public static int getBeamFrequency(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return ((itemStack.getItemDamage() % 16) + 1) * 10;
	}
	
	public static int getControlChannel(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return ((itemStack.getItemDamage() % 16) + 2);
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
		
		boolean hasVideoChannel = tileEntity instanceof IVideoChannel;
		boolean hasBeamFrequency = tileEntity instanceof IBeamFrequency;
		boolean hasControlChannel = tileEntity instanceof IControlChannel;
		if (!hasVideoChannel && !hasBeamFrequency && !hasControlChannel) {
			return EnumActionResult.FAIL;
		}
		if (hasVideoChannel && !(entityPlayer.isSneaking() && hasBeamFrequency)) {
			((IVideoChannel)tileEntity).setVideoChannel(getVideoChannel(itemStack));
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.video_channel.set",
					tileEntity.getBlockType().getLocalizedName(),
					getVideoChannel(itemStack)));
			world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.DING, SoundCategory.PLAYERS, 0.1F, 1F, false);
			
		} else if (hasControlChannel && !(entityPlayer.isSneaking() && hasBeamFrequency)) {
			((IControlChannel)tileEntity).setControlChannel(getControlChannel(itemStack));
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.control_channel.set",
				tileEntity.getBlockType().getLocalizedName(),
				getControlChannel(itemStack)));
			world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.DING, SoundCategory.PLAYERS, 0.1F, 1F, false);
			
		} else if (hasBeamFrequency) {
			((IBeamFrequency)tileEntity).setBeamFrequency(getBeamFrequency(itemStack));
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.beam_frequency.set",
					tileEntity.getBlockType().getLocalizedName(),
					getBeamFrequency(itemStack)));
			world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.DING, SoundCategory.PLAYERS, 0.1F, 1F, false);
			
		} else {
			Commons.addChatMessage(entityPlayer, new TextComponentString("Error: invalid state, please contact the mod authors"
					+ "\nof " + itemStack
					+ "\nand " + tileEntity));
		}
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack itemStack, IBlockAccess world, BlockPos blockPos, EntityPlayer player) {
		Block block = world.getBlockState(blockPos).getBlock();
		return block instanceof BlockEnergyBank || super.doesSneakBypassUse(itemStack, world, blockPos, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip = "";
		tooltip += new TextComponentTranslation("warpdrive.video_channel.tooltip", getVideoChannel(itemStack)).getFormattedText();
		tooltip += "\n" + new TextComponentTranslation("warpdrive.beam_frequency.tooltip", getBeamFrequency(itemStack)).getFormattedText();
		tooltip += "\n" + new TextComponentTranslation("warpdrive.control_channel.tooltip", getControlChannel(itemStack)).getFormattedText();
		
		tooltip += "\n\n" + new TextComponentTranslation("item.warpdrive.tool.tuning_fork.tooltip.usage").getFormattedText();
		
		Commons.addTooltip(list, tooltip);
	}
}
