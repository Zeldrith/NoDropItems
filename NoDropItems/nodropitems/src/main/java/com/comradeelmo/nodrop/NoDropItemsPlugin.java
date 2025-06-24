package com.comradeelmo.nodrop;

import org.bukkit.plugin.java.JavaPlugin;

public class NoDropItemsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DeathItemFilter filter = new DeathItemFilter(this);
        ItemGUI itemGUI = new ItemGUI(this, filter);

        getServer().getPluginManager().registerEvents(filter, this);
        getServer().getPluginManager().registerEvents(itemGUI, this);

        getCommand("reloadnodrop").setExecutor(new ReloadCommand(this, filter));
        getCommand("nodropgui").setExecutor(new NoDropCommand(itemGUI));
    }

    @Override
    public void onDisable() {
        // Nothing needed here for now
    }
}
