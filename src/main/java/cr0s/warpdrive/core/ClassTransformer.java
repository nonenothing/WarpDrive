package cr0s.warpdrive.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {
	
	private static HashMap<String, String> nodeMap = new HashMap<>();
	public static ConcurrentSkipListMap<String, Integer> countClass = new ConcurrentSkipListMap<>();
	public static ConcurrentHashMap<String, Long> sizeClass = new ConcurrentHashMap<>(8192);
	
	private static final String GRAVITY_MANAGER_CLASS = "cr0s/warpdrive/data/GravityManager";
	private static final String CLOAK_MANAGER_CLASS = "cr0s/warpdrive/data/CloakManager";
	private boolean debugLog = false;
	
	public ClassTransformer() {
		nodeMap.put("EntityLivingBase.class", "vn");
		nodeMap.put("travel.name", "func_191986_a");
		nodeMap.put("travel.desc", "(FFF)V");
		
		nodeMap.put("EntityItem.class", "acj");
		nodeMap.put("onUpdate.name", "func_70071_h_");
		nodeMap.put("onUpdate.desc", "()V");
		
		nodeMap.put("WorldClient.class", "brz");
		nodeMap.put("invalidateRegionAndSetBlock.name", "func_180503_b");
		nodeMap.put("invalidateRegionAndSetBlock.desc", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z");
		nodeMap.put("setBlockState.name", "func_180501_a");
		
		nodeMap.put("Chunk.class", "axu");
		nodeMap.put("read.name", "func_186033_a");
		nodeMap.put("read.desc", "(Lnet/minecraft/network/PacketBuffer;IZ)V");
		nodeMap.put("generateHeightMap.name", "func_76590_a");
		nodeMap.put("generateHeightMap.desc", "()V");
	}
	
	@Override
	public byte[] transform(final String name, final String transformedName, byte[] bytes) {
		if (nodeMap == null) {
			FMLLoadingPlugin.logger.info(String.format("Nodemap is null, transformation cancelled for %s", name));
			return bytes;
		}
		if (bytes == null) {
			if (debugLog) {
				FMLLoadingPlugin.logger.trace(String.format("bytes is null, transformation cancelled for %s", name));
			}
			return null;
		}
		
		// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Checking %s", name)); }
		saveClassToFile(false, transformedName, bytes);
		switch (transformedName) {
		case "net.minecraft.entity.EntityLivingBase":
			bytes = transformMinecraftEntityLivingBase(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
			
		case "net.minecraft.entity.item.EntityItem":
			bytes = transformMinecraftEntityItem(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
			
		case "com.creativemd.itemphysic.physics.ServerPhysic":
			bytes = transformItemPhysicEntityItem(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
			
		case "micdoodle8.mods.galacticraft.core.util.WorldUtil":
			bytes = transformGalacticraftWorldUtil(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
			
		case "net.minecraft.client.multiplayer.WorldClient":
			bytes = transformMinecraftWorldClient(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
			
		case "net.minecraft.world.chunk.Chunk":
			bytes = transformMinecraftChunk(bytes);
			saveClassToFile(true, transformedName, bytes);
			break;
		}
		
		try {
			collectClientValidation(transformedName, bytes);
		} catch (final Exception exception) {
			// nop
		}
		
		return bytes;
	}
	
	private static void collectClientValidation(final String transformedName, final byte[] bytes) {
		final String[] names = transformedName.split("[.$]");
		final String shortName = names[0] + "." + (names.length > 1 ? names[1] : "");
		Integer count = countClass.get(shortName);
		Long size = sizeClass.get(shortName);
		if (count == null) {
			count = 0;
			size = 0L;
		}
		countClass.put(shortName, count + 1);
		sizeClass.put(shortName, size + bytes.length);
	}
	
	public static String getClientValidation() {
		final StringBuilder result = new StringBuilder().append(new Date().toString());
		for (final String key : countClass.keySet()) {
			result.append("\n").append(key)
			      .append("\t").append(countClass.get(key))
			      .append("\t").append(sizeClass.get(key));
		}
		return result.toString();
	}
	
	private static String SHA1(final byte[] bytes) {
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			final Formatter formatter = new Formatter();
			for (final byte byteValue : md.digest(bytes)) {
				formatter.format("%02x", byteValue);
			}
			return formatter.toString();
		} catch (final Exception exception) {
			return exception.getMessage();
		}
	}
	
	private byte[] transformMinecraftEntityLivingBase(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 2;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }
			
			if ( (methodNode.name.equals(nodeMap.get("travel.name")) || methodNode.name.equals("travel"))
			  && methodNode.desc.equals(nodeMap.get("travel.desc")) ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						final LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(-0.080000000000000002D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getNegGravityForEntity",
									"(Lnet/minecraft/entity/Entity;)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
							
						} else if (nodeAt.cst.equals(0.080000000000000002D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getGravityForEntity",
									"(Lnet/minecraft/entity/Entity;)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private byte[] transformMinecraftEntityItem(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 2;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }
			
			if ( (methodNode.name.equals(nodeMap.get("onUpdate.name")) || methodNode.name.equals("onUpdate"))
			  && methodNode.desc.equals(nodeMap.get("onUpdate.desc")) ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						final LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(0.039999999105930328D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
						
						if (nodeAt.cst.equals(0.98000001907348633D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity2",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private byte[] transformItemPhysicEntityItem(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 2;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }
			
			if ( (methodNode.name.equals("update"))
			  && methodNode.desc.equals("(Lnet/minecraft/entity/item/EntityItem;)V") ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						final LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(0.04D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
						
						if (nodeAt.cst.equals(0.98D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity2",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private byte[] transformGalacticraftWorldUtil(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 3 + 2 + 0;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }

			// Entities gravity
			if ( (methodNode.name.equals("getGravityForEntity"))
			  && methodNode.desc.equals("(Lnet/minecraft/entity/Entity;)D") ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						final LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(0.08D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getGravityForEntity",
									"(Lnet/minecraft/entity/Entity;)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
			
			// Items gravity
			if ( (methodNode.name.equals("getItemGravity"))
			  && methodNode.desc.equals("(Lnet/minecraft/entity/item/EntityItem;)D") ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof LdcInsnNode) {
						final LdcInsnNode nodeAt = (LdcInsnNode) abstractNode;
						
						if (nodeAt.cst.equals(0.03999999910593033D)) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
						/*
						if (nodeAt.cst.equals(Double.valueOf(0.98D))) {
							final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									GRAVITY_MANAGER_CLASS,
									"getItemGravity2",
									"(L" + "net/minecraft/entity/item/EntityItem" + ";)D",
									false);
							methodNode.instructions.insertBefore(nodeAt, beforeNode);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
						/**/
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount < operationCount) {// https://github.com/micdoodle8/Galacticraft/commit/d0e7e9ae932092e8a4584bac43338d2dc1bbfe23 added 2 occurrences
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private byte[] transformMinecraftWorldClient(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 1;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			// if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }
			
			if ( (methodNode.name.equals(nodeMap.get("invalidateRegionAndSetBlock.name")) || methodNode.name.equals("invalidateRegionAndSetBlock"))
			  && methodNode.desc.equals(nodeMap.get("invalidateRegionAndSetBlock.desc")) ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					
					if (abstractNode instanceof MethodInsnNode) {
						final MethodInsnNode nodeAt = (MethodInsnNode) abstractNode;
						
						if (nodeAt.name.equals(nodeMap.get("setBlockState.name")) || nodeAt.name.equals("setBlockState")) {
							final MethodInsnNode overwriteNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									CLOAK_MANAGER_CLASS,
									"WorldClient_invalidateRegionAndSetBlock_setBlockState",
									"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
									false);
							methodNode.instructions.set(nodeAt, overwriteNode);
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private byte[] transformMinecraftChunk(byte[] bytes) {
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		final int operationCount = 1;
		int injectedCount = 0;
		final Iterator methods = classNode.methods.iterator();
		do {
			if (!methods.hasNext()) {
				break;
			}
			
			final MethodNode methodNode = (MethodNode) methods.next();
			if (debugLog) { FMLLoadingPlugin.logger.info(String.format("- Method %s %s", methodNode.name, methodNode.desc)); }
			
			if ( (methodNode.name.equals(nodeMap.get("read.name")) || methodNode.name.equals("read"))
			  && methodNode.desc.equals(nodeMap.get("read.desc")) ) {
				if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Found method to inject into: %s %s", methodNode.name, methodNode.desc)); }
				
				int instructionIndex = 0;
				
				while (instructionIndex < methodNode.instructions.size()) {
					final AbstractInsnNode abstractNode = methodNode.instructions.get(instructionIndex);
					if (debugLog) { decompile(abstractNode); }
					
					if (abstractNode instanceof MethodInsnNode) {
						final MethodInsnNode nodeAt = (MethodInsnNode) abstractNode;
						
						if ( (nodeAt.name.equals(nodeMap.get("generateHeightMap.name")) || nodeAt.name.equals("generateHeightMap"))
						  && nodeAt.desc.equals(nodeMap.get("generateHeightMap.desc")) ) {
							final MethodInsnNode insertMethodNode = new MethodInsnNode(
									Opcodes.INVOKESTATIC,
									CLOAK_MANAGER_CLASS,
									"Chunk_read",
									"(Lnet/minecraft/world/chunk/Chunk;)V",
									false);
							methodNode.instructions.insertBefore(nodeAt, insertMethodNode);
							instructionIndex++;
							
							final VarInsnNode insertVarNode = new VarInsnNode(Opcodes.ALOAD, 0);
							methodNode.instructions.insertBefore(nodeAt, insertVarNode);
							instructionIndex++;
							
							if (debugLog) { FMLLoadingPlugin.logger.info(String.format("Injecting into %s.%s %s", classNode.name, methodNode.name, methodNode.desc)); }
							injectedCount++;
						}
					}
					
					instructionIndex++;
				}
			}
		} while (true);
		
		if (injectedCount != operationCount) {
			FMLLoadingPlugin.logger.info(String.format("Injection failed for %s (%d/%d), aborting...", classNode.name, injectedCount, operationCount));
		} else {
			final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			bytes = writer.toByteArray();
			FMLLoadingPlugin.logger.info(String.format("Successful injection in %s", classNode.name));
		}
		return bytes;
	}
	
	private void saveClassToFile(final boolean isAfter, final String nameClass, final byte[] bytes) {
		if (debugLog) {
			try {
				final File fileDirRoot = new File(isAfter ? "asm" : "asm");
				if (!fileDirRoot.exists() && !fileDirRoot.mkdirs()) {
					FMLLoadingPlugin.logger.info("Unable to create ASM dump folder, skipping...");
					return;
				}
				final File fileDir = new File(isAfter ? "asm/warpdrive.after" : "asm/warpdrive.before");
				if (!fileDir.exists() && !fileDir.mkdirs()) {
					FMLLoadingPlugin.logger.info("Unable to create ASM dump sub-folder, skipping...");
					return;
				}
				
				final String nameClass_clean = nameClass.replace("/", "_").replace("\\", "_").replace(" ", "_");
				final File fileClass = new File(fileDir, nameClass_clean + ".class");
				final FileOutputStream fileOutputStream = new FileOutputStream(fileClass);
				final DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				dataOutputStream.write(bytes);
				dataOutputStream.flush();
				dataOutputStream.close();
			} catch (final Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	private static boolean opcodeToString_firstDump = true;
	private static String opcodeToString(final int opcode) {
		final Field[] fields = Opcodes.class.getFields();
		for (final Field field : fields) {
			if (field.getType() == int.class) {
				try {
					if (field.getInt(null) == opcode){
						return field.getName();
					}
				} catch (final Throwable throwable){
					if (opcodeToString_firstDump) {
						throwable.printStackTrace();
						opcodeToString_firstDump = false;
					}
				}
			}
		}
		return String.format("0x%x", opcode);
	}
	
	private static void decompile(final AbstractInsnNode abstractNode) {
		final String opcode = opcodeToString(abstractNode.getOpcode());
		if (abstractNode instanceof VarInsnNode) {
			final VarInsnNode node = (VarInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Var", node.var));
			
		} else if (abstractNode instanceof LabelNode) {
			final LabelNode node = (LabelNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Label", node.getLabel()));
			
		} else if (abstractNode instanceof LineNumberNode) {
			final LineNumberNode node = (LineNumberNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Line", node.line));
			
		} else if (abstractNode instanceof TypeInsnNode) {
			final TypeInsnNode node = (TypeInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Typed instruction", node.desc));
			
		} else if (abstractNode instanceof JumpInsnNode) {
			final JumpInsnNode node = (JumpInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Jump", node.label.getLabel()));
			
		} else if (abstractNode instanceof FrameNode) {
			final FrameNode node = (FrameNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %d %s %s", opcode, "Frame", node.type, node.local, node.stack));
			
		} else if (abstractNode instanceof InsnNode) {
			final InsnNode node = (InsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Instruction", node));
			
		} else if (abstractNode instanceof LdcInsnNode) {
			final LdcInsnNode node = (LdcInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s", opcode, "Load", node.cst));
			
		} else if (abstractNode instanceof FieldInsnNode) {
			final FieldInsnNode node = (FieldInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s %s %s", opcode, "Field", node.owner, node.name, node.desc));
			
		} else if (abstractNode instanceof MethodInsnNode) {
			final MethodInsnNode node = (MethodInsnNode) abstractNode;
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s %s %s %s", opcode, "Method", node.owner, node.name, node.desc, node.itf));
			
		} else {
			FMLLoadingPlugin.logger.info(String.format("%20s %-20s %s %s %s", opcode, "Instruction", abstractNode.getOpcode(), abstractNode.getType(), abstractNode));
		}
	}
}
