package cr0s.warpdrive.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	IIcon iconBuffer;
	
	public BlockChunkLoader() {
		super(Material.iron);
		setBlockName("warpdrive.machines.ChunkLoader");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityChunkLoader();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = iconRegister.registerIcon("warpdrive:chunkLoader");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int damage) {
		return iconBuffer;
	}
}
