package com.gmail.nossr50.skills.repair.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.config.ConfigLoader;
import com.gmail.nossr50.skills.repair.Repair;
import com.gmail.nossr50.skills.repair.RepairItemType;
import com.gmail.nossr50.skills.repair.RepairMaterialType;
import com.gmail.nossr50.skills.repair.Repairable;
import com.gmail.nossr50.skills.repair.RepairableFactory;
import com.gmail.nossr50.util.ItemUtils;

public class RepairConfig extends ConfigLoader {
    private List<Repairable> repairables;

    public RepairConfig(String fileName) {
        super(fileName);
        loadKeys();
    }

    @Override
    protected void loadKeys() {
        repairables = new ArrayList<Repairable>();

        ConfigurationSection section = config.getConfigurationSection("Repairables");
        Set<String> keys = section.getKeys(false);

        for (String key : keys) {
            if (config.contains("Repairables." + key + ".ItemId")) {
                plugin.getLogger().warning("You are using an old version of the " + fileName + " file.");
                plugin.getLogger().warning("You should delete your current file and allow a new one to generate.");
                plugin.getLogger().warning("Repair will not work properly until you do.");
                return;
            }

            // Validate all the things!
            List<String> reason = new ArrayList<String>();

            // Item Material
            Material itemMaterial = Material.matchMaterial(key);

            if (itemMaterial == null) {
                reason.add("Invalid material: " + key);
            }

            // Repair Material Type
            RepairMaterialType repairMaterialType = RepairMaterialType.OTHER;
            String repairMaterialTypeString = config.getString("Repairables." + key + ".MaterialType", "OTHER");

            if (!config.contains("Repairables." + key + ".MaterialType") && itemMaterial != null) {
                ItemStack repairItem = new ItemStack(itemMaterial);

                if (ItemUtils.isWoodTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.WOOD;
                }
                else if (ItemUtils.isStoneTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.STONE;
                }
                else if (ItemUtils.isStringTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.STRING;
                }
                else if (ItemUtils.isLeatherArmor(repairItem)) {
                    repairMaterialType = RepairMaterialType.LEATHER;
                }
                else if (ItemUtils.isIronArmor(repairItem) || ItemUtils.isIronTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.IRON;
                }
                else if (ItemUtils.isGoldArmor(repairItem) || ItemUtils.isGoldTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.GOLD;
                }
                else if (ItemUtils.isDiamondArmor(repairItem) || ItemUtils.isDiamondTool(repairItem)) {
                    repairMaterialType = RepairMaterialType.DIAMOND;
                }
            }
            else {
                try {
                    repairMaterialType = RepairMaterialType.valueOf(repairMaterialTypeString);
                }
                catch (IllegalArgumentException ex) {
                    reason.add(key + " has an invalid MaterialType of " + repairMaterialTypeString);
                }
            }

            // Repair Material
            String repairMaterialName = config.getString("Repairables." + key + ".RepairMaterial");
            Material repairMaterial = (repairMaterialName == null ? repairMaterialType.getDefaultRepairMaterial() : Material.matchMaterial(repairMaterialName));

            if (repairMaterial == null) {
                reason.add(key + " has an invalid repair material: " + repairMaterialName);
            }

            // Maximum Durability
            short maximumDurability = (itemMaterial != null ? itemMaterial.getMaxDurability() : (short) config.getInt("Repairables." + key + ".MaximumDurability"));

            if (maximumDurability <= 0) {
                reason.add("Maximum durability of " + key + " must be greater than 0!");
            }

            // Item Type
            RepairItemType repairItemType = RepairItemType.OTHER;
            String repairItemTypeString = config.getString("Repairables." + key + ".ItemType", "OTHER");

            if (!config.contains("Repairables." + key + ".ItemType") && itemMaterial != null) {
                ItemStack repairItem = new ItemStack(itemMaterial);

                if (ItemUtils.isMinecraftTool(repairItem)) {
                    repairItemType = RepairItemType.TOOL;
                }
                else if (ItemUtils.isArmor(repairItem)) {
                    repairItemType = RepairItemType.ARMOR;
                }
            }
            else {
                try {
                    repairItemType = RepairItemType.valueOf(repairItemTypeString);
                }
                catch (IllegalArgumentException ex) {
                    reason.add(key + " has an invalid ItemType of " + repairItemTypeString);
                }
            }

            byte repairMetadata = (byte) config.getInt("Repairables." + key + ".RepairMaterialMetadata", -1);
            int minimumLevel = config.getInt("Repairables." + key + ".MinimumLevel");
            double xpMultiplier = config.getDouble("Repairables." + key + ".XpMultiplier", 1);

            if (minimumLevel < 0) {
                reason.add(key + " has an invalid MinimumLevel of " + minimumLevel);
            }

            // Minimum Quantity
            int minimumQuantity = (itemMaterial != null && repairMaterial != null ? Repair.getRepairAndSalvageQuantities(new ItemStack(itemMaterial), repairMaterial, repairMetadata) : config.getInt("Repairables." + key + ".MinimumQuantity"));

            if (minimumQuantity <= 0 && itemMaterial != null) {
                minimumQuantity = Repair.getMaterialCount(new ItemStack(itemMaterial));
            }

            if (minimumQuantity <= 0) {
                reason.add("Minimum quantity of " + key + " must be greater than 0!");
            }

            if (noErrorsInRepairable(reason)) {
                Repairable repairable = RepairableFactory.getRepairable(itemMaterial, repairMaterial, repairMetadata, minimumLevel, minimumQuantity, maximumDurability, repairItemType, repairMaterialType, xpMultiplier);
                repairables.add(repairable);
            }
        }
    }

    protected List<Repairable> getLoadedRepairables() {
        return repairables == null ? new ArrayList<Repairable>() : repairables;
    }

    private boolean noErrorsInRepairable(List<String> issues) {
        for (String issue : issues) {
            plugin.getLogger().warning(issue);
        }

        return issues.isEmpty();
    }
}
