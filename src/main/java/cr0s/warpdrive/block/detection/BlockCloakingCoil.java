package cr0s.warpdrive.block.detection;

import java.util.Random;

import cr0s.warpdrive.block.BlockAbstractBase;
import net.minecraft.block.material.Material;

public class BlockCloakingCoil extends BlockAbstractBase {
	
	public BlockCloakingCoil(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(3.5F);
		setUnlocalizedName("warpdrive.detection.CloakingCoil");
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}
