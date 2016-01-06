package cr0s.warpdrive.block.energy;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;

public class BlockEnergyBank extends BlockAbstractContainer {
	private IIcon iconBuffer;
	
	public BlockEnergyBank() {
		super(Material.iron);
		setBlockName("warpdrive.energy.EnergyBank");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnergyBank();
	}
	
	@Override
	public IIcon getIcon(int side, int meta) {
		return iconBuffer;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = par1IconRegister.registerIcon("warpdrive:energy/energyBank");
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return false;
		}
		
		TileEntityAbstractEnergy abstractEnergy = (TileEntityAbstractEnergy) world.getTileEntity(x, y, z);
		if (abstractEnergy != null && (entityPlayer.getHeldItem() == null)) {
			WarpDrive.addChatMessage(entityPlayer, abstractEnergy.getStatus());
			return true;
		}
		
		return false;
	}
}