package cr0s.warpdrive.api;

import net.minecraft.world.World;

public interface IHullBlock {
	public void downgrade(World world, final int x, final int y, final int z);
}
