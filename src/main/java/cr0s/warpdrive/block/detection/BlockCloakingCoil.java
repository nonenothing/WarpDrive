package cr0s.warpdrive.block.detection;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import cr0s.warpdrive.WarpDrive;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockCloakingCoil extends Block {
	
	public BlockCloakingCoil(final String registryName) {
		super(Material.IRON);
		setHardness(3.5F);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("warpdrive.detection.CloakingCoil");
		setRegistryName(registryName);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		GameRegistry.register(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}
