package cr0s.warpdrive.config;

import cr0s.warpdrive.api.IParticleContainerItem;
import cr0s.warpdrive.api.ParticleStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

// Adds support for IParticleContainerItem ingredients
// Loosely inspired from vanilla ShapedOreRecipe
public class RecipeParticleShapedOre implements IRecipe {
	
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;
	
	private ItemStack itemStackResult = null;
	private Object[] itemStackIngredients = null;
	private int width = 0;
	private int height = 0;
	private boolean isMirrored = true;
	
	public RecipeParticleShapedOre(final Block result, Object... recipe){ this(new ItemStack(result), recipe); }
	public RecipeParticleShapedOre(final Item result, Object... recipe){ this(new ItemStack(result), recipe); }
	public RecipeParticleShapedOre(final ItemStack result, Object... recipe) {
		this.itemStackResult = result.copy();
		
		String shape = "";
		int indexRecipe = 0;
		
		// first parameter is an optional boolean 'mirrored'
		if (recipe[indexRecipe] instanceof Boolean) {
			isMirrored = (Boolean)recipe[indexRecipe];
			if (recipe[indexRecipe + 1] instanceof Object[]) {
				recipe = (Object[])recipe[indexRecipe+1];
			} else {
				indexRecipe = 1;
			}
		}
		
		// second parameter section is either a string array for aliases or a sequence of strings aliases
		if (recipe[indexRecipe] instanceof String[]) {
			String[] stringRecipeLines = ((String[]) recipe[indexRecipe++]);
			
			for (String stringRecipeLine : stringRecipeLines) {
				width = stringRecipeLine.length();
				shape += stringRecipeLine;
			}
			
			height = stringRecipeLines.length;
		} else {
			while (recipe[indexRecipe] instanceof String) {
				String stringRecipeLine = (String) recipe[indexRecipe++];
				shape += stringRecipeLine;
				width = stringRecipeLine.length();
				height++;
			}
		}
		
		// validate size
		if (width * height != shape.length()) {
			String stringMessage = "Invalid shaped ore recipe: ";
			for (Object objectIngredient : recipe) {
				stringMessage += objectIngredient + ", ";
			}
			stringMessage += itemStackResult;
			throw new RuntimeException(stringMessage);
		}
		
		HashMap<Character, Object> mapInputs = new HashMap<>();
		
		// third parameter section is the list of alias to component table
		// convert inputs to ItemStack or ArrayList<ItemStack>
		for (; indexRecipe < recipe.length; indexRecipe += 2) {
			Character character = (Character) recipe[indexRecipe];
			Object object = recipe[indexRecipe + 1];
			
			if (object instanceof ItemStack) {
				mapInputs.put(character, ((ItemStack) object).copy());
			} else if (object instanceof Item) {
				mapInputs.put(character, new ItemStack((Item) object));
			} else if (object instanceof Block) {
				mapInputs.put(character, new ItemStack((Block) object, 1, OreDictionary.WILDCARD_VALUE));
			} else if (object instanceof String) {
				mapInputs.put(character, OreDictionary.getOres((String) object));
			} else {
				String stringMessage = "Invalid shaped ore recipe: ";
				for (Object objectIngredient :  recipe) {
					stringMessage += objectIngredient + ", ";
				}
				stringMessage += itemStackResult;
				throw new RuntimeException(stringMessage);
			}
		}
		
		// save recipe inputs
		itemStackIngredients = new Object[width * height];
		int indexSlot = 0;
		for (char chr : shape.toCharArray()) {
			itemStackIngredients[indexSlot++] = mapInputs.get(chr);
		}
		
		// add lower priority vanilla Shaped recipe for NEI support
		GameRegistry.addRecipe(new ShapedOreRecipe(result, recipe));
	}
	
	// add ore dictionary support to an existing (vanilla) recipe
	RecipeParticleShapedOre(ShapedRecipes recipe, Map<ItemStack, String> replacements) {
		itemStackResult = recipe.getRecipeOutput();
		width = recipe.recipeWidth;
		height = recipe.recipeHeight;
		
		itemStackIngredients = new Object[recipe.recipeItems.length];
		
		for(int i = 0; i < itemStackIngredients.length; i++) {
			ItemStack itemStackIngredient = recipe.recipeItems[i];
			
			if (itemStackIngredient == null) {
				continue;
			}
			
			itemStackIngredients[i] = recipe.recipeItems[i];
			
			for(Entry<ItemStack, String> entry : replacements.entrySet()) {
				if (OreDictionary.itemMatches(entry.getKey(), itemStackIngredient, true)) {
					itemStackIngredients[i] = OreDictionary.getOres(entry.getValue());
					break;
				}
			}
		}
	}
	
	// Returns an Item that is the result of this recipe
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
		return itemStackResult.copy();
	}
	
	// Returns the size of the recipe area
	@Override
	public int getRecipeSize() {
		return itemStackIngredients.length;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return itemStackResult;
	}
	
	// check if a recipe matches current crafting inventory
	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		for (int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++) {
			for (int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y) {
				if (checkMatch(inv, x, y, false)) {
					return true;
				}
				
				if (isMirrored && checkMatch(inv, x, y, true)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean checkMatch(InventoryCrafting inventoryCrafting, int startX, int startY, boolean mirror) {
		for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
			for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
				final int subX = x - startX;
				final int subY = y - startY;
				Object target = null;
				
				if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
					if (mirror) {
						target = itemStackIngredients[width - subX - 1 + subY * width];
					} else {
						target = itemStackIngredients[subX + subY * width];
					}
				}
				
				ItemStack itemStackSlot = inventoryCrafting.getStackInRowAndColumn(x, y);
				
				if (target instanceof ItemStack) {// simple ingredient
					if ( itemStackSlot != null
					  && itemStackSlot.hasTagCompound()
					  && itemStackSlot.getItem() instanceof IParticleContainerItem
					  && ((ItemStack) target).getItem() instanceof IParticleContainerItem) {
						IParticleContainerItem particleContainerItemSlot = (IParticleContainerItem) itemStackSlot.getItem();
						ParticleStack particleStackSlot = particleContainerItemSlot.getParticle(itemStackSlot);
						
						IParticleContainerItem particleContainerItemTarget = (IParticleContainerItem) ((ItemStack) target).getItem();
						ParticleStack particleStackTarget = particleContainerItemTarget.getParticle((ItemStack) target);
						
						// reject different particles or insufficient quantity
						if (!particleStackSlot.containsParticle(particleStackTarget)) {
							return false;
						}
						// mark quantity otherwise
						particleContainerItemSlot.setAmountToConsume(itemStackSlot, particleStackTarget.getAmount());
						
					} else if (!OreDictionary.itemMatches((ItemStack)target, itemStackSlot, false)) {
						return false;
					}
					
				} else if (target instanceof ArrayList) {// ore dictionary ingredient
					boolean matched = false;
					
					@SuppressWarnings("unchecked")
					Iterator<ItemStack> iterator = ((ArrayList<ItemStack>)target).iterator();
					while (iterator.hasNext() && !matched) {
						matched = OreDictionary.itemMatches(iterator.next(), itemStackSlot, false);
					}
					
					if (!matched) {
						return false;
					}
					
				} else if (target == null && itemStackSlot != null) {// ingredient found while none expected
					return false;
				}
			}
		}
		
		return true;
	}
	
	public RecipeParticleShapedOre setMirrored(final boolean isMirrored) {
		this.isMirrored = isMirrored;
		return this;
	}
	
	/**
	 * Returns the ingredients for this recipe, any mod accessing this value should never
	 * manipulate the values in this array as it will effect the recipe itself.
	 * @return The recipes itemStackIngredients vales.
	 */
	public Object[] getIngredients() {
		return this.itemStackIngredients;
	}
	@Deprecated
	public Object[] getInput() {
		return this.itemStackIngredients;
	}
}
