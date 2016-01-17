/**
 *
 */
package cr0s.warpdrive.config.structures;

import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * @author Francesco, LemADEC
 *
 */
public abstract class DeployableStructure extends WorldGenerator {
	protected String name;
	protected int sizeX;
	protected int sizeY;
	protected int sizeZ;

	public DeployableStructure(final String name) {
		this.name = name;
	}
	
	public void setDimensions(final int sizeX, final int sizeY, final int sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}
	
	public void setRadius(final int radius) {
		sizeX = radius * 2;
		sizeY = radius * 2;
		sizeZ = radius * 2;
	}

}
