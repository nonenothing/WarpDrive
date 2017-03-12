package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import net.minecraft.block.ITileEntityProvider;
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

public class BlockAcceleratorControlPoint extends BlockAbstractAccelerator implements ITileEntityProvider {
	
	public BlockAcceleratorControlPoint(final String registryName) {
		super(registryName, (byte) 1);
		setUnlocalizedName("warpdrive.atomic.accelerator_control_point");
		GameRegistry.registerTileEntity(TileEntityAcceleratorControlPoint.class, WarpDrive.MODID + ":blockAcceleratorControlPoint");
	}
	
	BlockAcceleratorControlPoint(final String registryName, final byte tier) {
		super(registryName, tier);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem(EnumHand.MAIN_HAND) == null) {
			TileEntity tileEntity = world.getTileEntity(blockPos);
			
			if (tileEntity instanceof TileEntityAcceleratorControlPoint) {
				Commons.addChatMessage(entityPlayer, ((TileEntityAcceleratorControlPoint) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityAcceleratorControlPoint();
	}
}
