package com.mengcraft.logintp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("rawtypes")
public class Executor implements CommandExecutor, Listener {

    private final Location where;
    private final Main main;

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String lable, String[] args) {
        try {
            if (!sender.isOp()) {
                throw new RuntimeException("You are not OP!");
            }
            // Override field location.
            ((Player) sender).getLocation(where);

            List array = Arrays.asList(
                    where.getWorld().getName(),
                    where.getX(),
                    where.getY(),
                    where.getZ(),
                    where.getYaw(),
                    where.getPitch()
                    );

            main.getConfig().set("default", JSONArray.toJSONString(array));
            main.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "设置成功");

            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
        }
        return false;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        main.getServer()
            .getScheduler()
            .runTaskLater(main, new Teleport(event.getPlayer()), 1);
    }

    private class Teleport implements Runnable {

        private final Player player;

        public void run() {
            if (where.getWorld() != null) try {
                // Force load chunk.
                // Force wait chunk loaded.
                while (!where.getChunk().isLoaded()) {
                    where.getChunk().load();
                }
                // Force down vehicle.
                if (player.getVehicle() != null) player.getVehicle().eject();
                // And teleport the man.
                player.teleport(where);
            } catch (Exception e) {
                main.getLogger().warning(e.toString());
            }
        }

        public Teleport(Player player) {
            this.player = player;
        }

    }

    public void register() throws RuntimeException {
        String json = main.getConfig().getString("default");
        if (json != null) try {
            List array = (List) new JSONParser().parse(json);
            Iterator it = array.iterator();
            String worldName = (String) it.next();
            World world = main.getServer().getWorld(worldName);
            // Check if world be removed.
            if (world == null) throw new NullPointerException();
            where.setWorld(world);
            where.setX((double) it.next());
            where.setY((double) it.next());
            where.setZ((double) it.next());
            where.setYaw(new Float((double) it.next()));
            where.setPitch(new Float((double) it.next()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        main.getCommand("login-tp").setExecutor(this);
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public Executor(Main main) {
        this.main = main;
        this.where = new Location(null, 0, 0, 0);
    }

}
