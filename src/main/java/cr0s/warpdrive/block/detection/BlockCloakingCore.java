package cr0s.warpdrive.block.detection;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCloakingCore extends BlockAbstractContainer {
	
	public BlockCloakingCore(final String registryName) {
		super(Material.IRON);
		setUnlocalizedName("warpdrive.detection.CloakingCore");
		setRegistryName(registryName);
		GameRegistry.register(this);
		GameRegistry.registerTileEntity(TileEntityCloakingCore.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityCloakingCore();
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityCloakingCore) {
			TileEntityCloakingCore cloakingCore = (TileEntityCloakingCore)tileEntity;
			if (itemStackHeld == null) {
				WarpDrive.addChatMessage(entityPlayer, cloakingCore.getStatus());
				// + " isInvalid? " + te.isInvalid() + " Valid? " + te.isValid + " Cloaking? " + te.isCloaking + " Enabled? " + te.isEnabled
				return true;
			} else if (itemStackHeld.getItem() == Item.getItemFromBlock(Blocks.REDSTONE_TORCH)) {
				cloakingCore.isEnabled = !cloakingCore.isEnabled;
				WarpDrive.addChatMessage(entityPlayer, cloakingCore.getStatus());
				return true;
			// } else if (xxx) {// TODO if player has advanced tool
				// WarpDrive.addChatMessage(entityPlayer, cloakingCore.getStatus() + "\n" + cloakingCore.getEnergyStatus());
				// return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void breakBlock(World par1World, BlockPos blockPos, IBlockState blockState) {
		TileEntity te = par1World.getTileEntity(blockPos);
		
		if (te != null && te instanceof TileEntityCloakingCore) {
			((TileEntityCloakingCore)te).isEnabled = false;
			((TileEntityCloakingCore)te).disableCloakingField();
		}
		
		super.breakBlock(par1World, blockPos, blockState);
	}
}
