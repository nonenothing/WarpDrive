package cr0s.warpdrive.block.movement;

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

public class BlockTransporter extends BlockAbstractContainer {
	
	public BlockTransporter(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.movement.Transporter");
		GameRegistry.registerTileEntity(TileEntityTransporter.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityTransporter();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (itemStackHeld == null) {
			TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityTransporter) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityTransporter)tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}