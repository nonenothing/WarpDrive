package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.block.BlockAbstractOmnipanel;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import java.util.List;

import net.minecraft.block.Block;
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

public class BlockHullOmnipanel extends BlockAbstractOmnipanel implements IDamageReceiver {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	final byte tier;
	
	public BlockHullOmnipanel(final byte tier) {
		super(Material.glass);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setStepSound(Block.soundTypeGlass);
		setBlockName("warpdrive.hull" + tier + ".omnipanel.");
		setBlockTextureName("warpdrive:hull/glass");
		setLightLevel(10.0F / 15.0F);
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(final int side, final int metadata) {
		return icons[metadata % 16];
	}
	
	@Override
	public int damageDropped(final int metadata) {
		return metadata;
	}
	
	public static String getDyeColorName(final int metadata) {
		return ItemDye.field_150921_b[~metadata & 15];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		for (int i = 0; i < 16; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[16];
		
		for (int i = 0; i < 16; i++) {
			icons[i] = iconRegister.registerIcon(getTextureName() + "_" + getDyeColorName(i));
		}
	}
	
	@Override
	public MapColor getMapColor(int metadata) {
		return MapColor.getMapColorForBlockColored(metadata);
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z, DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(World world, int x, int y, int z, DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(x, y, z);
		} else {
			int metadata = world.getBlockMetadata(x, y, z);
			world.setBlock(x, y, z, WarpDrive.blockHulls_omnipanel[tier - 2], metadata, 2);
		}
		return 0;
	}
}