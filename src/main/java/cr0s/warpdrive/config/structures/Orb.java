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

public abstract class Orb extends DeployableStructure implements XmlRepresentable {

	private OrbShell[] shellRelative;
	// private ArrayList<OrbShell> shells;
	private String name;

	/**
	 * @return the radius
	 */
	public int getRadius() {
		return super.height / 2;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(int radius) {

		super.height = radius * 2;
		super.length = radius * 2;
		super.width = radius * 2;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Orb(int radius) {
		super(radius * 2, radius * 2, radius * 2);

		setRadius(radius);

	}

	@Override
	public void loadFromXmlElement(Element element) throws InvalidXmlException {

		name = element.getAttribute("name");

		ArrayList<OrbShell> newShells = new ArrayList<OrbShell>();
		int totalThickness = 0;

		NodeList shells = element.getElementsByTagName("shell");
		for (int i = 0; i < shells.getLength(); i++) {
			Element elementShell = (Element) shells.item(i);

			OrbShell shell = new OrbShell();
			shell.loadFromXmlElement(elementShell);
			shell.finishContruction();
			totalThickness += shell.thickness;
			newShells.add(shell);
		}

		int index = 0;
		shellRelative = new OrbShell[totalThickness];

		for (OrbShell shell : newShells) {

			for (int i = 0; i < shell.thickness; i++)
				shellRelative[index++] = shell;
		}

		setRadius(totalThickness - 1);
	}

	@Override
	public void saveToXmlElement(Element element, Document document) {
		/* TODO: dead code?
		for (OrbShell shell : shells) {
			Element tmp = document.createElement("shell");
			shell.saveToXmlElement(tmp, document);
			element.appendChild(tmp);
		}
		/**/
	}

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, getRadius(), this, true);
		world.spawnEntityInWorld(entitySphereGen);
		return false;
	}

	public boolean generate(World world, Random random, int x, int y, int z, final int radius) {
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, radius, this, true);
		world.spawnEntityInWorld(entitySphereGen);
		return false;
	}

	public OrbShell getShellForRadius(int r) {
		return shellRelative[r];
	}

	public class OrbShell extends FillerSet {

		private int thickness;

		/**
		 * @return the thickness
		 */
		public int getThickness() {
			return thickness;
		}

		/**
		 * @param thickness
		 *            the thickness to set
		 */
		public void setThickness(int thickness) {
			this.thickness = thickness;
		}

		public OrbShell() {

			super("");
		}

		@Override
		public void loadFromXmlElement(Element element) throws InvalidXmlException {

			WarpDrive.logger.info("Loading shell " + element.getAttribute("name"));
			name = element.getAttribute("name");

			super.loadFromXmlElement(element);

			if (element.hasAttribute("fillerSets")) {
				String[] imports = element.getAttribute("fillerSets").split(",");

				for (String imp : imports) {
					FillerSet fillSet = FillerManager.getFillerSet(imp);

					if (fillSet == null)
						throw new InvalidXmlException("Shell loading tries to import a non-existant fillerSet!");

					super.loadFrom(fillSet);
				}
			}

			try {
				thickness = Integer.parseInt(element.getAttribute("maxThickness"));
			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("MaxThickness is not valid!");
			}

			//TODO: Implement random thickness

		}

		@Override
		public void saveToXmlElement(Element e, Document d) {
			//Not needed
		}

	}

}
