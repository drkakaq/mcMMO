package com.gmail.nossr50.config.mods;

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

public class CustomArmorConfig extends ConfigLoader {
    private static CustomArmorConfig instance;

    private List<Repairable> repairables;

    private List<Material> customBoots       = new ArrayList<Material>();
    private List<Material> customChestplates = new ArrayList<Material>();
    private List<Material> customHelmets     = new ArrayList<Material>();
    private List<Material> customLeggings    = new ArrayList<Material>();

    public CustomArmorConfig() {
        super("ModConfigs", "armor.yml");
        loadKeys();
    }

    public static CustomArmorConfig getInstance() {
        if (instance == null) {
            instance = new CustomArmorConfig();
        }

        return instance;
    }

    public List<Repairable> getLoadedRepairables() {
        if (repairables == null) {
            return new ArrayList<Repairable>();
        }

        return repairables;
    }

    @Override
    protected void loadKeys() {
        repairables = new ArrayList<Repairable>();

        loadArmor("Boots", customBoots);
        loadArmor("Chestplates", customChestplates);
        loadArmor("Helmets", customHelmets);
        loadArmor("Leggings", customLeggings);
    }

    private void loadArmor(String armorType, List<Material> materialList) {
        ConfigurationSection armorSection = config.getConfigurationSection(armorType);

        if (armorSection == null) {
            return;
        }

        Set<String> armorConfigSet = armorSection.getKeys(false);

        for (String armorName : armorConfigSet) {
            Material armorMaterial = Material.matchMaterial(armorName);

            if (armorMaterial == null) {
                plugin.getLogger().warning("Invalid material name. This item will be skipped.");
                continue;
            }

            boolean repairable = config.getBoolean(armorType + "." + armorName + ".Repairable");
            Material repairMaterial = Material.matchMaterial(config.getString(armorType + "." + armorName + ".Repair_Material", ""));

            if (repairMaterial == null) {
                plugin.getLogger().warning("Incomplete repair information. This item will be unrepairable.");
                repairable = false;
            }

            if (repairable) {
                byte repairData = (byte) config.getInt(armorType + "." + armorName + ".Repair_Material_Data_Value", -1);
                int repairQuantity = Repair.getRepairAndSalvageQuantities(new ItemStack(armorMaterial), repairMaterial, repairData);

                if (repairQuantity == 0) {
                    repairQuantity = config.getInt(armorType + "." + armorName + ".Repair_Material_Data_Quantity", 2);
                }

                short durability = armorMaterial.getMaxDurability();

                if (durability == 0) {
                    durability = (short) config.getInt(armorType + "." + armorName + ".Durability", 70);
                }

                repairables.add(RepairableFactory.getRepairable(armorMaterial, repairMaterial, repairData, repairQuantity, 0, durability, RepairItemType.ARMOR, RepairMaterialType.OTHER, 1.0));
            }

            materialList.add(armorMaterial);
        }
    }

    public boolean isCustomBoots(Material material) {
        return customBoots.contains(material);
    }

    public boolean isCustomChestplate(Material material) {
        return customChestplates.contains(material);
    }

    public boolean isCustomHelmet(Material material) {
        return customHelmets.contains(material);
    }

    public boolean isCustomLeggings(Material material) {
        return customLeggings.contains(material);
    }
}
