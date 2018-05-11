package cr0s.warpdrive.config.structures;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * @author LemADEC
 *
 */
public abstract class AbstractStructureInstance extends WorldGenerator {
	protected AbstractStructure structure;
	protected HashMap<String,Double> variables = new HashMap<>();
	
	public AbstractStructureInstance(final AbstractStructure structure, final Random random) {
		this.structure = structure;
		
		// evaluate variables
		for (final Entry<String, String> entry : structure.variables.entrySet()) {
			final double value;
			String stringValue = entry.getValue();
			try {
				if (stringValue.contains(",")) {
					final String[] values = stringValue.split(",");
					stringValue = values[random.nextInt(values.length)];
				}
				value = Double.parseDouble(entry.getValue());
			} catch (final NumberFormatException exception) {
				throw new RuntimeException("Invalid expression '" + entry.getValue() + "'"
						+ (stringValue.equalsIgnoreCase(entry.getValue()) ? "" : " in '" + entry.getValue() + "'")
						+ " for variable " + entry.getKey()
						+ " in deployable structure " + structure.name
						+ ": a numeric value is expected. Check the related XML configuration file...");
			}
			
			variables.put(entry.getKey(), value);
		}
	}
	
	protected String evaluate(final String valueOrExpression) {
		if (!valueOrExpression.contains("%")) {
			return valueOrExpression;
		}
		String result = valueOrExpression;
		for (final Entry<String, Double> variable : variables.entrySet()) {
			result = result.replaceAll(variable.getKey(), "" + variable.getValue());
		}
		return result;
	}
	
	public AbstractStructureInstance(final NBTTagCompound tagCompound) {
		// FIXME to be implemented
		
		// get deployable
		// String deployableName = tagCompound.getString("wd_structureName");
		
		// get variables values
		/*
		final NBTTagCompound tagVariables = tagCompound.getCompoundTag("wd_variables");
		final NBTTagList names = tagVariables.getTagList("x", 0);
		for (final Entry<String, Double> entry : tagVariables.getTagList("x", 0)) {
			tagVariables.setDouble(entry.getKey(), entry.getValue());
		}
		/**/
	}
	
	public void WriteToNBT(final NBTTagCompound tagCompound) {
		tagCompound.setString("wd_structureGroup", structure.group);
		tagCompound.setString("wd_structureName", structure.name);
		final NBTTagCompound tagVariables = new NBTTagCompound();
		for (final Entry<String, Double> entry : variables.entrySet()) {
			tagVariables.setDouble(entry.getKey(), entry.getValue());
		}
		tagCompound.setTag("wd_variables", tagVariables);
	}
}
