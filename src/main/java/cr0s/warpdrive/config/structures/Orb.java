package cr0s.warpdrive.config.structures;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.XmlRepresentable;
import cr0s.warpdrive.config.filler.FillerManager;
import cr0s.warpdrive.config.filler.FillerSet;
import cr0s.warpdrive.world.EntitySphereGen;
import cr0s.warpdrive.world.EntityStarCore;

public abstract class Orb extends DeployableStructure implements XmlRepresentable {
	
	private OrbShell[] orbShells;
	protected boolean hasStarCore = false;
	private ArrayList<String> fillerSetGroupOrNames = new ArrayList<String>(); 
	
	public Orb(final String name) {
		super(name);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void loadFromXmlElement(Element element) throws InvalidXmlException {
		
		int maxThickness = 0;
		
		NodeList nodeListShells = element.getElementsByTagName("shell");
		orbShells = new OrbShell[nodeListShells.getLength()];
		for (int shellIndex = 0; shellIndex < nodeListShells.getLength(); shellIndex++) {
			Element elementShell = (Element) nodeListShells.item(shellIndex);
			String orbShellName = element.getAttribute("name");
			
			orbShells[shellIndex] = new OrbShell(name, orbShellName);
			orbShells[shellIndex].loadFromXmlElement(elementShell);
			orbShells[shellIndex].finishContruction();
			maxThickness += orbShells[shellIndex].maxThickness;
		}
		
		setRadius(maxThickness - 1);
	}
	
	/**
	 * @deprecated Not implemented
	 **/
	@Deprecated
	@Override
	public void saveToXmlElement(Element element, Document document) throws InvalidXmlException {
		throw new InvalidXmlException("Not implemented");
	}
	
	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		int[] thicknesses = randomize(random);
		int totalThickness = 0;
		for (int thickness : thicknesses) {
			totalThickness += thickness;
		}
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, this, thicknesses, totalThickness, true);
		world.spawnEntityInWorld(entitySphereGen);
		if (hasStarCore) {
			return world.spawnEntityInWorld(new EntityStarCore(world, x, y, z, totalThickness));
		}
		return false;
	}
	
	/**
	 * 
	 * @Deprecated pending addition of variables offsets in structure XML attributes
	 */
	@Deprecated
	public boolean generate(World world, Random random, int x, int y, int z, final int radius) {
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, this, null, radius, true);
		world.spawnEntityInWorld(entitySphereGen);
		return false;
	}
	
	public int[] randomize(Random random) {
		int[] thicknesses = new int[orbShells.length];
		for(int orbShellIndex = 0; orbShellIndex < orbShells.length; orbShellIndex++) {
			OrbShell orbShell = orbShells[orbShellIndex];
			thicknesses[orbShellIndex] = orbShell.minThickness
					+ ((orbShell.maxThickness - orbShell.minThickness > 0) ? random.nextInt(orbShell.maxThickness - orbShell.minThickness) : 0);
		}
		return thicknesses;
	}
	
	public OrbShell getShellForRadius(final int[] thicknesses, final int range) {
		int cumulatedRange = 0;
		for (int shellIndex = 0; shellIndex < orbShells.length; shellIndex++) {
			cumulatedRange += thicknesses[shellIndex];
			if (range <= cumulatedRange) {
				return orbShells[shellIndex];
			}
		}
		return null;
	}
	
	public class OrbShell extends FillerSet {
		private String parentName;
		private int minThickness;
		private int maxThickness;
		
		public OrbShell(String parentName, String name) {
			super(null, name);
			this.parentName = parentName;
		}
		
		@Override
		public void loadFromXmlElement(Element element) throws InvalidXmlException {
			WarpDrive.logger.info("Loading shell " + element.getAttribute("name"));
			
			super.loadFromXmlElement(element);
			
			if (element.hasAttribute("fillerSets")) {
				String[] allFillerSetGroupOrNames = element.getAttribute("fillerSets").split(",");
				
				for (String fillerSetGroupOrName : allFillerSetGroupOrNames) {
					if (!FillerManager.doesFillerSetExist(fillerSetGroupOrName)) {
						WarpDrive.logger.warn("Skipping missing FillerSet " + fillerSetGroupOrName + " in shell " + name);
					} else {
						fillerSetGroupOrNames.add(fillerSetGroupOrName);
					}
				}
			}
			
			try {
				minThickness = Integer.parseInt(element.getAttribute("minThickness"));
			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("Invalid minThickness in shell " + name + " of orb " + parentName);
			}
			
			try {
				maxThickness = Integer.parseInt(element.getAttribute("maxThickness"));
			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("Invalid maxThickness in shell " + name + " of orb " + parentName);
			}
			
			if (maxThickness < minThickness) {
				throw new InvalidXmlException("Invalid maxThickness " + maxThickness + " lower than minThickness " + minThickness + " in shell " + name + " of orb " + parentName);
			}
		}
	}
}
