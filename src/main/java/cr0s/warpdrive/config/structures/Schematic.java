package cr0s.warpdrive.config.structures;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Schematic extends AbstractStructure {
	
	public Schematic(final String group, final String name) {
		super(group, name);
	}
	
	@Override
	public boolean generate(final World world, final Random random, final BlockPos blockPos) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractStructureInstance instantiate(final Random random) {
		// TODO Auto-generated method stub
		return null;
	}
}
