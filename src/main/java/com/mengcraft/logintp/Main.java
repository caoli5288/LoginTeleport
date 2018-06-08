package com.mengcraft.logintp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin {


    private UnmodifiableIterator<? extends Player> onlineitr;
    private Executor executor;

    public void onEnable() {
        saveDefaultConfig();

        String[] ad = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(ad);

        executor = new Executor(this);
        run(executor::load);// To avoid NPE if world load after plugin loaded

        getCommand("logintp").setExecutor(executor);
        getServer().getPluginManager().registerEvents(executor, this);

        if (Mgr.INSTANCE.isPortalFalling()) {
            Bukkit.getScheduler().runTaskTimer(this, this::avoidFalling, 1, 1);
        }

        try {
            new Metrics(this).start();
        } catch (Exception e) {
            getLogger().warning(e.toString());
        }
    }

    private void avoidFalling() {
        if (onlineitr == null || !onlineitr.hasNext()) {
            onlineitr = ImmutableList.copyOf(Bukkit.getOnlinePlayers()).iterator();
        }
        if (!onlineitr.hasNext()) {
            return;
        }
        Player nplayer = onlineitr.next();
        if (nplayer.isOnline()) {
            double y = nplayer.getLocation().getY();
            if (y <= -10) {
                executor.portal(nplayer);
            }
            return;
        }

        avoidFalling();
    }

    public void run(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public BukkitTask run(int later, Runnable runnable) {
        return getServer().getScheduler().runTaskLater(this, runnable, later);
    }

    public static boolean nil(Object i) {
        return i == null;
    }
}
