package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockAcceleratorController extends BlockAbstractContainer {
	
	public BlockAcceleratorController(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.atomic.accelerator_controller");
		
		GameRegistry.registerTileEntity(TileEntityAcceleratorController.class, WarpDrive.PREFIX + registryName);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityAcceleratorController)) {
			return false;
		}
		final TileEntityAcceleratorController tileEntityAcceleratorController = (TileEntityAcceleratorController) tileEntity;
		
		if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityAcceleratorController.getStatus());
			return true;
			
		}
		
		return false;
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityAcceleratorController();
	}
}
