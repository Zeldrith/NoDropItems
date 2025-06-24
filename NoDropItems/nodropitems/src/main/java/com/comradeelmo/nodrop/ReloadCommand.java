package com.comradeelmo.nodrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final DeathItemFilter filter;

    public ReloadCommand(JavaPlugin plugin, DeathItemFilter filter) {
        this.plugin = plugin;
        this.filter = filter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        filter.reloadConfig(plugin.getConfig());
        sender.sendMessage("§a[NoDropItems] Configuration reloaded.");
        return true;
    }
}
