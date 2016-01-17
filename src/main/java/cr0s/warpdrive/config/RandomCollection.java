package cr0s.warpdrive.config;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.w3c.dom.Element;

import cr0s.warpdrive.WarpDrive;

/**
 * Collection of elements with weights. Helps to select element with controlled odds.
 * 
 * @author ncrashed
 *
 * @param <E>
 */
public class RandomCollection<E extends XmlRepresentable> {
	private final NavigableMap<Double, E> weightMap = new TreeMap<Double, E>();
	private double totalWeight = 0;
	private final NavigableMap<Double, E> ratioMap = new TreeMap<Double, E>();
	private double totalRatio = 0;
	private final ArrayList<E> list = new ArrayList<E>();
	
	/**
	 * Add new object and its weight.
	 * 
	 * @param weight
	 *            Used for random pick. The higher the value is relatively to others, the higher odds of choosing the object.
	 * @param object
	 *            Object to add
	 */
	public void addWeight(double weight, E object) {
		if (weight <= 0) {
			WarpDrive.logger.warn("Weight is negative or zero, skipping " + object);
			return;
		}
		totalWeight += weight;
		weightMap.put(totalWeight, object);
		list.add(object);
	}
	
	/**
	 * Add new object and its ratio. Warning: if total ratio goes higher than 1.0, element won't be added to collection.
	 * 
	 * @param ratio
	 *            Chance of random pick in range (0, 1.0]. In contrast to weights, ratio is fixed and chances don't change if you add more elements.
	 * @param object
	 *            Object to add
	 */
	public void addRatio(double ratio, E object) {
		if (ratio <= 0 || ratio >= 1.0) {
			WarpDrive.logger.warn("Ratio isn't in (0, 1.0] bounds, skipping " + object);
			return;
		}
		
		if (totalRatio + ratio > 1.0) {
			WarpDrive.logger.warn("Total ratio is greater than 1.0, skipping " + object);
			return;
		}
		totalRatio += ratio;
		ratioMap.put(totalRatio, object);
		list.add(object);
	}
	
	/**
	 * Get a random object according weights and ratios
	 * 
	 * @param random
	 * @return Random object or null if there is no objects to pick.
	 */
	public E getRandomEntry(Random random) {
		double value = random.nextDouble();
		
		if (value < totalRatio) { // hit ratio part of values
			return ratioMap.ceilingEntry(value).getValue();
		} else { // hit dynamic part of values, weighted ones
			double weight = (value - totalRatio) * totalWeight;
			return weightMap.ceilingEntry(weight).getValue();
		}
	}
	
	/**
	 * Get a specific object through its name
	 * 
	 * @param name Exact name of the object
	 * @return Named object or null if there is no object with that name
	 */
	public E getNamedEntry(final String name) {
		for(E object : list) {
			if (object.getName().equals(name)) {
				return object;
			}
		}
		return null;
	}
	
	/**
	 * @return All registered objects
	 */
	public ArrayList<E> elements() {
		return list;
	}
	
	/**
	 * Loads object from given XML element and parses configurations for weighted pick.
	 * 
	 * @param object
	 *            Object to load into
	 * @param element
	 *            Element of an XML file
	 * @throws InvalidXmlException
	 */
	public void loadFromXML(E object, Element element) throws InvalidXmlException {
		object.loadFromXmlElement(element);
		
		try {
			String ratioStr = element.getAttribute("ratio");
			if (!ratioStr.isEmpty()) {
				double ratio = Double.parseDouble(ratioStr);
				addRatio(ratio, object);
			} else { // try weight
				try {
					int weight = 1;
					String stringWeight = element.getAttribute("weight");
					if (!stringWeight.isEmpty()) {
						weight = Integer.parseInt(stringWeight);
						weight = Math.max(1, weight);
					}
					
					addWeight(weight, object);
				} catch (NumberFormatException exceptionWeight) {
					throw new InvalidXmlException("Weight must be an integer!");
				}
			}
		} catch (NumberFormatException exceptionRatio) {
			throw new InvalidXmlException("Ratio must be double!");
		}
	}
}