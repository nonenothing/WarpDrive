package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IItemBase;
import cr0s.warpdrive.client.ClientProxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemAbstractBase extends Item implements IItemBase {
	
	public ItemAbstractBase(final String registryName) {
		super();
		setRegistryName(registryName);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		GameRegistry.register(this);
	}
	
	@Override
	public void onEntityExpireEvent(final EntityItem entityItem, final ItemStack itemStack) {
	}
	
	@Override
	@Nonnull
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		return ClientProxy.getModelResourceLocation(itemStack);
	}
	
	@Override
	public void addInformation(final ItemStack itemStack, final EntityPlayer entityPlayer,
	                           final List<String> list, final boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		final String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (I18n.hasKey(tooltipName1)) {
			Commons.addTooltip(list, new TextComponentTranslation(tooltipName1).getFormattedText());
		}
		
		final String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && I18n.hasKey(tooltipName2)) {
			Commons.addTooltip(list, new TextComponentTranslation(tooltipName2).getFormattedText());
		}
	}
}
