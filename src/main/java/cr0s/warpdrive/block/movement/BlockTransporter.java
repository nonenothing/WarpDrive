package cr0s.warpdrive.block.movement;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockTransporter extends BlockAbstractContainer {

	private IIcon[] iconBuffer;

	public BlockTransporter() {
		super(Material.rock);
		setBlockName("warpdrive.movement.Transporter");
		setStepSound(Block.soundTypeMetal);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityTransporter();
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[3];
		// Solid textures
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:movement/transporterBottom");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:movement/transporterTop");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:movement/transporterSide");
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[side];
		}

		return iconBuffer[2];
	}
	
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		if (par5EntityPlayer.getHeldItem() == null) {
			TileEntity te = par1World.getTileEntity(par2, par3, par4);
			if (te != null && te instanceof TileEntityTransporter) {
				WarpDrive.addChatMessage(par5EntityPlayer, ((TileEntityTransporter)te).getStatus());
				return true;
			}
		}
		
		return false;
	}
}