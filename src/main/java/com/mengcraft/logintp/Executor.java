package com.mengcraft.logintp;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mengcraft.logintp.Main.nil;

public class Executor implements CommandExecutor, Listener {

    private final List<Location> loc = new ArrayList<>();
    private final Main main;

    private Iterator<Location> it;
    private int c = -1;

    Executor(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] in) {
        try {
            if (!Player.class.isInstance(sender) || !sender.isOp()) {
                throw new RuntimeException("You are not operator!");
            }
            return execute(Player.class.cast(sender), in);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
        }
        return false;
    }

    private final Map<UUID, BukkitTask> portal = new HashMap<>();

    @EventHandler
    public void handle(EntityPortalEnterEvent event) {
        if (!Mgr.INSTANCE.isPortalPortal() || !(event.getEntityType() == EntityType.PLAYER) || portal.containsKey(event.getEntity().getUniqueId())) {
            return;
        }

        portal.put(event.getEntity().getUniqueId(), main.run(80, () -> {
            portal.remove(event.getEntity().getUniqueId());
            portalIfPortal((Player) event.getEntity());
        }));
    }

    @EventHandler
    public void handle(EntityPortalExitEvent event) {
        val remove = portal.remove(event.getEntity().getUniqueId());
        if (!nil(remove)) {
            remove.cancel();
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("logintp.bypass")) {
            return;
        }
        main.run(() -> portal(event.getPlayer()));
    }

    private void portalIfPortal(Player p) {
        val b = p.getLocation().getBlock();
        if (b.getType() == Material.PORTAL) {
            main.run(() -> portal(p));
        }
    }

    @EventHandler
    public void handle(PlayerRespawnEvent event) {
        if (Mgr.INSTANCE.isPortalSpawn() && !event.getPlayer().hasPermission("logintp.bypass")) {
            Location location = nextLocation(event.getPlayer());
            if (!nil(location)) {
                event.setRespawnLocation(location);
            }
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        if (Mgr.INSTANCE.isPortalQuit() && !event.getPlayer().hasPermission("logintp.bypass")) {
            portal(event.getPlayer());
        }
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (Mgr.INSTANCE.isPortalVoid() && event.getEntityType() == EntityType.PLAYER && event.getCause() == DamageCause.VOID) {
            event.setCancelled(true);
            portal(Player.class.cast(event.getEntity()));
        }
    }

    protected void portal(Player p) {
        Location location = nextLocation(p);
        if (!nil(location)) {
            portal(p, location);
        }
    }

    private Location nextLocation(Player p) {
        if (nil(it) || !it.hasNext()) {
            if (loc.isEmpty()) {
                return p.getWorld().getSpawnLocation();
            }
            it = loc.iterator();
        }
        return it.next();
    }

    private void portal(Player player, Location location) {
        if (!nil(location.getWorld())) {
            Entity vehicle = player.getVehicle();
            if (!nil(vehicle)) {
                vehicle.eject();
                vehicle.teleport(location);
                main.run(() -> {
                    vehicle.setPassenger(player);
                });
            }
            player.teleport(location);
        }
    }

    public void load() {
        // Low version compatible code.
        String state = main.getConfig().getString("default", null);
        if (state != null) {
            add(convert(state));
            main.getConfig().set("default", null);
        }
        // For multiple location code.
        if (!loc.isEmpty()) loc.clear();
        val list = main.getConfig().getStringList("locations");
        for (String string : list) {
            add(convert(string));
        }
        Mgr.INSTANCE.load(main);
    }

    private void add(Location where) {
        if (where.getWorld() != null) {
            loc.add(where);
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
            if (loc.size() != 0) {
                portal(p, loc.get(c()));
            }
        } else if (args[0].equals("del")) {
            if (loc.size() != 0) {
                loc.remove(c != -1 ? (c != 0 ? c-- : 0) : 0);
                p.sendMessage(ChatColor.GOLD + "Done! Please save it.");
            }
        } else if (args[0].equals("add")) {
            loc.add(p.getLocation());
            c = loc.size() - 1;
            p.sendMessage(ChatColor.GOLD + "Done! Please save it.");
        } else if (args[0].equals("save")) {
            List<String> array = new ArrayList<>();
            for (Location where : loc) {
                if (where.getWorld() != null) array.add(convert(where));
            }
            main.getConfig().set("locations", array);
            main.saveConfig();

            p.sendMessage(ChatColor.GOLD + "Done!");
        } else if (args[0].equals("load")) {
            main.reloadConfig();
            load();
            p.sendMessage(ChatColor.GOLD + "Done!");
        } else if (args[0].equals("list")) {
            for (Location loc : this.loc) {
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
        if (c + 1 != loc.size()) {
            return (c = c + 1);
        }
        return (c = 0);
    }

}
