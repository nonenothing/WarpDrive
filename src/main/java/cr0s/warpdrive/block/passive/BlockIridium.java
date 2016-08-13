package cr0s.warpdrive.block.passive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import cr0s.warpdrive.WarpDrive;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockIridium extends Block {
	public BlockIridium(final String registryName) {
		super(Material.IRON);
		setHardness(3.4F);
		setResistance(360.0F * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setUnlocalizedName("warpdrive.passive.IridiumBlock");
		setRegistryName(registryName);
		GameRegistry.register(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}