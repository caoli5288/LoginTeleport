package com.mengcraft.bukkit;

import com.google.gson.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class LoginTeleport extends JavaPlugin {
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new TeleportListener(), this);
		getCommand("login-tp").setExecutor(new Commander());
		String[] strings = {
				ChatColor.GREEN + "梦梦家高性能服务器出租",
				ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com"
		};
		getServer().getConsoleSender().sendMessage(strings);
		try {
			new Metrics(this).start();
		} catch (IOException e) {
			getLogger().warning("Can not link to Metrics server!");
		}
	}

	private class TeleportListener implements Listener {
		@EventHandler
		public void onJoin(PlayerJoinEvent event) {
			if (!event.getPlayer().hasPlayedBefore()) {
				String place = getConfig().getString("default");
				if (place != null) {
					JsonArray array = new JsonParser().parse(place).getAsJsonArray();
					World world = getServer().getWorld(array.get(0).getAsString());
					double x = array.get(1).getAsDouble();
					double y = array.get(2).getAsDouble();
					double z = array.get(3).getAsDouble();
					float yaw = array.get(4).getAsFloat();
					float pitch = array.get(5).getAsFloat();
					event.getPlayer().teleport(new Location(world, x, y, z, yaw, pitch));
				}
			}
		}

		@EventHandler
		public void playerQuit(PlayerQuitEvent event) {
			String place = getConfig().getString("default");
			if (place != null) {
				JsonArray array = new JsonParser().parse(place).getAsJsonArray();
				World world = getServer().getWorld(array.get(0).getAsString());
				event.getPlayer().leaveVehicle();
				if (array.size() > 4) {
					event.getPlayer().teleport(
							new Location(world, array.get(1).getAsDouble(), array.get(2).getAsDouble(), array.get(3).getAsDouble(), array.get(4).getAsFloat(),
									array.get(5).getAsFloat()));
				} else {
					event.getPlayer().teleport(new Location(world, array.get(1).getAsDouble(), array.get(2).getAsDouble(), array.get(3).getAsDouble()));
				}
			}
		}
	}

	public class Commander implements CommandExecutor {

		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (sender instanceof Player && sender.isOp()) {
				Location location = getServer().getPlayer(sender.getName()).getLocation();
				JsonArray array = new JsonArray();
				Gson gson = new Gson();
				array.add(gson.toJsonTree(location.getWorld().getName()));
				array.add(gson.toJsonTree(location.getX()));
				array.add(gson.toJsonTree(location.getY()));
				array.add(gson.toJsonTree(location.getZ()));
				array.add(gson.toJsonTree(location.getYaw()));
				array.add(gson.toJsonTree(location.getPitch()));
				getConfig().set("default", array.toString());
				saveConfig();
				sender.sendMessage(ChatColor.GREEN + "设置成功");
			}
			return true;
		}
	}
}
