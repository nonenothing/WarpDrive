package cr0s.warpdrive.data;

import java.util.HashMap;

public enum EnumStarMapEntryType {
	UNDEFINED  (0, "-undefined-"),
	SHIP       (1, "ship"       ), // a ship core
	JUMPGATE   (2, "jumpgate"   ), // a jump gate
	PLANET     (3, "planet"     ), // a planet (a transition plane allowing to move to another dimension)
	STAR       (4, "star"       ), // a star
	STRUCTURE  (5, "structure"  ), // a structure from WorldGeneration (moon, asteroid field, etc.)
	WARP_ECHO  (6, "warp_echo"  ), // remains of a warp
	ACCELERATOR(7, "accelerator"), // an accelerator setup
	TRANSPORTER(8, "transporter"); // a transporter room
	
	private final int id;
	private final String name;
	
	// cached values
	public static final int length;
	private static final HashMap<String, EnumStarMapEntryType> mapNames = new HashMap<>();
	
	static {
		length = EnumStarMapEntryType.values().length;
		for (final EnumStarMapEntryType enumStarMapEntryType : values()) {
			mapNames.put(enumStarMapEntryType.getName(), enumStarMapEntryType);
		}
	}
	
	EnumStarMapEntryType(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public static EnumStarMapEntryType getByName(final String name) {
		return mapNames.get(name);
	}
}
