package cr0s.warpdrive.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemBlockAbstractBase extends ItemBlock {
	
	public ItemBlockAbstractBase(Block block) {
		super(block);	// sets field_150939_a to block
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int p_77617_1_) {
		return field_150939_a.getIcon(2, p_77617_1_);
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	public String getStatus(final NBTTagCompound nbtTagCompound) {
		TileEntity tileEntity = field_150939_a.createTileEntity(null, 0);
		if (tileEntity instanceof TileEntityAbstractEnergy) {
			if (nbtTagCompound != null) {
				tileEntity.readFromNBT(nbtTagCompound);
			}
			return ((TileEntityAbstractEnergy)tileEntity).getStatus();
			
		} else {
			return "";
		}
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (StatCollector.canTranslate(tooltipName1)) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName1));
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && StatCollector.canTranslate(tooltipName2)) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName2));
		}
		
		WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted(getStatus(itemStack.getTagCompound())));
	}
}
