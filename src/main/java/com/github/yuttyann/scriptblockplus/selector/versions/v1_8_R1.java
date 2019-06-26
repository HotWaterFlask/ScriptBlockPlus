package com.github.yuttyann.scriptblockplus.selector.versions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.google.common.base.Joiner;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.CommandBlockListenerAbstract;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.ICommandListener;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.PlayerSelector;
import net.minecraft.server.v1_8_R1.TileEntityCommand;
import net.minecraft.server.v1_8_R1.WorldServer;

public final class v1_8_R1 extends Vx_x_Rx {

	@Override
	public int executeCommand(Object listener, CommandSender bSender, Location location, String command) {
		if (command.charAt(0) == '/') {
			command = command.substring(1);
		}
		ICommandListener sender = (ICommandListener) listener;
		SimpleCommandMap commandMap = sender.getWorld().getServer().getCommandMap();
		Joiner joiner = Joiner.on(" ");
		String[] args = StringUtils.split(command, " ");
		List<String[]> commands = new ArrayList<>();
		String cmd = args[0];
		if (cmd.startsWith("minecraft:")) {
			cmd = cmd.substring("minecraft:".length());
		}
		if (cmd.startsWith("bukkit:")) {
			cmd = cmd.substring("bukkit:".length());
		}
		if (cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("op")
				|| cmd.equalsIgnoreCase("deop") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("ban-ip")
				|| cmd.equalsIgnoreCase("pardon") || cmd.equalsIgnoreCase("pardon-ip") || cmd.equalsIgnoreCase("reload")
				|| sender.getWorld().players.isEmpty() || commandMap.getCommand(args[0]) == null) {
			return 0;
		}
		commands.add(args);
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer[] prev = server.worldServer;
		server.worldServer = new WorldServer[server.worlds.size()];
		server.worldServer[0] = (WorldServer) sender.getWorld();
		for (int pos = 1, bpos = 0; pos < server.worldServer.length; pos++) {
			WorldServer world = server.worlds.get(bpos++);
			if (server.worldServer[0] == world) {
				pos--;
				continue;
			}
			server.worldServer[pos] = world;
		}
		try {
			List<String[]> newCommands = new ArrayList<>();
			for (int i = 0; i < args.length; i++) {
				if (PlayerSelector.isPattern(args[i])) {
					for (int j = 0; j < commands.size(); j++) {
						newCommands.addAll(buildCommands(sender, commands.get(j), i));
					}
					List<String[]> temp = commands;
					commands = newCommands;
					newCommands = temp;
					newCommands.clear();
				}
			}
		} finally {
			server.worldServer = prev;
		}
		int completed = 0;
		for (int i = 0; i < commands.size(); i++) {
			try {
				if (commandMap.dispatch(bSender, joiner.join(Arrays.asList(commands.get(i))))) {
					completed++;
				}
			} catch (Throwable exception) {
				String message = "CommandBlock at (%d,%d,%d) failed to handle command";
				BlockPosition position = sender.getChunkCoordinates();
				server.server.getLogger().log(Level.WARNING, String.format(message, position.getX(), position.getY(), position.getZ()), exception);
			}
		}
		return completed;
	}

	@Override
	protected ICommandListener getListener(CommandSender sender, Location location) throws ReflectiveOperationException {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		TileEntityCommand tileEntityCommand = new TileEntityCommand();
		tileEntityCommand.a(((CraftWorld) location.getWorld()).getHandle());
		tileEntityCommand.a(new BlockPosition(x, y, z));
		CommandBlockListenerAbstract commandListener = tileEntityCommand.getCommandBlock();
		if (sender != null) {
			commandListener.setName(sender.getName());
		}
		return commandListener;
	}

	private List<String[]> buildCommands(ICommandListener sender, String[] args, int pos) {
		List<String[]> commands = new ArrayList<>();
		List<EntityPlayer> playerList = PlayerSelector.getPlayers(sender, args[pos], EntityPlayer.class);
		EntityPlayer[] players = playerList.toArray(new EntityPlayer[playerList.size()]);
		if (players != null) {
			for (EntityPlayer player : players) {
				if (player.world != sender.getWorld()) {
					continue;
				}
				String[] command = args.clone();
				command[pos] = player.getName();
				commands.add(command);
			}
		}
		return commands;
	}
}