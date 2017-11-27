package com.mengcraft.logintp;

import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin {

    public void onEnable() {
        saveDefaultConfig();

        String[] ad = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(ad);

        val exec = new Executor(this);

        getCommand("logintp").setExecutor(exec);
        getServer().getPluginManager().registerEvents(exec, this);

        exec.load();

        try {
            new Metrics(this).start();
        } catch (Exception e) {
            getLogger().warning(e.toString());
        }
    }

    public BukkitTask run(Runnable runnable) {
        return getServer().getScheduler().runTask(this, runnable);
    }

    public BukkitTask run(int later, Runnable runnable) {
        return getServer().getScheduler().runTaskLater(this, runnable, later);
    }

    public static boolean nil(Object i) {
        return i == null;
    }
}
