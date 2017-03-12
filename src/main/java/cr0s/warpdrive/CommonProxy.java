package cr0s.warpdrive;

import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.entity.EntityParticleBunch;
import cr0s.warpdrive.render.EntityCamera;
import cr0s.warpdrive.world.EntitySphereGen;
import cr0s.warpdrive.world.EntityStarCore;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import static cr0s.warpdrive.WarpDrive.MODID;


public class CommonProxy {
	
	private static final WeakHashMap<GameProfile, WeakReference<EntityPlayer>> fakePlayers = new WeakHashMap<>(100);
	
	void registerEntities() {
		EntityRegistry.registerModEntity(EntitySphereGen.class    , "EntitySphereGenerator", WarpDriveConfig.G_ENTITY_SPHERE_GENERATOR_ID, WarpDrive.instance, 200, 1, false);
		EntityRegistry.registerModEntity(EntityStarCore.class     , "EntityStarCore"       , WarpDriveConfig.G_ENTITY_STAR_CORE_ID       , WarpDrive.instance, 300, 1, false);
		EntityRegistry.registerModEntity(EntityCamera.class       , "EntityCamera"         , WarpDriveConfig.G_ENTITY_CAMERA_ID          , WarpDrive.instance, 300, 1, false);
		EntityRegistry.registerModEntity(EntityParticleBunch.class, "EntityParticleBunch"  , WarpDriveConfig.G_ENTITY_PARTICLE_BUNCH_ID  , WarpDrive.instance, 300, 1, false);
	}
	
	public void registerRendering() {
		// client side only
	}
	
	private static EntityPlayerMP getPlayer(WorldServer world, final UUID uuidPlayer) {
		for (Object object : world.getMinecraftServer().getPlayerList().getPlayerList()) {
			if (object instanceof EntityPlayerMP) {
				EntityPlayerMP entityPlayerMP = (EntityPlayerMP) object;
				if (entityPlayerMP.getUniqueID() == uuidPlayer) {
					return entityPlayerMP;
				}
			}
		}
		return null;
	}
	
	private static EntityPlayer getFakePlayer(final UUID uuidPlayer, WorldServer world, BlockPos blockPos) {
		EntityPlayer entityPlayer = uuidPlayer == null ? null : getPlayer(world, uuidPlayer);
		GameProfile gameProfile = entityPlayer == null ? WarpDrive.gameProfile : entityPlayer.getGameProfile();
		WeakReference<EntityPlayer> weakFakePlayer = fakePlayers.get(gameProfile);
		EntityPlayer entityFakePlayer = (weakFakePlayer == null) ? null : weakFakePlayer.get();
		if (entityFakePlayer == null) {
			entityFakePlayer = FakePlayerFactory.get(world, gameProfile);
			((EntityPlayerMP)entityFakePlayer).interactionManager.setGameType(GameType.SURVIVAL);
			weakFakePlayer = new WeakReference<>(entityFakePlayer);
			fakePlayers.put(gameProfile, weakFakePlayer);
		} else {
			entityFakePlayer.worldObj = world;
		}
		entityFakePlayer.posX = blockPos.getX() + 0.5D;
		entityFakePlayer.posY = blockPos.getY() + 0.5D;
		entityFakePlayer.posZ = blockPos.getZ() + 0.5D;
		
		return entityFakePlayer;
	}
	
	public static EntityPlayer getFakePlayer(final UUID uuidPlayer, World world, final double x, final double y, final double z) {
		if (world.isRemote || !(world instanceof WorldServer)) {
			return null;
		}
		return getFakePlayer(uuidPlayer, world, x, y, z);
	}
	
	public static boolean isBlockBreakCanceled(final UUID uuidPlayer, BlockPos blockPosSource, World world, BlockPos blockPosEvent) {
		if (world.isRemote || !(world instanceof WorldServer)) {
			return false;
		}
		if (WarpDriveConfig.LOGGING_BREAK_PLACE) {
			WarpDrive.logger.info("isBlockBreakCanceled by " + uuidPlayer
			                      + " at " + blockPosSource.getX() + " " + blockPosSource.getY() + " " + blockPosSource.getZ()
			                      + " to " + world.provider.getDimensionType().getName() + " " + blockPosEvent.getX() + " " + blockPosEvent.getY() + " " + blockPosEvent.getZ());
		}
		
		IBlockState blockState = world.getBlockState(blockPosEvent);
		if (!blockState.getBlock().isAir(blockState, world, blockPosEvent)) {
			BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(
			    world, blockPosEvent, world.getBlockState(blockPosEvent),
				WarpDrive.proxy.getFakePlayer(uuidPlayer, (WorldServer) world, blockPosSource));
			MinecraftForge.EVENT_BUS.post(breakEvent);
			if (WarpDriveConfig.LOGGING_BREAK_PLACE) {
				WarpDrive.logger.info("isBlockBreakCanceled player " + breakEvent.getPlayer()
					+ " isCanceled " + breakEvent.isCanceled());
			}
			return breakEvent.isCanceled();
		}
		return false;
	}
	
	public static boolean isBlockPlaceCanceled(final UUID uuidPlayer, BlockPos blockPosSource, World world, BlockPos blockPosEvent, IBlockState blockState) {
		if (world.isRemote || !(world instanceof WorldServer)) {
			return false;
		}
		if (WarpDriveConfig.LOGGING_BREAK_PLACE) {
			WarpDrive.logger.info("isBlockPlaceCanceled by " + uuidPlayer
			                      + " at " + blockPosSource.getX() + " " + blockPosSource.getY() + " " + blockPosSource.getZ()
			                      + " to " + world.provider.getDimensionType().getName() + " " + blockPosEvent.getX() + " " + blockPosEvent.getY() + " " + blockPosEvent.getZ()
			                      + " of " + blockState);
		}
		BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(
			new BlockSnapshot(world, blockPosEvent, blockState), Blocks.AIR.getDefaultState(),
			WarpDrive.proxy.getFakePlayer(uuidPlayer, (WorldServer) world, blockPosSource) );
		
		MinecraftForge.EVENT_BUS.post(placeEvent);
		if (WarpDriveConfig.LOGGING_BREAK_PLACE) {
			WarpDrive.logger.info("isBlockPlaceCanceled player " + placeEvent.getPlayer() + " isCanceled " + placeEvent.isCanceled());
		}
		return placeEvent.isCanceled();
	}
	
	public void onForgePreInitialisation() {
		OBJLoader.INSTANCE.addDomain(MODID);
	}
}