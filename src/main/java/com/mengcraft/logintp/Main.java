package com.mengcraft.logintp;

import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public void onEnable() {
        saveDefaultConfig();

        if (getConfig().get("portal.portal", null) == null) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

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

    public void run(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public static boolean nil(Object i) {
        return i == null;
    }
}
