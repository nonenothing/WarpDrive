package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.block.BlockAbstractOmnipanel;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHullOmnipanel extends BlockAbstractOmnipanel implements IDamageReceiver {
	
	final byte tier;
	
	public BlockHullOmnipanel(final String registryName, final byte tier) {
		super(null, Material.GLASS);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setLightLevel(10.0F / 15.0F);
		setSoundType(SoundType.GLASS);
		setUnlocalizedName("warpdrive.hull" + tier + ".omnipanel.");
		setDefaultState(blockState.getBaseState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE));
		setRegistryName(registryName);
		WarpDrive.register(this, new ItemBlockHull(this));
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	@Override
	public int damageDropped(IBlockState blockState) {
		return blockState.getValue(BlockColored.COLOR).getMetadata();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		for (EnumDyeColor enumDyeColor : EnumDyeColor.values()) {
			list.add(new ItemStack(item, 1, enumDyeColor.getMetadata()));
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public MapColor getMapColor(IBlockState blockState) {
		return blockState.getValue(BlockColored.COLOR).getMapColor();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata) {
		return this.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byMetadata(metadata));
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState) {
		return blockState.getValue(BlockColored.COLOR).getMetadata();
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockColored.COLOR);
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos blockPos, DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(IBlockState blockState, World world, BlockPos blockPos,
	                       DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(blockPos);
		} else {
			world.setBlockState(blockPos, WarpDrive.blockHulls_omnipanel[tier - 2]
			                              .getDefaultState()
			                              .withProperty(BlockColored.COLOR, blockState.getValue(BlockColored.COLOR)), 2);
		}
		return 0;
	}
}