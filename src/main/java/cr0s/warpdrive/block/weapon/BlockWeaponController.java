package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockWeaponController extends BlockAbstractContainer {
	
	public BlockWeaponController(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setUnlocalizedName("warpdrive.weapon.WeaponController");
		GameRegistry.registerTileEntity(TileEntityWeaponController.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityWeaponController();
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
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		if (itemStackHeld == null) {
			final TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityWeaponController) {
				Commons.addChatMessage(entityPlayer, ((TileEntityWeaponController) tileEntity).getStatus());
			} else {
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.guide.prefix",
						getLocalizedName())
				    .appendSibling(new TextComponentTranslation("warpdrive.error.badTileEntity")));
				WarpDrive.logger.error("Block " + this + " with invalid tile entity " + tileEntity);
			}
			return true;
		}
		
		return false;
	}
}