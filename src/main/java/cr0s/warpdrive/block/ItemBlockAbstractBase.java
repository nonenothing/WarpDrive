package cr0s.warpdrive.block;

import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.api.IItemBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockAbstractBase extends ItemBlock implements IItemBase {
	
	public ItemBlockAbstractBase(Block block) {
		super(block);
		if (block instanceof BlockAbstractContainer) {
			setHasSubtypes(((BlockAbstractContainer) block).hasSubBlocks);
		}
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		if ( itemStack == null 
		  || !(block instanceof BlockAbstractContainer)
		  || !((BlockAbstractContainer) block).hasSubBlocks ) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + itemStack.getItemDamage();
	}
	
	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		if ( itemStack == null
		  || !(block instanceof BlockAbstractContainer) ) {
			return super.getRarity(itemStack);
		}
		return ((BlockAbstractContainer) block).getRarity(itemStack, super.getRarity(itemStack));
	}
	
	public ITextComponent getStatus(final NBTTagCompound nbtTagCompound, final IBlockState blockState) {
		TileEntity tileEntity = block.createTileEntity(Minecraft.getMinecraft().theWorld, blockState);
		if (tileEntity instanceof TileEntityAbstractBase) {
			if (nbtTagCompound != null) {
				tileEntity.readFromNBT(nbtTagCompound);
			}
			return ((TileEntityAbstractBase) tileEntity).getStatus();
			
		} else {
			return new TextComponentString("");
		}
	}
	
	@Nonnull
	@Override
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		return ClientProxy.getModelResourceLocation(itemStack);
	}
	
	@Override
	public void addInformation(@Nonnull ItemStack itemStack, @Nonnull EntityPlayer entityPlayer, @Nonnull List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (I18n.hasKey(tooltipName1)) {
			WarpDrive.addTooltip(list, new TextComponentTranslation(tooltipName1).getFormattedText());
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && net.minecraft.client.resources.I18n.hasKey(tooltipName2)) {
			WarpDrive.addTooltip(list, new TextComponentTranslation(tooltipName2).getFormattedText());
		}
		IBlockState blockState = block.getStateFromMeta(itemStack.getMetadata());   // @TODO: integrate tooltips on tile entities
		WarpDrive.addTooltip(list, getStatus(itemStack.getTagCompound(), blockState).getFormattedText());
	}
}
