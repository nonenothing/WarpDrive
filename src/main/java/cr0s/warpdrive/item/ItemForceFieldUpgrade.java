package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemForceFieldUpgrade extends Item {
	private static ItemStack[] itemStackCache;
	
	public ItemForceFieldUpgrade(final String registryName) {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.upgrade");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setRegistryName(registryName);
		GameRegistry.register(this);
		
		itemStackCache = new ItemStack[EnumForceFieldUpgrade.length];
	}
	
	public static ItemStack getItemStack(EnumForceFieldUpgrade enumForceFieldUpgrade) {
		if (enumForceFieldUpgrade != null) {
			int damage = enumForceFieldUpgrade.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemForceFieldUpgrade, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumForceFieldUpgrade enumForceFieldUpgrade, int amount) {
		return new ItemStack(WarpDrive.itemForceFieldUpgrade, amount, enumForceFieldUpgrade.ordinal());
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return getUnlocalizedName() + "." + EnumForceFieldUpgrade.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public void getSubItems(@Nonnull Item item, @Nonnull CreativeTabs creativeTabs, @Nonnull List<ItemStack> subItems) {
		for(EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			if (enumForceFieldUpgrade != EnumForceFieldUpgrade.NONE) {
				subItems.add(new ItemStack(item, 1, enumForceFieldUpgrade.ordinal()));
			}
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack itemStack, IBlockAccess blockAccess, BlockPos blockPos, EntityPlayer player) {
		Block block = blockAccess.getBlockState(blockPos).getBlock();
		return block instanceof BlockForceFieldRelay || block instanceof BlockForceFieldProjector || super.doesSneakBypassUse(itemStack, blockAccess, blockPos, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (I18n.canTranslate(tooltipName1)) {
			WarpDrive.addTooltip(list, new TextComponentTranslation(tooltipName1).getFormattedText());
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && I18n.canTranslate(tooltipName2)) {
			WarpDrive.addTooltip(list, new TextComponentTranslation(tooltipName2).getFormattedText());
		}
		
		WarpDrive.addTooltip(list, "\n");
		
		EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStack.getItemDamage());
		if (enumForceFieldUpgrade.maxCountOnProjector > 0) {
			WarpDrive.addTooltip(list, new TextComponentTranslation("item.warpdrive.forcefield.upgrade.tooltip.usage.projector").getFormattedText());
		}
		if (enumForceFieldUpgrade.maxCountOnRelay > 0) {
			WarpDrive.addTooltip(list, new TextComponentTranslation("item.warpdrive.forcefield.upgrade.tooltip.usage.relay").getFormattedText());
		}
		WarpDrive.addTooltip(list, new TextComponentTranslation("item.warpdrive.forcefield.upgrade.tooltip.usage.dismount").getFormattedText());
	}
}
