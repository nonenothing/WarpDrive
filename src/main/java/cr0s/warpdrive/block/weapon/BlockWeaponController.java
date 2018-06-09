package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockWeaponController extends BlockAbstractContainer {
	
	public BlockWeaponController(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setUnlocalizedName("warpdrive.weapon.WeaponController");
		registerTileEntity(TileEntityWeaponController.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityWeaponController();
	}
}