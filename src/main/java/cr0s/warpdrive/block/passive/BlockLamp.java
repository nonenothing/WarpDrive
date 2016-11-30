package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockLamp extends BlockAbstractBase {
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	protected static final AxisAlignedBB AABB_DOWN  = new AxisAlignedBB(0.00D, 0.84D, 0.00D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_UP    = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 1.00D, 0.16D, 1.00D);
	protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.00D, 0.00D, 0.84D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 1.00D, 1.00D, 0.16D);
	protected static final AxisAlignedBB AABB_WEST  = new AxisAlignedBB(0.84D, 0.00D, 0.00D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 0.16D, 1.00D, 1.00D);
	
	public BlockLamp(final String registryName) {
		super(registryName, Material.ROCK);
		setHardness(WarpDriveConfig.HULL_HARDNESS[0]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[0] * 5 / 3);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("warpdrive.passive.lamp");
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		
		setLightLevel(14.0F / 15.0F);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this,
				new IProperty[] { FACING },
				new IUnlistedProperty[] {  });
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.getFront(metadata & 7));
	}
	
	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState blockState, IBlockAccess world, BlockPos blockPos) {
		return blockState;
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState) {
		return blockState.getValue(FACING).getIndex();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing side) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isBlockNormalCube(IBlockState blockState) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}
		
	@Nonnull
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entityLiving) {
		EnumFacing enumFacing = getFacingFromEntity(pos, entityLiving);
		return this.getDefaultState().withProperty(FACING, enumFacing);
	}
	
	public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
		return EnumFacing.getFacingFromVector(
				(float) (entity.posX - clickedBlock.getX()),
				(float) (entity.posY - clickedBlock.getY()),
				(float) (entity.posZ - clickedBlock.getZ()));
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
			case DOWN : return AABB_DOWN ;
			case UP   : return AABB_UP   ;
			case NORTH: return AABB_NORTH;
			case SOUTH: return AABB_SOUTH;
			case WEST : return AABB_WEST ;
			case EAST : return AABB_EAST ;
			default   : return AABB_UP;
		}
	}
	
	@Nullable
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
		return NULL_AABB;
	}

	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot)
	{
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}
}