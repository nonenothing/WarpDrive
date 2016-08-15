package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class BlockAbstractForceField extends BlockAbstractContainer {
	protected byte tier;
	
	BlockAbstractForceField(final byte tier, final Material material) {
		super(material);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return tier;
	}
		
	@Override
	public void onEMP(World world, final int x, final int y, final int z, final float efficiency) {
		super.onEMP(world, x, y, z, efficiency * (1.0F - 0.2F * (tier - 1)));
	}
}
