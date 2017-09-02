package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class ItemShipToken extends ItemAbstractBase {	
	
	private static ItemStack[] itemStackCache;
	private static final int[] VALID_METADATAS = { 0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 45 };
	
	public ItemShipToken(final String registryName) {
		super(registryName);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.tool.ship_token");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[VALID_METADATAS.length];
	}
	
	public static ItemStack getItemStack(final int metadataWanted) {
		for (int index = 0; index < VALID_METADATAS.length; index++) {
			if (metadataWanted == VALID_METADATAS[index]) {
				if (itemStackCache[index] == null) {
					itemStackCache[index] = new ItemStack(WarpDrive.itemShipToken, 1, metadataWanted);
				}
				return itemStackCache[index];
			}
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final int metadataWanted, final int amount) {
		for (final int metadataValid : VALID_METADATAS) {
			if (metadataWanted == metadataValid) {
				return new ItemStack(WarpDrive.itemShipToken, amount, metadataWanted);
			}
		}
		return new ItemStack(WarpDrive.itemShipToken, amount, 0);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		final int metadata = itemStack.getItemDamage();
		for (final int metadataValid : VALID_METADATAS) {
			if (metadata == metadataValid) {
				return "item.warpdrive.tool.ship_token" + metadata;
			}
		}
		return getUnlocalizedName();
	}
	
	@Override
	public void getSubItems(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		for (final int metadataValid : VALID_METADATAS) {
			list.add(new ItemStack(item, 1, metadataValid));
		}
	}
	
	public static String getSchematicName(final ItemStack itemStack) {
		String schematicName = "" + itemStack.getItemDamage();
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound != null && tagCompound.hasKey("shipName")) {
			schematicName = tagCompound.getString("shipName");
		}
		return schematicName;
	}
	
	public static void setSchematicName(final ItemStack itemStack, final String schematicName) {
		if (!itemStack.hasTagCompound()) {
			itemStack.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		tagCompound.setString("shipName", schematicName);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		final String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (I18n.hasKey(tooltipName1)) {
			Commons.addTooltip(list, new TextComponentTranslation(tooltipName1, getSchematicName(itemStack)).getFormattedText());
		}
		
		final String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && I18n.hasKey(tooltipName2)) {
			Commons.addTooltip(list, new TextComponentTranslation(tooltipName2, getSchematicName(itemStack)).getFormattedText());
		}
	}
}