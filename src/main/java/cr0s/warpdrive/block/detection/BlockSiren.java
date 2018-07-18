package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSiren extends BlockAbstractContainer {
	
	private final boolean isIndustrial;
	
	public BlockSiren(final String registryName, final EnumTier enumTier, final boolean isIndustrial) {
		super(registryName, enumTier, Material.IRON);
		
		this.isIndustrial = isIndustrial;
		setUnlocalizedName("warpdrive.detection.siren_" + (isIndustrial ? "industrial" : "military") + "." + enumTier.getName());
		registerTileEntity(TileEntitySiren.class, new ResourceLocation(WarpDrive.MODID, registryName));
		
		setDefaultState(getDefaultState()
				                .withProperty(BlockProperties.ACTIVE, false)
				                .withProperty(BlockProperties.FACING, EnumFacing.DOWN) );
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.ACTIVE, BlockProperties.FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(BlockProperties.ACTIVE, (metadata & 0x8) != 0)
				       .withProperty(BlockProperties.FACING, EnumFacing.getFront(metadata & 0x7));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return (blockState.getValue(BlockProperties.ACTIVE) ? 8 : 0)
		       + blockState.getValue(BlockProperties.FACING).getIndex();
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntitySiren(enumTier);
	}
	
	public boolean getIsIndustrial() {
		return isIndustrial;
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		return getMetaFromState(blockState);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull final ItemStack itemStack, @Nullable World world,
	                           @Nonnull final List<String> list, @Nullable final ITooltipFlag advancedItemTooltips) {
		super.addInformation(itemStack, world, list, advancedItemTooltips);
		
		final int range = MathHelper.floor(WarpDriveConfig.SIREN_RANGE_BLOCKS_BY_TIER[enumTier.getIndex()]);
		final String unlocalizedName_withoutTier = getUnlocalizedName().replace("." + enumTier.getName(), "");
		Commons.addTooltip(list, new TextComponentTranslation(unlocalizedName_withoutTier + ".tooltip.usage",
		                                                      range).getFormattedText());
	}
}
