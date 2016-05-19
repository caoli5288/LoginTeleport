package com.mengcraft.logintp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Executor implements CommandExecutor, Listener {

    private final List<Location> a = new ArrayList<>();
    private final Main main;
    private final Config config;

    private boolean b = true;
    private int cursor;
    private int c = -1;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            if (!Player.class.isInstance(sender) || !sender.isOp()) {
                throw new RuntimeException("You are not operator!");
            }
            return execute(Player.class.cast(sender), args);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
        }
        return false;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("logintp.bypass")) {
            main.getServer().getScheduler().runTask(main, new Teleport(event.getPlayer()));
        }
    }

    @EventHandler
    public void handle(PlayerRespawnEvent event) {
        if (config.isPortalQuit() && !event.getPlayer().hasPermission("logintp.bypass")) {
            main.getServer().getScheduler().runTask(main, new Teleport(event.getPlayer()));
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        if (config.isPortalQuit() && !event.getPlayer().hasPermission("logintp.bypass")) {
            new Teleport(event.getPlayer()).run();
        }
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (config.isPortalVoid() && event.getEntityType() == EntityType.PLAYER && event.getCause() == DamageCause.VOID) {
            main.getServer().getScheduler().runTask(main, new Teleport((Player) event.getEntity()));
        }
    }

    private class Teleport implements Runnable {

        private final Player player;

        public void run() {
            if (a.size() != 0) {
                teleport(player, a.get(cursor()));
            }
        }

        private int cursor() {
            if (cursor < a.size()) {
                return cursor++;
            }
            return cursor = 0;
        }

        public Teleport(Player player) {
            this.player = player;
        }

    }

    public void register() {
        // Low version compatible code.
        String state = main.getConfig().getString("default", null);
        if (state != null) {
            add(convert(state));
            main.getConfig().set("default", null);
        }
        if (b) {
            main.getCommand("logintp").setExecutor(this);
            main.getServer().getPluginManager().registerEvents(this, main);
            b = !b;
        } else {
            a.clear();
        }
        // For multiple location code.
        for (String string : main.getConfig().getStringList("locations")) {
            add(convert(string));
        }
        config.load();
    }

    private void teleport(Player player, Location location) {
        if (location.getWorld() != null) try {
            // Force load chunk.
            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }
            // Force down vehicle.
            if (player.getVehicle() != null) player.getVehicle().eject();
            player.teleport(location);
        } catch (Exception e) {
            main.getLogger().warning(e.toString());
        }
    }

    private void add(Location where) {
        if (where.getWorld() != null) {
            a.add(where);
        }
    }

    private Location convert(String string) {
        Location where = new Location(null, 0, 0, 0);
        try {
            Iterator it = ((List) new JSONParser().parse(string)).iterator();
            String worldName = (String) it.next();
            World world = main.getServer().getWorld(worldName);
            // Check if world be removed.
            if (world != null) {
                where.setWorld(world);

                where.setX((double) it.next());
                where.setY((double) it.next());
                where.setZ((double) it.next());

                where.setYaw((float) (double) it.next());
                where.setPitch((float) (double) it.next());
            }
        } catch (ParseException e) {
            main.getLogger().warning(e.toString());
        }
        return where;
    }

    public Executor(Main main, Config config) {
        this.main = main;
        this.config = config;
    }

    private boolean execute(Player p, String[] args) {
        if (args.length < 1) {
            p.sendMessage(new String[]{
                    ChatColor.GOLD + "/logintp next",
                    ChatColor.GOLD + "/logintp del",
                    ChatColor.GOLD + "/logintp add",
                    ChatColor.GOLD + "/logintp save",
                    ChatColor.GOLD + "/logintp load",
                    ChatColor.GOLD + "/logintp list"
            });
        } else if (args[0].equals("next")) {
            if (a.size() != 0) {
                teleport(p, a.get(c()));
            }
        } else if (args[0].equals("del")) {
            if (a.size() != 0) {
                a.remove(c != -1 ? (c != 0 ? c-- : 0) : 0);
                p.sendMessage(ChatColor.GOLD + "Done! Please save it.");
            }
        } else if (args[0].equals("add")) {
            a.add(p.getLocation());
            c = a.size() - 1;
            p.sendMessage(ChatColor.GOLD + "Done! Please save it.");
        } else if (args[0].equals("save")) {
            List<String> array = new ArrayList<>();
            for (Location where : a) {
                if (where.getWorld() != null) array.add(convert(where));
            }
            main.getConfig().set("locations", array);
            main.saveConfig();

            p.sendMessage(ChatColor.GOLD + "Done!");
        } else if (args[0].equals("load")) {
            main.reloadConfig();
            register();
            p.sendMessage(ChatColor.GOLD + "Done!");
        } else if (args[0].equals("list")) {
            for (Location loc : a) {
                p.sendMessage(ChatColor.GOLD + convert(loc));
            }
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private String convert(Location where) {
        JSONArray array = new JSONArray();
        array.add(where.getWorld().getName());
        array.add(where.getX());
        array.add(where.getY());
        array.add(where.getZ());
        array.add(where.getYaw());
        array.add(where.getPitch());

        return array.toJSONString();
    }

    private int c() {
        if (c + 1 != a.size()) {
            return (c = c + 1);
        }
        return (c = 0);
    }

}
