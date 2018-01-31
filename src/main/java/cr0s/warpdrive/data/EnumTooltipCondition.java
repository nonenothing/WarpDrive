package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IStringSerializable;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

public enum EnumTooltipCondition implements IStringSerializable {
	
	NEVER             ("never"            ),
	ON_SNEAK          ("on_sneak"         ),
	ADVANCED_TOOLTIPS ("advanced_tooltips"),
	CREATIVE_ONLY     ("creative_only"    ),
	ALWAYS            ("always"           );
	
	public final String unlocalizedName;
	
	EnumTooltipCondition(final String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
	}
	
	public boolean isEnabled(final boolean isSneaking, final boolean isCreativeMode) {
		switch(this) {
		case NEVER: return false;
		case ON_SNEAK: return isSneaking;
		case ADVANCED_TOOLTIPS: return Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
		case CREATIVE_ONLY: return isCreativeMode;
		case ALWAYS: return true;
		default: return false;
		}
	}
	
	@Override
	public String toString() {
		return unlocalizedName;
	}
	
	@Nonnull
	@Override
	public String getName() { return unlocalizedName; }
	
	public static String formatAllValues() {
		final StringBuilder result = new StringBuilder();
		for (EnumTooltipCondition enumTooltipCondition : EnumTooltipCondition.values()) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(enumTooltipCondition);
		}
		return result.toString();
	}
}
