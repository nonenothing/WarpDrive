package cr0s.warpdrive.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class ParticleRegistry {
	
	private static BiMap<String, Particle> particles = HashBiMap.create();
	
	public static final Particle ION = new Particle("ion") { }.setColor(0xE5FF54).setRarity(EnumRarity.common);
	public static final Particle PROTON = new Particle("proton") { }.setColor(0xE5FF54).setRarity(EnumRarity.common);
	public static final Particle ANTIMATTER = new Particle("antimatter") { }.setColor(0x1C3CAF).setRarity(EnumRarity.uncommon);
	public static final Particle STRANGE_MATTER = new Particle("strange_matter") { }.setColor(0xE2414C).setRarity(EnumRarity.rare);
	// public static final Particle TACHYONS = new Particle("tachyons") { }.setColor(0xE5FF54).setRarity(EnumRarity.epic);
	
	static {
		registerParticle(ION);
		registerParticle(PROTON);
		registerParticle(ANTIMATTER);
		registerParticle(STRANGE_MATTER);
		// registerParticle(TACHYONS);
	}
	
	private ParticleRegistry() {
		
	}
	
	public static boolean registerParticle(Particle particle) {
		if (particles.containsKey(particle.getRegistryName())) {
			FMLLog.getLogger().error(String.format("Mod %s FAILED to register particle %s: it was already registered!", getActiveModId(), particle.getRegistryName()));
			return false;
		}
		particles.put(particle.getRegistryName(), particle);
		
		FMLLog.getLogger().info(String.format("Mod %s has registered particle %s", getActiveModId(), particle.getRegistryName()));
		MinecraftForge.EVENT_BUS.post(new ParticleRegisterEvent(particle.getRegistryName()));
		return true;
	}
	
	private static String getActiveModId() {
		ModContainer activeModContainer = Loader.instance().activeModContainer();
		return activeModContainer == null ? "minecraft" : activeModContainer.getModId();
	}
	
	public static boolean isParticleRegistered(Particle particle) {
		return particle != null && particles.containsKey(particle.getRegistryName());
	}
	
	public static boolean isParticleRegistered(String particleRegistryName) {
		return particles.containsKey(particleRegistryName);
	}
	
	public static Particle getParticle(String particleRegistryName) {
		return particles.get(particleRegistryName);
	}
	
	public static String getParticleName(Particle particle) {
		return particle.getRegistryName();
	}
	
	public static String getParticleName(ParticleStack stack) {
		return stack.getParticle().getRegistryName();
	}
	
	public static ParticleStack getParticleStack(String particleRegistryName, int amount) {
		if (!particles.containsKey(particleRegistryName)) {
			return null;
		}
		return new ParticleStack(getParticle(particleRegistryName), amount);
	}
	
	public static Map<String, Particle> getRegisteredParticles()
	{
		return ImmutableMap.copyOf(particles);
	}
		
	public static class ParticleRegisterEvent extends Event {
		public final String particleRegistryName;
		
		public ParticleRegisterEvent(final String particleRegistryName) {
			this.particleRegistryName = particleRegistryName;
		}
	}
}
