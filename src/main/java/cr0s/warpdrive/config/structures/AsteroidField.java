package cr0s.warpdrive.config.structures;

import cr0s.warpdrive.config.InvalidXmlException;
import org.w3c.dom.Element;

import java.util.Random;

import net.minecraft.world.World;

public class AsteroidField extends AbstractStructure {
	
	public AsteroidField(String group, String name) {
		super(group, name);
	}
	
	@Override
	public boolean loadFromXmlElement(Element element) throws InvalidXmlException {
		return false;
	}
	
	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		return instantiate(random).generate(world, random, x, y, z);
	}
	
	@Override
	public AbstractStructureInstance instantiate(Random random) {
		return new AsteroidFieldInstance(this, random);
	}
}
