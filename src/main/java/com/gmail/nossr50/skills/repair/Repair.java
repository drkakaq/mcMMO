package com.gmail.nossr50.skills.repair;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.config.Config;

public class Repair {
    public static int    repairMasteryMaxBonusLevel = AdvancedConfig.getInstance().getRepairMasteryMaxLevel();
    public static double repairMasteryMaxBonus      = AdvancedConfig.getInstance().getRepairMasteryMaxBonus();

    public static int    superRepairMaxBonusLevel = AdvancedConfig.getInstance().getSuperRepairMaxLevel();
    public static double superRepairMaxChance     = AdvancedConfig.getInstance().getSuperRepairChanceMax();

    public static int repairAnvilId = Config.getInstance().getRepairAnvilId();
    public static boolean anvilMessagesEnabled = Config.getInstance().getRepairAnvilMessagesEnabled();

    /**
     * Search the inventory for an item and return the index.
     *
     * @param inventory PlayerInventory to scan
     * @param itemId Item id to look for
     * @return index location where the item was found, or -1 if not found
     */
    protected static int findInInventory(PlayerInventory inventory, int itemId) {
        int location = inventory.first(itemId);

        // VALIDATE
        if (inventory.getItem(location).getTypeId() == itemId) {
            return location;
        }

        return -1;
    }

    /**
     * Search the inventory for an item and return the index.
     *
     * @param inventory PlayerInventory to scan
     * @param itemId Item id to look for
     * @param metadata Metadata to look for
     * @return index location where the item was found, or -1 if not found
     */
    protected static int findInInventory(PlayerInventory inventory, int itemId, byte metadata) {
        int location = -1;

        ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null) {
                continue;
            }

            if (item.getTypeId() == itemId && item.getData().getData() == metadata) {
                return i;
            }
        }

        return location;
    }

    /**
     * Decrease the amount of items in this slot by one
     *
     * @param inventory PlayerInventory to work in
     * @param index Item index to decrement
     */
    protected static void removeOneFrom(PlayerInventory inventory, int index) {
        ItemStack item = inventory.getItem(index).clone();
        item.setAmount(1);

        inventory.removeItem(item);
    }
}
