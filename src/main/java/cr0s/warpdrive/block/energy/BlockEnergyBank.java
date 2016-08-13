package cr0s.warpdrive.block.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.item.ItemTuningFork;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

public class BlockEnergyBank extends BlockAbstractContainer {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockEnergyBank() {
		super(Material.iron);
		setBlockName("warpdrive.energy.EnergyBank");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnergyBank();
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[3];
		icons[0] = iconRegister.registerIcon("warpdrive:energy/energyBank");
		icons[1] = iconRegister.registerIcon("warpdrive:energy/energyBankInput");
		icons[2] = iconRegister.registerIcon("warpdrive:energy/energyBankOutput");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityEnergyBank)) {
			return icons[0];
		}
		
		return icons[((TileEntityEnergyBank) tileEntity).getMode(EnumFacing.getFront(side))];
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[side == 1 ? 1 : 2];
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityEnergyBank)) {
			return false;
		}
		TileEntityEnergyBank tileEntityEnergyBank = (TileEntityEnergyBank) tileEntity;
		ItemStack itemStackHeld = entityPlayer.getHeldItem();
		EnumFacing facing = EnumFacing.getFront(side);
		
		if (itemStackHeld == null) {
			WarpDrive.addChatMessage(entityPlayer, tileEntityEnergyBank.getStatus());
			return true;
		} else if (itemStackHeld.getItem() instanceof ItemTuningFork) {
			tileEntityEnergyBank.setMode(facing, (byte)((tileEntityEnergyBank.getMode(facing) + 1) % 3));
			switch (tileEntityEnergyBank.getMode(facing)) {
				case TileEntityEnergyBank.MODE_INPUT:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
					    getLocalizedName())
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToInput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_OUTPUT:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
					    getLocalizedName())
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToOutput", facing.name()));
					return true;
				case TileEntityEnergyBank.MODE_DISABLED:
				default:
					WarpDrive.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
					    getLocalizedName())
					    + StatCollector.translateToLocalFormatted("warpdrive.energy.side.changedToDisabled", facing.name()));
					return true;
			}
		}
		
		return false;
	}
}