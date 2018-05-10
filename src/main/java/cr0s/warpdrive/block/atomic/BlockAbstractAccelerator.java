package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.block.BlockAbstractBase;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class BlockAbstractAccelerator extends BlockAbstractBase implements IBlockBase {
	
	public final byte tier;
	
	public BlockAbstractAccelerator(final byte tier) {
		super(Material.iron);
		this.tier = tier;
		setHardness(4 + tier);
		setResistance((2 + 2 * tier) * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return true;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 0;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}
	
	@Override
	public int damageDropped(final int metadata) {
		return 0;
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public boolean canCreatureSpawn(final EnumCreatureType type, final IBlockAccess blockAccess, final int x, final int y, final int z) {
		return false;
	}
}
