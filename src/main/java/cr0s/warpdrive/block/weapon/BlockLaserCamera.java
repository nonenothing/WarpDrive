package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.render.ClientCameraHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class BlockLaserCamera extends BlockAbstractContainer {
	
	public BlockLaserCamera(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setUnlocalizedName("warpdrive.weapon.LaserCamera");
		registerTileEntity(TileEntityLaserCamera.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityLaserCamera();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean causesSuffocation(final IBlockState state) {
		return false;
	}
}