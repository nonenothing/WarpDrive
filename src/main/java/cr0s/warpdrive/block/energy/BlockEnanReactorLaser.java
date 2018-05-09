package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockEnanReactorLaser extends BlockAbstractContainer {
	
	public BlockEnanReactorLaser(final String registryName) {
		super(registryName, Material.IRON);
		setResistance(60.0F * 5 / 3);
		setUnlocalizedName("warpdrive.energy.enan_reactor_laser");
		GameRegistry.registerTileEntity(TileEntityEnanReactorLaser.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityEnanReactorLaser();
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
}