package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockEnanReactorLaser extends BlockAbstractContainer {
	
	public BlockEnanReactorLaser() {
		super(Material.IRON);
		setResistance(60.0F * 5 / 3);
		setRegistryName("warpdrive.energy.EnanReactorLaser");
		GameRegistry.register(this);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityEnanReactorLaser();
	}
	
	private static boolean isActive(int side, int meta) {
		if (side == 3 && meta == 1) {
			return true;
		}
		
		if (side == 2 && meta == 2) {
			return true;
		}
		
		if (side == 4 && meta == 4) {
			return true;
		}
		
		if (side == 5 && meta == 3) {
			return true;
		}
		return false;
	}
}