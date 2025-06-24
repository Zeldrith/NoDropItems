package com.comradeelmo.nodrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ItemGUI implements Listener {

    private final JavaPlugin plugin;
    private final DeathItemFilter filter;
    private final String MAIN_MENU_TITLE = ChatColor.BLUE + "NoDropItems Menu";
    private final String EXCLUDED_ITEMS_TITLE = ChatColor.RED + "Excluded Items";

    public ItemGUI(JavaPlugin plugin, DeathItemFilter filter) {
        this.plugin = plugin;
        this.filter = filter;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, MAIN_MENU_TITLE);

        ItemStack viewExcluded = new ItemStack(Material.BARRIER);
        ItemMeta excludedMeta = viewExcluded.getItemMeta();
        excludedMeta.setDisplayName(ChatColor.RED + "View Excluded Items");
        viewExcluded.setItemMeta(excludedMeta);

        ItemStack addExcluded = new ItemStack(Material.ANVIL);
        ItemMeta addMeta = addExcluded.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add Held Item to Excluded List");
        addExcluded.setItemMeta(addMeta);

        gui.setItem(3, viewExcluded);
        gui.setItem(5, addExcluded);

        player.openInventory(gui);
    }

    public void openExcludedMenu(Player player) {
        List<Material> materials = new ArrayList<>(filter.getExcludedMaterials());
        int size = ((materials.size() / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, Math.max(9, size), EXCLUDED_ITEMS_TITLE);

        for (Material material : materials) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Click to remove");
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        if (title.equals(MAIN_MENU_TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;

            switch (event.getSlot()) {
                case 3:
                    openExcludedMenu(player);
                    break;
                case 5:
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem == null || handItem.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "Hold an item to add it to the excluded list.");
                        break;
                    }
                    Material mat = handItem.getType();
                    if (!filter.getExcludedMaterials().contains(mat)) {
                        filter.getExcludedMaterials().add(mat);
                        plugin.getConfig().set("excluded-items", serializeMaterials(filter.getExcludedMaterials()));
                        plugin.saveConfig();
                        player.sendMessage(ChatColor.GREEN + mat.name() + " has been added to the excluded list.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + mat.name() + " is already excluded.");
                    }
                    player.closeInventory();
                    break;
            }
        } else if (title.equals(EXCLUDED_ITEMS_TITLE)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            Material mat = clicked.getType();
            if (filter.getExcludedMaterials().remove(mat)) {
                plugin.getConfig().set("excluded-items", serializeMaterials(filter.getExcludedMaterials()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + mat.name() + " has been removed from the excluded list.");
                openExcludedMenu(player);
            }
        }
    }

    private List<String> serializeMaterials(Iterable<Material> materials) {
        List<String> list = new ArrayList<>();
        for (Material mat : materials) {
            list.add(mat.name());
        }
        return list;
    }
}
