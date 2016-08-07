package cr0s.warpdrive.block.passive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import cr0s.warpdrive.WarpDrive;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockHighlyAdvancedMachine extends Block {
	public BlockHighlyAdvancedMachine() {
		super(Material.IRON);
		setHardness(5.0F);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setRegistryName("warpdrive.passive.HighlyAdvancedMachine");
		GameRegistry.register(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}