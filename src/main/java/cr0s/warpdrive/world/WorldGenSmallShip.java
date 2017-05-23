package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;

import java.util.Random;

import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSmallShip extends WorldGenerator {
	
	private final boolean isCorrupted;
	private final boolean isCreative;
	
	public WorldGenSmallShip(final boolean isCorrupted, final boolean isCreative) {
		this.isCorrupted = isCorrupted;
		this.isCreative = isCreative;
	}
	
	@Override
	public boolean generate(final World world, final Random rand, final int centerX, final int centerY, final int centerZ) {
		final WorldGenStructure genStructure = new WorldGenStructure(isCorrupted, rand);
		final boolean hasGlassRoof = rand.nextBoolean();
		final boolean hasWings = rand.nextBoolean();
		final int i = centerX - 5;
		final int j = centerY - 4;
		final int k = centerZ - 6;
		genStructure.setHullPlain(world, i, j + 1, k + 4);
		genStructure.setHullPlain(world, i, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 1, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 1, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 1, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 1, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 1, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 1, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 2, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 2, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 2, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 2, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 2, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 2, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 2, j + 3, k + 6);
		genStructure.setHullPlain(world, i + 2, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 2, j + 3, k + 8);
		genStructure.setHullGlass(world, i + 2, j + 4, k + 6);
		genStructure.setHullGlass(world, i + 2, j + 4, k + 7);
		genStructure.setHullGlass(world, i + 2, j + 4, k + 8);
		genStructure.setHullGlass(world, i + 2, j + 5, k + 6);
		genStructure.setHullGlass(world, i + 2, j + 5, k + 7);
		genStructure.setHullGlass(world, i + 2, j + 5, k + 8);
		genStructure.setHullPlain(world, i + 3, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 3, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 3, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 3, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 6);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 8);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 9);
		genStructure.setHullPlain(world, i + 3, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 3, j + 3, k + 5);
		genStructure.setHullPlain(world, i + 3, j + 3, k + 9);
		genStructure.setHullPlain(world, i + 3, j + 4, k + 5);
		genStructure.setHullPlain(world, i + 3, j + 4, k + 9);
		genStructure.setHullPlain(world, i + 3, j + 5, k + 5);
		genStructure.setHullPlain(world, i + 3, j + 5, k + 9);
		genStructure.setHullPlain(world, i + 3, j + 6, k + 6);
		genStructure.setHullPlain(world, i + 3, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 3, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 4, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 4, j + 2, k + 4);
		world.setBlock(i + 4, j + 2, k + 5, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 4, j + 2, k + 6);
		genStructure.setHullPlain(world, i + 4, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 4, j + 2, k + 8);
		world.setBlock(i + 4, j + 2, k + 9, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 4, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 4, j + 3, k + 4);
		genStructure.setHullPlain(world, i + 4, j + 3, k + 10);
		genStructure.setHullGlass(world, i + 4, j + 4, k + 4);
		genStructure.setHullGlass(world, i + 4, j + 4, k + 10);
		genStructure.setHullPlain(world, i + 4, j + 5, k + 4);
		genStructure.setHullPlain(world, i + 4, j + 5, k + 5);
		genStructure.setHullPlain(world, i + 4, j + 5, k + 9);
		genStructure.setHullPlain(world, i + 4, j + 5, k + 10);
		genStructure.setHullPlain(world, i + 4, j + 6, k + 6);
		genStructure.setHullPlain(world, i + 4, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 4, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 5, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 5, j + 2, k + 3);
		world.setBlock(i + 5, j + 2, k + 4, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 5, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 5, j + 2, k + 6);
		world.setBlock(i + 5, j + 2, k + 7, Blocks.wool, 14, 0);
		world.setBlock(i + 5, j + 2, k + 8, Blocks.wool, 8, 0);
		genStructure.setHullPlain(world, i + 5, j + 2, k + 9);
		world.setBlock(i + 5, j + 2, k + 10, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 5, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 5, j + 3, k + 3);
		genStructure.setHullPlain(world, i + 5, j + 3, k + 11);
		genStructure.setHullGlass(world, i + 5, j + 4, k + 3);
		genStructure.setHullGlass(world, i + 5, j + 4, k + 11);
		genStructure.setHullPlain(world, i + 5, j + 5, k + 3);
		genStructure.setHullPlain(world, i + 5, j + 5, k + 4);
		genStructure.setHullPlain(world, i + 5, j + 5, k + 10);
		genStructure.setHullPlain(world, i + 5, j + 5, k + 11);
		genStructure.setHullPlain(world, i + 5, j + 6, k + 5);
		genStructure.setHullPlain(world, i + 5, j + 6, k + 9);
		genStructure.setHullPlain(world, i + 5, j + 7, k + 6);
		genStructure.setHullPlain(world, i + 5, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 5, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 6, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 5);
		world.setBlock(i + 6, j + 2, k + 6, Blocks.wool, 14, 0);
		world.setBlock(i + 6, j + 2, k + 7, Blocks.wool, 8, 0);
		world.setBlock(i + 6, j + 2, k + 8, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 9);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 6, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 6, j + 3, k + 2);
		world.setBlock(i + 6, j + 3, k + 3, Blocks.chest, 3, 0);
		genStructure.fillInventoryWithLoot(world, rand, i + 6, j + 3, k + 3, "ship");
		world.setBlock(i + 6, j + 3, k + 11, Blocks.chest, 2, 0);
		genStructure.fillInventoryWithLoot(world, rand, i + 6, j + 3, k + 11, "ship");
		genStructure.setHullPlain(world, i + 6, j + 3, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 4, k + 2);
		world.setBlock(i + 6, j + 4, k + 3, Blocks.chest, 3, 0);
		genStructure.fillInventoryWithLoot(world, rand, i + 6, j + 4, k + 3, "ship");
		world.setBlock(i + 6, j + 4, k + 11, Blocks.chest, 2, 0);
		genStructure.fillInventoryWithLoot(world, rand, i + 6, j + 4, k + 11, "ship");
		genStructure.setHullPlain(world, i + 6, j + 4, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 5, k + 2);
		genStructure.setHullPlain(world, i + 6, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 6, j + 7, k + 5);
		if (hasGlassRoof) {
			genStructure.setHullGlass(world, i + 6, j + 7, k + 6);
			genStructure.setHullGlass(world, i + 6, j + 7, k + 7);
			genStructure.setHullGlass(world, i + 6, j + 7, k + 8);
		} else {
			genStructure.setHullPlain(world, i + 6, j + 7, k + 6);
			genStructure.setHullPlain(world, i + 6, j + 7, k + 7);
			genStructure.setHullPlain(world, i + 6, j + 7, k + 8);
		}
		genStructure.setHullPlain(world, i + 6, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 7, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 6);
		world.setBlock(i + 7, j + 2, k + 7, Blocks.wool, 8, 0);
		world.setBlock(i + 7, j + 2, k + 8, Blocks.wool, 8, 0);
		world.setBlock(i + 7, j + 2, k + 9, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 7, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 7, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 7, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 7, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 7, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 7, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 7, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 7, j + 7, k + 5);
		if (hasGlassRoof) {
			genStructure.setHullGlass(world, i + 7, j + 7, k + 6);
			genStructure.setHullGlass(world, i + 7, j + 7, k + 7);
			genStructure.setHullGlass(world, i + 7, j + 7, k + 8);
		} else {
			genStructure.setHullPlain(world, i + 7, j + 7, k + 6);
			genStructure.setHullPlain(world, i + 7, j + 7, k + 7);
			genStructure.setHullPlain(world, i + 7, j + 7, k + 8);
		}
		genStructure.setHullPlain(world, i + 7, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 8, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 5);
		world.setBlock(i + 8, j + 2, k + 6, Blocks.wool, 14, 0);
		world.setBlock(i + 8, j + 2, k + 7, Blocks.wool, 14, 0);
		world.setBlock(i + 8, j + 2, k + 8, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 9);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 8, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 8, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 8, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 8, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 8, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 8, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 8, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 8, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 8, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 5);
		if (hasGlassRoof) {
			genStructure.setHullGlass(world, i + 8, j + 7, k + 6);
			genStructure.setHullGlass(world, i + 8, j + 7, k + 7);
			genStructure.setHullGlass(world, i + 8, j + 7, k + 8);
		} else {
			genStructure.setHullPlain(world, i + 8, j + 7, k + 6);
			genStructure.setHullPlain(world, i + 8, j + 7, k + 7);
			genStructure.setHullPlain(world, i + 8, j + 7, k + 8);
		}
		genStructure.setHullPlain(world, i + 8, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 10);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 9, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 9, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 9, j + 2, k + 4);
		world.setBlock(i + 9, j + 2, k + 5, Blocks.wool, 14, 0);
		world.setBlock(i + 9, j + 2, k + 6, Blocks.wool, 8, 0);
		world.setBlock(i + 9, j + 2, k + 7, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 9, j + 2, k + 8);
		genStructure.setHullPlain(world, i + 9, j + 2, k + 9);
		world.setBlock(i + 9, j + 2, k + 10, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 9, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 9, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 9, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 9, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 9, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 9, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 9, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 9, j + 6, k + 3);
		if (!isCorrupted || rand.nextBoolean()) {
			world.setBlock(i + 9, j + 6, k + 7, WarpDrive.blockAirGenerator, 0, 0);
		}
		genStructure.setHullPlain(world, i + 9, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 6);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 10);
		genStructure.setHullPlain(world, i + 9, j + 8, k + 4);
		genStructure.setHullPlain(world, i + 9, j + 8, k + 10);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 10, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 10, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 10, j + 2, k + 4);
		world.setBlock(i + 10, j + 2, k + 5, Blocks.wool, 8, 0);
		world.setBlock(i + 10, j + 2, k + 6, Blocks.wool, 8, 0);
		genStructure.setHullPlain(world, i + 10, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 10, j + 2, k + 8);
		world.setBlock(i + 10, j + 2, k + 9, Blocks.wool, 8, 0);
		world.setBlock(i + 10, j + 2, k + 10, Blocks.wool, 14, 0);
		genStructure.setHullPlain(world, i + 10, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 10, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 10, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 10, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 10, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 10, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 10, j + 5, k + 7);
		genStructure.setHullGlass(world, i + 10, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 6);
		genStructure.setCable(world, i + 10, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 10, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 10, j + 7, k + 10);
		genStructure.setHullPlain(world, i + 10, j + 8, k + 4);
		genStructure.setHullPlain(world, i + 10, j + 8, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 11, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 6);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 9);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 11, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 11, j + 3, k + 7);
		world.setBlock(i + 11, j + 3, k + 9, Blocks.heavy_weighted_pressure_plate);
		genStructure.setHullPlain(world, i + 11, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 11, j + 4, k + 2);
		if ((!isCorrupted || rand.nextBoolean()) && WarpDriveConfig.isComputerCraftLoaded) {
			world.setBlock(i + 11, j + 4, k + 7, WarpDriveConfig.CC_Computer, 12, 3);
		}
		genStructure.setHullGlass(world, i + 11, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 11, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 11, j + 5, k + 6);
		if (!isCorrupted || rand.nextBoolean()) {
			world.setBlock(i + 11, j + 5, k + 7, WarpDrive.blockShipController);
		}
		genStructure.setHullGlass(world, i + 11, j + 5, k + 8);
		genStructure.setHullGlass(world, i + 11, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 5);
		genStructure.setCable(world, i + 11, j + 6, k + 6);
		genStructure.setCable(world, i + 11, j + 6, k + 7);
		genStructure.setCable(world, i + 11, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 9);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 5);
		genStructure.setSolarPanel(world, i + 11, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 11, j + 7, k + 7);
		genStructure.setSolarPanel(world, i + 11, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 8, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 8, k + 10);
		if (hasWings) {
			genStructure.setHullPlain(world, i + 11, j + 8, k + 3);
			genStructure.setHullPlain(world, i + 11, j + 8, k + 11);
		} else {
			genStructure.setHullPlain(world, i + 11, j + 9, k + 4);
			genStructure.setHullPlain(world, i + 11, j + 9, k + 10);
		}
		genStructure.setHullPlain(world, i + 12, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 12, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 12, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 12, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 12, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 6);
		genStructure.setCable(world, i + 12, j + 2, k + 7);
		genStructure.setCable(world, i + 12, j + 2, k + 8);
		if (!isCorrupted || rand.nextBoolean()) {
			world.setBlock(i + 12, j + 2, k + 9, WarpDrive.blockLift);
			if (isCreative) {// fill with energy
				TileEntity tileEntity = world.getTileEntity(i + 12, j + 2, k + 9);
				if (tileEntity instanceof TileEntityAbstractEnergy) {
					((TileEntityAbstractEnergy) tileEntity).energy_consume(-((TileEntityAbstractEnergy) tileEntity).energy_getMaxStorage());
				}
			}
		}
		genStructure.setHullPlain(world, i + 12, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 6);
		genStructure.setCable(world, i + 12, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 2);
		genStructure.setHullPlain(world, i + 12, j + 4, k + 6);
		genStructure.setHullPlain(world, i + 12, j + 4, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 4, k + 8);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 12, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 12, j + 5, k + 6);
		if (!isCorrupted || rand.nextBoolean()) {
			world.setBlock(i + 12, j + 5, k + 7, WarpDrive.blockShipCore);
			if (isCreative) {// fill with energy
				TileEntity tileEntity = world.getTileEntity(i + 12, j + 5, k + 7);
				if (tileEntity instanceof TileEntityAbstractEnergy) {
					((TileEntityAbstractEnergy) tileEntity).energy_consume( - ((TileEntityAbstractEnergy) tileEntity).energy_getMaxStorage() / 2);
				}
			}
		}
		genStructure.setHullGlass(world, i + 12, j + 5, k + 8);
		genStructure.setHullGlass(world, i + 12, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 5);
		genStructure.setCable(world, i + 12, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 9);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 12, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 1, k + 10);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 13, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 2);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 6);
		genStructure.setCable(world, i + 13, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 10);
		world.setBlock(i + 13, j + 3, k + 11, WarpDrive.blockAirShield);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 13, j + 4, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 6);
		genStructure.setCable(world, i + 13, j + 4, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 10);
		world.setBlock(i + 13, j + 4, k + 11, WarpDrive.blockAirShield);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 2);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 3);
		world.setBlock(i + 13, j + 5, k + 4, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 6);
		genStructure.setCable(world, i + 13, j + 5, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 9);
		world.setBlock(i + 13, j + 5, k + 10, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 6);
		genStructure.setCable(world, i + 13, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 7, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 14, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 14, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 14, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 14, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 4);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 5);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 6);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 9);
		genStructure.setHullPlain(world, i + 14, j + 3, k + 10);
		genStructure.setHullPlain(world, i + 14, j + 4, k + 4);
		genStructure.setResource(world, i + 14, j + 4, k + 5);
		genStructure.setResource(world, i + 14, j + 4, k + 6);
		genStructure.setHullPlain(world, i + 14, j + 4, k + 7);
		genStructure.setResource(world, i + 14, j + 4, k + 8);
		genStructure.setResource(world, i + 14, j + 4, k + 9);
		genStructure.setHullPlain(world, i + 14, j + 4, k + 10);
		genStructure.setHullPlain(world, i + 14, j + 5, k + 4);
		genStructure.setResource(world, i + 14, j + 5, k + 5);
		genStructure.setResource(world, i + 14, j + 5, k + 6);
		genStructure.setHullPlain(world, i + 14, j + 5, k + 7);
		genStructure.setResource(world, i + 14, j + 5, k + 8);
		genStructure.setResource(world, i + 14, j + 5, k + 9);
		genStructure.setHullPlain(world, i + 14, j + 5, k + 10);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 5);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 6);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 8);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 9);
		genStructure.setHullPlain(world, i + 14, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 15, j + 2, k + 3);
		genStructure.setHullPlain(world, i + 15, j + 2, k + 4);
		genStructure.setHullPlain(world, i + 15, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 15, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 15, j + 3, k + 4);
		genStructure.setHullPlain(world, i + 15, j + 3, k + 10);
		genStructure.setHullPlain(world, i + 15, j + 4, k + 7);
		genStructure.setResource(world, i + 15, j + 5, k + 7);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 16, j + 4, k + 7);
		genStructure.setResource(world, i + 16, j + 5, k + 7);
		genStructure.setHullPlain(world, i + 16, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 17, j + 5, k + 7);
		spawnNPC(world, i + 9, j + 3, k + 5);
		return true;
	}
	
	public static void spawnNPC(final World world, final int x, final int y, final int z) {
		final int countMobs = 2 + world.rand.nextInt(10);
		
		if (world.rand.nextBoolean()) {// Villagers
			for (int idx = 0; idx < countMobs; idx++) {
				EntityVillager entityVillager = new EntityVillager(world, 0);
				entityVillager.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
				entityVillager.setCurrentItemOrArmor(4, new ItemStack(WarpDrive.itemWarpArmor[0], 1, 1));
				world.spawnEntityInWorld(entityVillager);
			}
		} else if (world.rand.nextBoolean()) {// Zombies
			for (int idx = 0; idx < countMobs; idx++) {
				EntityZombie entityZombie = new EntityZombie(world);
				entityZombie.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityZombie);
			}
		} else {// Zombie pigmen
			for (int idx = 0; idx < countMobs; idx++) {
				EntityPigZombie entityZombie = new EntityPigZombie(world);
				entityZombie.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityZombie);
			}
		}
	}
}
