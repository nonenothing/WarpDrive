package cr0s.warpdrive.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockChunkLoader() {
		super(Material.iron);
		setBlockName("warpdrive.machines.ChunkLoader");
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityChunkLoader();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[3];
		icons[0] = iconRegister.registerIcon("warpdrive:chunk_loader-offline");
		icons[1] = iconRegister.registerIcon("warpdrive:chunk_loader-out_of_power");
		icons[2] = iconRegister.registerIcon("warpdrive:chunk_loader-active");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		if (metadata < icons.length) {
			return icons[metadata];
		}
		
		return icons[0];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int damage) {
		return icons[2];
	}
}
