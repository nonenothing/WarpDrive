package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockHullSlab extends BlockSlab implements IBlockBase, IDamageReceiver {
	
	// Metadata values are
	// 0-5 for plain slabs orientations
	// 6-11 for tiled slabs orientations
	// 12 for plain double slab
	// 13-15 for tiled double slabs
	
	@SideOnly(Side.CLIENT)
	private IIcon iconPlainFull;
	@SideOnly(Side.CLIENT)
	private IIcon iconTiledFull;
	@SideOnly(Side.CLIENT)
	private IIcon iconTiledHorizontal;
	@SideOnly(Side.CLIENT)
	private IIcon iconTiledVertical;
	
	@Deprecated() // Dirty hack for rendering vertical slabs
	private int metadataForRender;
	
	final byte tier;
	private final int metaHull;
	
	public BlockHullSlab(final int metaHull, final byte tier) {
		super(false, Material.rock);
		this.tier = tier;
		this.metaHull = metaHull;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.hull" + tier + ".slab." + ItemDye.field_150923_a[BlockColored.func_150031_c(metaHull)]);
		setBlockTextureName("warpdrive:hull/");
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 2));
		list.add(new ItemStack(item, 1, 6));
		list.add(new ItemStack(item, 1, 8));
	}
	
	@Override
	public int getRenderColor(int metadata) {
		metadataForRender = metadata;
		return super.getRenderColor(metadata);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		// plain slab => same texture all around
		if (metadata < 6) {
			return iconPlainFull;
		}
		// tiled slab
		if (metadata < 12) {
			final int direction = metadata - 6; 
			// outside plain face
			if (side == direction) {
				return iconTiledFull;
			}
			// inner plain face
			ForgeDirection directionSide = ForgeDirection.getOrientation(side);
			ForgeDirection directionSlab = ForgeDirection.getOrientation(direction);
			if (directionSide == directionSlab.getOpposite()) {
				return iconTiledFull;
			}
			// sides
			if (direction == 0 || direction == 1) {
				return iconTiledHorizontal;
			}
			if (direction == 2 || direction == 3) {
				if (side == 0 || side == 1) {
					return iconTiledHorizontal;
				}
			}
			return iconTiledVertical;
		}
		// plain full block
		if (metadata == 12) {
			return iconPlainFull;
		}
		// horizontal full block
		if (metadata == 13) {
			if (side == 0 || side == 1) {
				return iconTiledFull;
			} else {
				return iconTiledHorizontal;
			}
		}
		// vertical north full block
		if (metadata == 14) {
			if (side == 2 || side == 3) {
				return iconTiledFull;
			} else if (side == 0 || side == 1) {
				return iconTiledHorizontal;
			} else {
				return iconTiledVertical;
			}
		}
		// vertical south full block
		if (metadata == 15) {
			if (side == 4 || side == 5) {
				return iconTiledFull;
			} else {
				return iconTiledVertical;
			}
		}
		// invalid
		return Blocks.fire.getIcon(4, 0);
	}
	
	@Override
	public int damageDropped(int metadata) {
		return metadata <= 1 ? 0    // plain horizontal
		     : metadata <= 5 ? 2    // plain vertical
		     : metadata <= 7 ? 6    // tiled horizontal
		     : metadata <= 11 ? 8   // tiled vertical
		     : metadata;            // others
	}
	
	@Override
	public String func_150002_b(int p_150002_1_) {
		return getUnlocalizedName();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconPlainFull       = iconRegister.registerIcon(getTextureName() + "plain-" + BlockHullPlain.getDyeColorName(metaHull));
		iconTiledFull       = iconRegister.registerIcon(getTextureName() + "tiled-" + BlockHullPlain.getDyeColorName(metaHull));
		iconTiledHorizontal = iconRegister.registerIcon(getTextureName() + "tiled_horizontal-" + BlockHullPlain.getDyeColorName(metaHull));
		iconTiledVertical   = iconRegister.registerIcon(getTextureName() + "tiled_vertical-" + BlockHullPlain.getDyeColorName(metaHull));
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		setBlockBoundsFromMetadata(metadata);
	}
	
	private void setBlockBoundsFromMetadata(final int metadata) {
		if (metadata >= 12) {
			setBlockBounds(0.00F, 0.00F, 0.00F, 1.00F, 1.00F, 1.00F);
			
		} else {
			switch (metadata % 6) {
			case 0:
				setBlockBounds(0.00F, 0.00F, 0.00F, 1.00F, 0.50F, 1.00F);
				return;
			case 1:
				setBlockBounds(0.00F, 0.50F, 0.00F, 1.00F, 1.00F, 1.00F);
				return;
			case 2:
				setBlockBounds(0.00F, 0.00F, 0.00F, 1.00F, 1.00F, 0.50F);
				return;
			case 3:
				setBlockBounds(0.00F, 0.00F, 0.50F, 1.00F, 1.00F, 1.00F);
				return;
			case 4:
				setBlockBounds(0.00F, 0.00F, 0.00F, 0.50F, 1.00F, 1.00F);
				return;
			case 5:
				setBlockBounds(0.50F, 0.00F, 0.00F, 1.00F, 1.00F, 1.00F);
				return;
			default:
				setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
				// return;
			}
		}
	}
	
	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBoundsFromMetadata(metadataForRender);
	}
	
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity) {
		setBlockBoundsBasedOnState(world, x, y, z);
		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return field_150004_a;
	}
	
	@Override
	public int onBlockPlaced(final World world, final int x, final int y, final int z, final int side,
	                         final float hitX, final float hitY, final float hitZ, final int metadata) {
		final ForgeDirection facing = ForgeDirection.getOrientation(side);
		final int variant = metadata < 6 ? 0 : metadata < 12 ? 6 : metadata;
		
		// full block?
		if (field_150004_a || metadata >= 12) {
			return metadata;
		}
		
		// horizontal slab?
		if (metadata == 0 || metadata == 6) {
			// reuse vanilla logic
			final ForgeDirection blockFacing = (facing != ForgeDirection.DOWN && (facing == ForgeDirection.UP || hitY <= 0.5F) ? ForgeDirection.DOWN : ForgeDirection.UP);
			return variant + blockFacing.ordinal();
		}
		// vertical slab?
		if (metadata == 2 || metadata == 8) {
			if (facing != ForgeDirection.DOWN && facing != ForgeDirection.UP) {
				return variant + facing.getOpposite().ordinal();
			}
			// is X the furthest away from center?
			if (Math.abs(hitX - 0.5F) > Math.abs(hitZ - 0.5F)) {
				// west (4) vs east (5)
				return hitX > 0.5F ? variant + ForgeDirection.EAST.ordinal() : variant + ForgeDirection.WEST.ordinal(); 
			}
			// north (2) vs south (3)
			return hitZ > 0.5F ? variant + ForgeDirection.SOUTH.ordinal() : variant + ForgeDirection.NORTH.ordinal();
		}
		return metadata;
	}
	
	@Override
	public int quantityDropped(Random random) {
		return field_150004_a ? 2 : 1;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return field_150004_a;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if (this.field_150004_a) {
			return super.shouldSideBeRendered(blockAccess, x, y, z, side);
		} else if (side != 1 && side != 0 && !super.shouldSideBeRendered(blockAccess, x, y, z, side)) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public MapColor getMapColor(int metadata) {
		return MapColor.getMapColorForBlockColored(metaHull);
	}
	
	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return damageDropped(world.getBlockMetadata(x, y, z)); // override BlockSlab to remove filtering
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
		case 0:	return EnumRarity.epic;
		case 1:	return EnumRarity.common;
		case 2:	return EnumRarity.uncommon;
		case 3:	return EnumRarity.rare;
		default: return rarity;
		}
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
			world.setBlock(x, y, z, WarpDrive.blockHulls_slab[tier - 2][metaHull], metadata, 2);
		}
		return 0;
	}
}
