package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockJumpGateCore extends BlockAbstractContainer {
	
	public BlockJumpGateCore(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.movement.jump_gate_core");
		registerTileEntity(TileEntityShipCore.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityShipCore();
	}
}