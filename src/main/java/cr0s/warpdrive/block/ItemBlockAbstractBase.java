package cr0s.warpdrive.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockAbstractBase extends ItemBlock {
	
	public ItemBlockAbstractBase(Block block) {
		super(block);
		setRegistryName(block.getRegistryName());
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	public ITextComponent getStatus(final NBTTagCompound nbtTagCompound) {
		TileEntity tileEntity = block.createTileEntity(Minecraft.getMinecraft().theWorld, block.getDefaultState());
		if (tileEntity instanceof TileEntityAbstractEnergy) {
			if (nbtTagCompound != null) {
				tileEntity.readFromNBT(nbtTagCompound);
			}
			return ((TileEntityAbstractEnergy)tileEntity).getStatus();
			
		} else {
			return new TextComponentString("");
		}
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
		
		WarpDrive.addTooltip(list, getStatus(itemStack.getTagCompound()).getFormattedText());
	}
}
