package cr0s.warpdrive.config.filler;

import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.MetaBlock;
import cr0s.warpdrive.config.XmlRepresentable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Represents a set of fillers. Before using after construction, finishContruction() must be called.
 *
 * If FillerSet(blocks[]) is called, that is not necessary.
 *
 */
public class FillerSet implements XmlRepresentable, Comparable {
	private MetaBlock[] weightedFillerBlocks;
	private FillerFactory factory;
	protected String group;
	protected String name;
	
	public String getGroup() {
		return group;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public FillerSet(MetaBlock[] blocks) {
		weightedFillerBlocks = blocks;
	}
	
	public FillerSet(final String group, final String name) {
		
		this.group = group;
		this.name = name;
		
		weightedFillerBlocks = new MetaBlock[1];
		factory = new FillerFactory();
	}
	
	public MetaBlock getRandomBlock(Random rand) {
		return weightedFillerBlocks[rand.nextInt(weightedFillerBlocks.length)];
	}
	
	@Override
	public void loadFromXmlElement(Element element) throws InvalidXmlException {
		
		NodeList fillers = element.getElementsByTagName("filler");
		for (int i = 0; i < fillers.getLength(); i++) {
			
			Element filler = (Element) fillers.item(i);
			
			// Check there is a block name
			if (!filler.hasAttribute("block")) {
				throw new InvalidXmlException("Filler " + filler + " is missing a block tag!");
			}
			
			String blockName = filler.getAttribute("block");
			Block block = Block.getBlockFromName(blockName);
			if (block == null) {
				WarpDrive.logger.warn("Skipping missing block " + blockName);
				continue;
			}
			
			// Get metadata attribute, defaults to 0
			int intMetadata = 0;
			String stringMetadata = filler.getAttribute("metadata");
			if (!stringMetadata.isEmpty()) {
				try {
					intMetadata = Integer.parseInt(stringMetadata);
				} catch (NumberFormatException exception) {
					throw new InvalidXmlException("Invalid metadata for block " + blockName);
				}
			}
			
			boolean hasWeightOrRatio = false;
			
			// It is intentional that a filler could have both a ratio and a weight
			
			// Check for a weight and add it to the factory
			String stringWeight = filler.getAttribute("weight");
			int weight;
			
			if (!stringWeight.isEmpty()) {
				hasWeightOrRatio = true;
				
				try {
					weight = Integer.parseInt(stringWeight);
					
					factory.addWeightedBlock(block, intMetadata, weight);
					
				} catch (NumberFormatException exception) {
					throw new InvalidXmlException("Invalid weight for block " + blockName);
				} catch (IllegalArgumentException exception) {
					throw new InvalidXmlException(exception.getMessage());
				}
			}
			
			// Check for a ratio attribute, and add it to the factory
			String stringRatio = filler.getAttribute("ratio");
			if (!stringRatio.isEmpty()) {
				hasWeightOrRatio = true;
				
				try {
					factory.addRatioBlock(block, intMetadata, stringRatio);
					
				} catch (IllegalArgumentException exception) {
					throw new InvalidXmlException(exception.getMessage());
				}
			}
			
			if (!hasWeightOrRatio) {
				throw new InvalidXmlException("No ratio nor weight defined for block " + blockName + " " + stringMetadata);
			}
		}
	}
	
	/**
	 * @deprecated Not implemented
	 **/
	@Deprecated
	@Override
	public void saveToXmlElement(Element element, Document document) throws InvalidXmlException {
		throw new InvalidXmlException("Not implemented");
	}
	
	/**
	 * Uses the data that has been loaded thus far to construct the array in order to make the FillerSet functional. Must be called before calling getRandomBlock()
	 *
	 * Clears the memory used for construction
	 */
	public void finishContruction() {
		WarpDrive.logger.info("Finishing construction of " + name);
		weightedFillerBlocks = factory.constructWeightedMetaBlockList();
		
		//For some reason some entries are null, so replace them with air FIXME
		for (int i = 0; i < weightedFillerBlocks.length; i++) {
			if (weightedFillerBlocks[i] == null) {
				weightedFillerBlocks[i] = MetaBlock.getMetaBlock(Blocks.air, 0);
			}
		}
		
		factory = null;
	}
	
	@Override
	public int compareTo(Object object) {
		return name.compareTo(((FillerSet) object).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Adds the blocks from the given fillerSet into this one. Must be pre-finishConstruction()
	 *
	 * @param fillerSet
	 *            The fillerset to add from
	 */
	public void loadFrom(FillerSet fillerSet) {
		factory.addFromFactory(fillerSet.factory);
	}
}
