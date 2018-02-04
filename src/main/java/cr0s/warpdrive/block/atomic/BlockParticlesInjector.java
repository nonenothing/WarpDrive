package cr0s.warpdrive.block.atomic;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockParticlesInjector extends BlockAcceleratorControlPoint {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockParticlesInjector() {
		super();
		setBlockName("warpdrive.atomic.particles_injector");
		setBlockTextureName("warpdrive:atomic/particles_injector");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[2];
		
		icons[0] = iconRegister.registerIcon(getTextureName() + "-off");
		icons[1] = iconRegister.registerIcon(getTextureName() + "-on");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata % 2];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityParticlesInjector();
	}
}
