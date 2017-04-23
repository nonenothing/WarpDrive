package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

import java.util.Random;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSmallShip extends WorldGenerator {
	
	private final boolean corrupted;
	
	public WorldGenSmallShip(final boolean corrupted) {
		this.corrupted = corrupted;
	}
	
	@Override
	public boolean generate(final World world, final Random rand, final int centerX, final int centerY, final int centerZ) {
		WorldGenStructure genStructure = new WorldGenStructure(corrupted, rand);
		int i = centerX - 5, j = centerY - 4, k = centerZ - 6;
		genStructure.setHullPlain(world, i + 0, j + 1, k + 4);
		genStructure.setHullPlain(world, i + 0, j + 1, k + 10);
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
		genStructure.setHullPlain(world, i + 5, j + 6, k + 6);
		genStructure.setHullPlain(world, i + 5, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 5, j + 6, k + 8);
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
		fillChestWithBonuses(world, rand, i + 6, j + 3, k + 3);
		world.setBlock(i + 6, j + 3, k + 11, Blocks.chest, 2, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 3, k + 11);
		genStructure.setHullPlain(world, i + 6, j + 3, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 4, k + 2);
		world.setBlock(i + 6, j + 4, k + 3, Blocks.chest, 3, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 4, k + 3);
		world.setBlock(i + 6, j + 4, k + 11, Blocks.chest, 2, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 4, k + 11);
		genStructure.setHullPlain(world, i + 6, j + 4, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 5, k + 2);
		genStructure.setHullPlain(world, i + 6, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 4);
		if (!corrupted || rand.nextBoolean()) {
			world.setBlock(i + 6, j + 6, k + 7, WarpDrive.blockAirGenerator, 0, 0);
		}
		genStructure.setHullPlain(world, i + 6, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 6, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 6, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 6, j + 7, k + 6);
		genStructure.setHullPlain(world, i + 6, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 6, j + 7, k + 8);
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
		genStructure.setCable(world, i + 7, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 7, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 7, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 7, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 7, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 7, j + 7, k + 8);
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
		genStructure.setHullPlain(world, i + 8, j + 6, k + 4);
		genStructure.setCable(world, i + 8, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 8, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 8, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 8, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 8, j + 7, k + 8);
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
		genStructure.setHullPlain(world, i + 9, j + 6, k + 4);
		genStructure.setCable(world, i + 9, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 9, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 9, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 9, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 9, j + 7, k + 7);
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
		genStructure.setHullGlass(world, i + 10, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 10, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 10, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 10, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 10, j + 5, k + 2);
		genStructure.setHullGlass(world, i + 10, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 10, j + 6, k + 4);
		genStructure.setCable(world, i + 10, j + 6, k + 7);	
		genStructure.setHullPlain(world, i + 10, j + 6, k + 10);
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
		genStructure.setHullGlass(world, i + 11, j + 3, k + 6);
		if (!corrupted || rand.nextBoolean()) {
			world.setBlock(i + 11, j + 3, k + 7, WarpDrive.blockShipController);
		}
		genStructure.setHullGlass(world, i + 11, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 11, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 11, j + 4, k + 6);
		if ((!corrupted || rand.nextBoolean()) && WarpDriveConfig.isComputerCraftLoaded) {
			world.setBlock(i + 11, j + 4, k + 7, WarpDriveConfig.CC_Computer, 12, 3);
		}
		genStructure.setHullGlass(world, i + 11, j + 4, k + 8);
		genStructure.setHullGlass(world, i + 11, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 11, j + 5, k + 2);
		if (!corrupted || rand.nextBoolean()) {
			world.setBlock(i + 11, j + 5, k + 7, WarpDrive.blockAirGenerator, 0, 0);
		}
		genStructure.setHullGlass(world, i + 11, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 4);
		genStructure.setCable(world, i + 11, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 6);
		genStructure.setSolarPanel(world, i + 11, j + 7, k + 7);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 8);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 9);
		genStructure.setHullPlain(world, i + 11, j + 7, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 8, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 8, k + 10);
		genStructure.setHullPlain(world, i + 11, j + 9, k + 4);
		genStructure.setHullPlain(world, i + 11, j + 9, k + 10);
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
		genStructure.setHullPlain(world, i + 12, j + 2, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 8);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 10);
		genStructure.setHullPlain(world, i + 12, j + 2, k + 11);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 2);
		genStructure.setHullGlass(world, i + 12, j + 3, k + 6);
		if (!corrupted || rand.nextBoolean()) {
			world.setBlock(i + 12, j + 3, k + 7, WarpDrive.blockShipCore);
		}
		genStructure.setHullGlass(world, i + 12, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 12, j + 3, k + 12);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 6);
		genStructure.setCable(world, i + 12, j + 4, k + 7);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 8);
		genStructure.setHullGlass(world, i + 12, j + 4, k + 12);
		genStructure.setHullGlass(world, i + 12, j + 5, k + 2);
		genStructure.setCable(world, i + 12, j + 5, k + 7);
		genStructure.setHullGlass(world, i + 12, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 4);
		genStructure.setCable(world, i + 12, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 12, j + 6, k + 11);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 5);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 6);
		genStructure.setHullPlain(world, i + 12, j + 7, k + 7);
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
		genStructure.setHullPlain(world, i + 13, j + 3, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 10);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 3, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 2);
		genStructure.setHullGlass(world, i + 13, j + 4, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 9);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 10);
		genStructure.setHullGlass(world, i + 13, j + 4, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 4, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 2);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 3);
		world.setBlock(i + 13, j + 5, k + 4, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 7);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 8);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 9);
		world.setBlock(i + 13, j + 5, k + 10, Blocks.glowstone);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 11);
		genStructure.setHullPlain(world, i + 13, j + 5, k + 12);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 3);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 5);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 6);
		genStructure.setHullPlain(world, i + 13, j + 6, k + 7);
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
		world.setBlock(i + 14, j + 4, k + 5, Blocks.redstone_block);
		world.setBlock(i + 14, j + 4, k + 6, Blocks.redstone_block);
		genStructure.setHullPlain(world, i + 14, j + 4, k + 7);
		world.setBlock(i + 14, j + 4, k + 8, Blocks.redstone_block);
		world.setBlock(i + 14, j + 4, k + 9, Blocks.redstone_block);
		genStructure.setHullPlain(world, i + 14, j + 4, k + 10);
		genStructure.setHullPlain(world, i + 14, j + 5, k + 4);
		world.setBlock(i + 14, j + 5, k + 5, Blocks.redstone_block);
		world.setBlock(i + 14, j + 5, k + 6, Blocks.redstone_block);
		genStructure.setHullPlain(world, i + 14, j + 5, k + 7);
		world.setBlock(i + 14, j + 5, k + 8, Blocks.redstone_block);
		world.setBlock(i + 14, j + 5, k + 9, Blocks.redstone_block);
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
		world.setBlock(i + 15, j + 5, k + 7, Blocks.redstone_block);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 4);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 15, j + 6, k + 10);
		genStructure.setHullPlain(world, i + 16, j + 4, k + 7);
		world.setBlock(i + 16, j + 5, k + 7, Blocks.redstone_block);
		genStructure.setHullPlain(world, i + 16, j + 6, k + 7);
		genStructure.setHullPlain(world, i + 17, j + 5, k + 7);
		world.setBlock(i + 12, j + 2, k + 9, Blocks.trapdoor, 10, 0);
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
		} else {// Zombies
			for (int idx = 0; idx < countMobs; idx++) {
				EntityZombie entityZombie = new EntityZombie(world);
				entityZombie.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityZombie);
			}
		}
	}
	
	public void fillChestWithBonuses(final World worldObj, final Random rand, final int x, final int y, final int z) {
		final TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
		
		if (tileEntity != null) {
			final TileEntityChest chest = (TileEntityChest) tileEntity;
			final int size = chest.getSizeInventory();
			int numBonuses = rand.nextInt(size) / 2;
			
			for (int i = 0; i < size; i++) {
				if (rand.nextInt(size) <= numBonuses) {
					numBonuses--;
					chest.setInventorySlotContents(i, getRandomBonus(rand));
				}
			}
		}
	}
	
	private ItemStack getRandomBonus(final Random rand) {
		ItemStack res = null;
		boolean isDone = false;
		
		while (!isDone) {
			switch (rand.nextInt(14)) {
			case 0: // Mass fabricator
				if (WarpDriveConfig.isIndustrialCraft2Loaded) {
					res = WarpDriveConfig.getModItemStack("IC2", "blockMachine", -1);
					res.setItemDamage(14);
					res.stackSize = 1; // + rand.nextInt(2);
					isDone = true;
				}
				break;
				
			case 1:
				if (WarpDriveConfig.isIndustrialCraft2Loaded) {
					res = WarpDriveConfig.getModItemStack("IC2", "blockNuke", -1);
					res.stackSize = 1 + rand.nextInt(2);
					isDone = true;
				}
				break;
				
			case 2: // Quantum armor bonuses
			case 3:
			case 4:
			case 5:
				isDone = true;
				break;// skipped
				
			case 6:// Glass fiber cable item
				if (WarpDriveConfig.isIndustrialCraft2Loaded) {
					res = WarpDriveConfig.getModItemStack("IC2", "itemCable", -1);
					res.setItemDamage(9);
					res.stackSize = 2 + rand.nextInt(12);
					isDone = true;
				}
				break;
			
			case 7:// UU matter cell
				if (WarpDriveConfig.isIndustrialCraft2Loaded) {
					res = WarpDriveConfig.getModItemStack("IC2", "itemCellEmpty", -1);
					res.setItemDamage(3);
					res.stackSize = 2 + rand.nextInt(14);
					isDone = true;
				}
				break;
			
			case 8:
				isDone = true;
				break;// skipped
			
			case 9:
			case 10:
			case 11: // Rocket launcher platform Tier3
				if (WarpDriveConfig.isICBMLoaded) {
					// TODO: No 1.7 ICBM yet
					// res = new ItemStack(WarpDriveConfig.ICBM_Machine, 1 +
					// rand.nextInt(1), 2).copy();
					isDone = true;
				}
				break;
			
			
			case 12: // Missiles from conventional to hypersonic
				if (WarpDriveConfig.isICBMLoaded) {
					// TODO: No 1.7 ICBM yet
					// res = new ItemStack(WarpDriveConfig.ICBM_Missile, 2 +
					// rand.nextInt(1), rand.nextInt(10)).copy();
					isDone = true;
				}
				break;
			
			case 13: // Advanced solar panels
				if (WarpDriveConfig.isAdvancedSolarPanelLoaded) {
					// TODO: res = new ItemStack(solarPanel_block, rand.nextInt(3), solarPanel_metadata);
					isDone = true;
				}
				break;
			
			default:
				break;
			}
		}
		
		return res;
	}
}
