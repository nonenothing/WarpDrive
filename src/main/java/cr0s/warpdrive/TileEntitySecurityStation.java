package cr0s.warpdrive;

import cr0s.warpdrive.api.computer.ISecurityStation;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;

public class TileEntitySecurityStation extends TileEntityAbstractInterfaced implements ISecurityStation {
	
	// persistent properties
	public final ArrayList<String> players = new ArrayList<>();
	
	public TileEntitySecurityStation() {
		super();
		
		// (abstract) peripheralName = "warpdriveSecurityStation";
		addMethods(new String[] {
				"getAttachedPlayers"
				});
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		players.clear();
		final NBTTagList tagListPlayers = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
		for (int index = 0; index < tagListPlayers.tagCount(); index++) {
			final String namePlayer = tagListPlayers.getStringTagAt(index);
			players.add(namePlayer);
		}
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		
		final NBTTagList tagListPlayers = new NBTTagList();
		for (final String namePlayer : players) {
			final NBTTagString tagStringPlayer = new NBTTagString(namePlayer);
			tagListPlayers.appendTag(tagStringPlayer);
		}
		tagCompound.setTag("players", tagListPlayers);
		
		return tagCompound;
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeItemDropNBT(tagCompound);
		
		tagCompound.removeTag("players");
		
		return tagCompound;
	}
	
	@Override
	public ITextComponent getStatus() {
		return super.getStatus()
		            .appendSibling(new TextComponentTranslation("warpdrive.ship.attached_players",
		                                                        getAttachedPlayersList()));
	}
	
	public ITextComponent attachPlayer(final EntityPlayer entityPlayer) {
		for (int i = 0; i < players.size(); i++) {
			final String name = players.get(i);
			
			if (entityPlayer.getName().equals(name)) {
				players.remove(i);
				return Commons.getChatPrefix(getBlockType())
				              .appendSibling(new TextComponentTranslation("warpdrive.ship.player_detached",
				                                                          "-",  // @TODO named block
				                                                          getAttachedPlayersList()));
			}
		}
		
		entityPlayer.attackEntityFrom(DamageSource.GENERIC, 1);
		players.add(entityPlayer.getName());
		return Commons.getChatPrefix(getBlockType())
		              .appendSibling(new TextComponentTranslation("warpdrive.ship.player_attached",
		                                                          "-",  // @TODO named block
		                                                          getAttachedPlayersList()));
	}
	
	protected String getAttachedPlayersList() {
		if (players.isEmpty()) {
			return "<nobody>";
		}
		
		final StringBuilder list = new StringBuilder();
		
		for (int i = 0; i < players.size(); i++) {
			final String nick = players.get(i);
			list.append(nick).append(((i == players.size() - 1) ? "" : ", "));
		}
		
		return list.toString();
	}
	
	public String getFirstOnlinePlayer() {
		if (players == null || players.isEmpty()) {// no crew defined
			return null;
		}
		
		for (final String namePlayer : players) {
			final EntityPlayer entityPlayer = Commons.getOnlinePlayerByName(namePlayer);
			if (entityPlayer != null) {// crew member is online
				return namePlayer;
			}
		}
		
		// all cleared
		return null;
	}
	
	// Common OC/CC methods
	@Override
	public Object[] getAttachedPlayers() {
		final StringBuilder list = new StringBuilder();
		
		if (!players.isEmpty()) {
			for (int i = 0; i < players.size(); i++) {
				final String nick = players.get(i);
				list.append(nick).append((i == players.size() - 1) ? "" : ",");
			}
		}
		
		return new Object[] { list.toString(), players.toArray() };
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getAttachedPlayers(final Context context, final Arguments arguments) {
		return getAttachedPlayers();
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(@Nonnull final IComputerAccess computer, @Nonnull final ILuaContext context, final int method, @Nonnull final Object[] arguments) {
		final String methodName = CC_getMethodNameAndLogCall(method, arguments);
		
		switch (methodName) {
		case "getAttachedPlayers":
			return getAttachedPlayers();
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
