package com.mengcraft.logintp;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        String[] ad = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(ad);

        Executor executor = new Executor(this, new Config(this));

        getCommand("logintp").setExecutor(executor);
        getServer().getPluginManager().registerEvents(executor, this);

        executor.load();

        try {
            new Metrics(this).start();
        } catch (Exception e) {
            getLogger().warning(e.toString());
        }
    }

    public void run(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public static boolean nil(Object i) {
        return i == null;
    }
}
