package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class BlockAbstractBase extends Block implements IBlockBase {
	
	protected final EnumTier enumTier;
	
	protected BlockAbstractBase(final String registryName, final EnumTier enumTier, final Material material) {
		super(material);
		
		this.enumTier = enumTier;
		setHardness(5.0F);
		setResistance(6.0F * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabMain);
		setRegistryName(registryName);
		WarpDrive.register(this);
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockAbstractBase(this);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		final Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
		
		// Force a single model through through a custom state mapper
		/*
		final StateMapperBase stateMapperBase = new StateMapperBase() {
			@Nonnull
			@Override
			protected ModelResourceLocation getModelResourceLocation(@Nonnull final IBlockState blockState) {
				return modelResourceLocation;
			}
		};
		ModelLoader.setCustomStateMapper(this, stateMapperBase);
		/**/
		
		// Bind our TESR to our tile entity
		/*
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityXXXX.class, new TileEntityXXXRenderer());
		/**/
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final BlockPos blockPos, final IBlockState blockState,
	                            final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		final boolean isRotating = blockState.getProperties().containsKey(BlockProperties.FACING);
		if (isRotating) {
			EnumFacing enumFacing = Commons.getFacingFromEntity(entityLiving);
			world.setBlockState(blockPos, blockState.withProperty(BlockProperties.FACING, enumFacing));
		}
	}
	
	@Override
	public boolean removedByPlayer(@Nonnull final IBlockState blockState, final World world, @Nonnull final BlockPos blockPos,
	                               @Nonnull final EntityPlayer player, final boolean willHarvest) {
		return willHarvest || super.removedByPlayer(blockState, world, blockPos, player, false);
	}
	
	@Override
	public boolean rotateBlock(final World world, @Nonnull final BlockPos blockPos, @Nonnull final EnumFacing axis) {
		// already handled by vanilla
		return super.rotateBlock(world, blockPos, axis);
	}
	
	@Nonnull
	@Override
	public EnumTier getTier(final ItemStack itemStack) {
		return enumTier;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack) {
		return getTier(itemStack).getRarity();
	}
}
