package cr0s.warpdrive.render;

import com.google.common.collect.ImmutableList;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IMyBakedModel;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.data.EnumDisabledInputOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.common.property.IExtendedBlockState;

public class BakedModelEnergyBank implements IBakedModel, IMyBakedModel {
	
	private ResourceLocation resourceLocation;
	private IBakedModel bakedModelOriginal;
	private IExtendedBlockState extendedBlockStateDefault;
	
	public BakedModelEnergyBank() {
	}
	
	@Override
	public void setResourceLocation(final ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}
	
	@Override
	public void setOriginalBakedModel(final IBakedModel bakedModel) {
		this.bakedModelOriginal = bakedModel;
	}
	
	public IBakedModel getOriginalBakedModel() {
		return bakedModelOriginal;
	}
	
	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable final IBlockState blockState, @Nullable final EnumFacing facing, final long rand) {
		assert resourceLocation != null;
		assert bakedModelOriginal != null;
		
		final IExtendedBlockState extendedBlockState;
		if (blockState == null) {
			// dead code until we have different blocks for each tiers to support item rendering and 1.13+
			if (extendedBlockStateDefault == null) {
				extendedBlockStateDefault = ((IExtendedBlockState) WarpDrive.blockEnergyBank.getDefaultState())
				        .withProperty(BlockEnergyBank.DOWN , EnumDisabledInputOutput.INPUT)
				        .withProperty(BlockEnergyBank.UP   , EnumDisabledInputOutput.INPUT)
				        .withProperty(BlockEnergyBank.NORTH, EnumDisabledInputOutput.OUTPUT)
				        .withProperty(BlockEnergyBank.SOUTH, EnumDisabledInputOutput.OUTPUT)
				        .withProperty(BlockEnergyBank.WEST , EnumDisabledInputOutput.OUTPUT)
				        .withProperty(BlockEnergyBank.EAST , EnumDisabledInputOutput.OUTPUT);
			}
			extendedBlockState = extendedBlockStateDefault;
		} else if (blockState instanceof IExtendedBlockState) {
			extendedBlockState = (IExtendedBlockState) blockState;
		} else {
			extendedBlockState = null;
		}
		if (extendedBlockState != null) {
			final EnumDisabledInputOutput enumDisabledInputOutput = getEnumDisabledInputOutput(extendedBlockState, facing);
			final IBlockState blockStateToRender = extendedBlockState.getClean().withProperty(BlockEnergyBank.CONFIG, enumDisabledInputOutput);
			
			// remap to the json model representing the proper state
			final BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
			final IBakedModel bakedModelWrapped = blockModelShapes.getModelForState(blockStateToRender);
			final IBakedModel bakedModelToRender = ((BakedModelEnergyBank) bakedModelWrapped).getOriginalBakedModel();
			return bakedModelToRender.getQuads(blockStateToRender, facing, rand);
		}
		return getDefaultQuads(facing, rand);
	}
	
	public EnumDisabledInputOutput getEnumDisabledInputOutput(final IExtendedBlockState extendedBlockState, @Nullable final EnumFacing facing) {
		if (facing == null) {
			return EnumDisabledInputOutput.DISABLED;
		}
		switch (facing) {
		case DOWN : return extendedBlockState.getValue(BlockEnergyBank.DOWN);
		case UP   : return extendedBlockState.getValue(BlockEnergyBank.UP);
		case NORTH: return extendedBlockState.getValue(BlockEnergyBank.NORTH);
		case SOUTH: return extendedBlockState.getValue(BlockEnergyBank.SOUTH);
		case WEST : return extendedBlockState.getValue(BlockEnergyBank.WEST);
		case EAST : return extendedBlockState.getValue(BlockEnergyBank.EAST);
		default: return EnumDisabledInputOutput.DISABLED;
		}
	}
	
	public List<BakedQuad> getDefaultQuads(final EnumFacing side, final long rand) {
		final IBlockState blockState = Blocks.FIRE.getDefaultState();
		return Minecraft.getMinecraft().getBlockRendererDispatcher()
		       .getModelForState(blockState).getQuads(blockState, side, rand);
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return bakedModelOriginal.isAmbientOcclusion();
	}
	
	@Override
	public boolean isGui3d() {
		return bakedModelOriginal.isGui3d();
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return bakedModelOriginal.isBuiltInRenderer();
	}
	
	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		// Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("warpdrive:someTexture")
		return bakedModelOriginal.getParticleTexture();
	}
	
	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		// ItemCameraTransforms.DEFAULT
		return bakedModelOriginal.getItemCameraTransforms();
	}
	
	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return itemOverrideList;
	}
	
	private final ItemOverrideList itemOverrideList = new ItemOverrideList(ImmutableList.of()) {
		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull final IBakedModel model, @Nonnull final ItemStack itemStack,
		                                   @Nonnull final World world, @Nonnull final EntityLivingBase entity) {
			final IBlockState blockState = WarpDrive.blockEnergyBank.getStateFromMeta(itemStack.getMetadata());
			final IBakedModel bakedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(blockState);
			if (bakedModel instanceof BakedModelEnergyBank) {
				return ((BakedModelEnergyBank) bakedModel).bakedModelOriginal;
			}
			return bakedModel;
		}
	};
}