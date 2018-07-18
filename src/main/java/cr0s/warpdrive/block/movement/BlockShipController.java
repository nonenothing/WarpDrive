package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumShipCommand;
import cr0s.warpdrive.data.EnumTier;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockShipController extends BlockAbstractContainer {
	
	public static final PropertyEnum<EnumShipCommand> COMMAND = PropertyEnum.create("command", EnumShipCommand.class);
	
	public BlockShipController(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.movement.ship_controller" + enumTier.getIndex());
		
		setDefaultState(getDefaultState()
				                .withProperty(COMMAND, EnumShipCommand.OFFLINE)
		               );
		registerTileEntity(TileEntityShipController.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, COMMAND);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(COMMAND, EnumShipCommand.get(metadata));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(COMMAND).ordinal();
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityShipController(enumTier);
	}
}