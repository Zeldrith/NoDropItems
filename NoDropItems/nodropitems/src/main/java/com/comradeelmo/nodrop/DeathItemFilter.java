package com.comradeelmo.nodrop;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DeathItemFilter implements Listener {

    private final JavaPlugin plugin;
    private final Set<Material> excludedMaterials = new HashSet<>();
    private final Set<Material> includedMaterials = new HashSet<>();
    private final Map<UUID, List<ItemStack>> savedItems = new HashMap<>();
    private DropMode dropMode = DropMode.SUPPRESS;

    private enum DropMode {
        KEEP, SUPPRESS
    }

    public DeathItemFilter(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig(plugin.getConfig());
    }

    public void reloadConfig(FileConfiguration config) {
        excludedMaterials.clear();
        includedMaterials.clear();

        List<String> excludedList = config.getStringList("excluded-items");
        for (String name : excludedList) {
            try {
                excludedMaterials.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in excluded-items: " + name);
            }
        }

        List<String> includedList = config.getStringList("included-items");
        for (String name : includedList) {
            try {
                includedMaterials.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in included-items: " + name);
            }
        }

        try {
            dropMode = DropMode.valueOf(config.getString("mode", "SUPPRESS").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid drop mode in config. Using SUPPRESS.");
            dropMode = DropMode.SUPPRESS;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> preserved = new ArrayList<>();
        Iterator<ItemStack> iter = event.getDrops().iterator();

        boolean isWhitelist = !includedMaterials.isEmpty();

        while (iter.hasNext()) {
            ItemStack item = iter.next();
            if (item == null)
                continue;

            Material type = item.getType();
            boolean matches;

            if (isWhitelist) {
                // Whitelist mode: only included items are affected
                matches = includedMaterials.contains(type);
            } else {
                // Exclusive mode: excluded items are affected
                matches = excludedMaterials.contains(type);
            }

            if (matches) {
                if (dropMode == DropMode.KEEP) {
                    preserved.add(item.clone());
                }
                iter.remove();
            }
        }

        if (!preserved.isEmpty()) {
            savedItems.put(event.getEntity().getUniqueId(), preserved);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (dropMode != DropMode.KEEP)
            return;

        UUID uuid = event.getPlayer().getUniqueId();
        List<ItemStack> items = savedItems.remove(uuid);
        if (items != null) {
            Player player = event.getPlayer();
            for (ItemStack item : items) {
                if (!tryEquipArmorOrOffhand(player, item)) {
                    player.getInventory().addItem(item);
                }
            }
            player.updateInventory(); // Force update in some clients
        }
    }

    private boolean tryEquipArmorOrOffhand(Player player, ItemStack item) {
        Material type = item.getType();
        boolean equipped = false;

        switch (type) {
            case DIAMOND_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case CHAINMAIL_HELMET:
            case TURTLE_HELMET:
            case LEATHER_HELMET:
                player.getInventory().setHelmet(item);
                equipped = true;
                break;

            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                player.getInventory().setChestplate(item);
                equipped = true;
                break;

            case DIAMOND_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
                player.getInventory().setLeggings(item);
                equipped = true;
                break;

            case DIAMOND_BOOTS:
            case IRON_BOOTS:
            case GOLDEN_BOOTS:
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
                player.getInventory().setBoots(item);
                equipped = true;
                break;

            case SHIELD:
                player.getInventory().setItemInOffHand(item);
                equipped = true;
                break;

            default:
                break;
        }

        return equipped;
    }

    public Set<Material> getExcludedMaterials() {
        return excludedMaterials;
    }

    public Set<Material> getIncludedMaterials() {
        return includedMaterials;
    }
}
