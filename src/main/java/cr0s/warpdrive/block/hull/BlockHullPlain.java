package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumHullPlainType;
import cr0s.warpdrive.data.Vector3;

import java.util.List;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHullPlain extends BlockAbstractBase implements IDamageReceiver {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	final byte tier;
	final EnumHullPlainType enumHullPlainType;
	
	public BlockHullPlain(final byte tier, final EnumHullPlainType enumHullPlainType) {
		super(Material.rock);
		this.tier = tier;
		this.enumHullPlainType = enumHullPlainType;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setBlockName("warpdrive.hull" + tier + ".plain.");
		setBlockTextureName("warpdrive:hull/" + enumHullPlainType.getName());
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return icons[metadata % 16];
	}
	
	@Override
	public int damageDropped(final int p_149692_1_) {
		return p_149692_1_;
	}
	
	public static String getDyeColorName(final int metadata) {
		return ItemDye.field_150921_b[~metadata & 15];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(final Item item, final CreativeTabs creativeTab, final List list) {
		for (int i = 0; i < 16; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[16];
		
		for (int i = 0; i < 16; ++i) {
			icons[i] = iconRegister.registerIcon(getTextureName() + "-" + getDyeColorName(i));
		}
	}
	
	@Override
	public MapColor getMapColor(final int metadata) {
		return MapColor.getMapColorForBlockColored(metadata);
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public float getBlockHardness(final World world, final int x, final int y, final int z,
	                              final DamageSource damageSource, final int damageParameter, final Vector3 damageDirection, final int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(final World world, final int x, final int y, final int z,
	                       final DamageSource damageSource, final int damageParameter, final Vector3 damageDirection, final int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(x, y, z);
		} else {
			final int metadata = world.getBlockMetadata(x, y, z);
			world.setBlock(x, y, z, WarpDrive.blockHulls_plain[tier - 2][enumHullPlainType.ordinal()], metadata, 2);
		}
		return 0;
	}
}
