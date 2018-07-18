package cr0s.warpdrive.data;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;

public class BlockProperties {
	
	// Common block properties
	public static final PropertyBool                   ACTIVE        = PropertyBool.create("active");
	public static final PropertyDirection              FACING        = PropertyDirection.create("facing");
	public static final UnlistedPropertyBlockState     CAMOUFLAGE    = new UnlistedPropertyBlockState("camouflage");
	public static final PropertyEnum<EnumValidPowered> VALID_POWERED = PropertyEnum.create("valid_powered", EnumValidPowered.class);
	
}
