package cr0s.warpdrive.render;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IMyBakedModel;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.data.BlockProperties;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BakedModelCamouflage implements IPerspectiveAwareModel, IMyBakedModel {
	
	private ResourceLocation resourceLocation;
	private IBakedModel bakedModelOriginal;
	
	public BakedModelCamouflage() {
	}
	
	@Override
	public void setResourceLocation(final ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}
	
	@Override
	public void setOriginalBakedModel(final IBakedModel bakedModel) {
		this.bakedModelOriginal = bakedModel;
	}
	
	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState blockState, @Nullable EnumFacing facing, long rand) {
		if (blockState instanceof IExtendedBlockState) {
			final IExtendedBlockState extendedBlockState = (IExtendedBlockState) blockState;
			final IBlockState blockStateReference = extendedBlockState.getValue(BlockProperties.CAMOUFLAGE);
			if (blockStateReference != Blocks.AIR.getDefaultState()) {
				try {
					// Retrieve the IBakedModel of the copied block and return it.
					final BlockModelShapes blockModelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
					final IBakedModel bakedModelResult = blockModelShapes.getModelForState(blockStateReference);
					return bakedModelResult.getQuads(blockStateReference, facing, rand);
				} catch(Exception exception) {
					exception.printStackTrace();
					WarpDrive.logger.error(String.format("Failed to render camouflage for block state %s, updating dictionary with %s = NOCAMOUFLAGE dictionary to prevent further errors",
					                                     blockStateReference,
					                                     blockStateReference.getBlock().getRegistryName()));
					Dictionary.BLOCKS_NOCAMOUFLAGE.add(blockStateReference.getBlock());
				}
			}
		}
		return bakedModelOriginal.getQuads(blockState, facing, rand);
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
		return bakedModelOriginal.getOverrides();
	}
	
	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		final Matrix4f matrix4f;
		if (bakedModelOriginal instanceof IPerspectiveAwareModel) {
			matrix4f = ((IPerspectiveAwareModel) bakedModelOriginal).handlePerspective(cameraTransformType).getRight();
		} else {
			final ItemTransformVec3f itemTransformVec3f = bakedModelOriginal.getItemCameraTransforms().getTransform(cameraTransformType);
			matrix4f = new TRSRTransformation(itemTransformVec3f).getMatrix();
		}
		return Pair.of(this, matrix4f);
	}
}