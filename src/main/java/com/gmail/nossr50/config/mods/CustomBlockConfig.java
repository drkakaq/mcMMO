package com.gmail.nossr50.config.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.gmail.nossr50.config.ConfigLoader;
import com.gmail.nossr50.datatypes.mods.CustomBlock;

public class CustomBlockConfig extends ConfigLoader {
    private static CustomBlockConfig instance;

    private List<MaterialData> customExcavationBlocks  = new ArrayList<MaterialData>();
    private List<MaterialData> customHerbalismBlocks   = new ArrayList<MaterialData>();
    private List<MaterialData> customMiningBlocks      = new ArrayList<MaterialData>();
    private List<MaterialData> customWoodcuttingBlocks = new ArrayList<MaterialData>();
    private List<MaterialData> customOres              = new ArrayList<MaterialData>();
    private List<MaterialData> customLogs              = new ArrayList<MaterialData>();
    private List<MaterialData> customLeaves            = new ArrayList<MaterialData>();
    private List<MaterialData> customAbilityBlocks     = new ArrayList<MaterialData>();

    private HashMap<MaterialData, CustomBlock> customBlockMap = new HashMap<MaterialData, CustomBlock>();

    public CustomBlockConfig() {
        super("ModConfigs", "blocks.yml");
        loadKeys();
    }

    public static CustomBlockConfig getInstance() {
        if (instance == null) {
            instance = new CustomBlockConfig();
        }

        return instance;
    }

    @Override
    protected void loadKeys() {
        loadBlocks("Excavation", customExcavationBlocks);
        loadBlocks("Herbalism", customHerbalismBlocks);
        loadBlocks("Mining", customMiningBlocks);
        loadBlocks("Woodcutting", customWoodcuttingBlocks);
        loadBlocks("Ability_Blocks", customAbilityBlocks);
    }

    private void loadBlocks(String skillType, List<MaterialData> blockList) {
        ConfigurationSection skillSection = config.getConfigurationSection(skillType);

        if (skillSection == null) {
            return;
        }

        Set<String> skillConfigSet = skillSection.getKeys(false);

        for (String blockName : skillConfigSet) {
            String[] blockInfo = blockName.split("[|]");

            Material blockMaterial = Material.matchMaterial(blockInfo[0]);

            if (blockMaterial == null) {
                plugin.getLogger().warning("Invalid material name. This item will be skipped. - " + blockInfo[0]);
                continue;
            }

            byte blockData = Byte.valueOf(blockInfo[1]);
            MaterialData blockMaterialData = new MaterialData(blockMaterial, blockData);
            blockList.add(blockMaterialData);

            if (skillType.equals("Ability_Blocks")) {
                continue;
            }

            int xp = config.getInt(skillType + "." + blockName + ".XP_Gain");

            boolean shouldDropItem = config.getBoolean(skillType + "." + blockName + ".Drop_Item");
            Material dropMaterial = Material.matchMaterial(config.getString(skillType + "." + blockName + ".Drop_Item_Name"));

            if (shouldDropItem && dropMaterial == null) {
                plugin.getLogger().warning("Incomplete item drop information. This block will drop itself. - " + blockInfo[0]);
                shouldDropItem = false;
            }

            ItemStack itemDrop;

            if (shouldDropItem) {
                byte dropData = (byte) config.getInt(skillType + "." + blockName + ".Drop_Item_Data_Value");
                itemDrop = (new MaterialData(dropMaterial, dropData)).toItemStack(1);
            }
            else {
                itemDrop = blockMaterialData.toItemStack(1);
            }

            int minimumDropAmount = config.getInt(skillType + "." + blockName + ".Min_Drop_Item_Amount", 1);
            int maxiumDropAmount = config.getInt(skillType + "." + blockName + ".Max_Drop_Item_Amount", 1);

            if (skillType.equals("Mining") && config.getBoolean(skillType + "." + blockName + ".Is_Ore")) {
                customOres.add(blockMaterialData);
            }
            else if (skillType.equals("Woodcutting")) {
                if (config.getBoolean(skillType + "." + blockName + ".Is_Log")) {
                    customLogs.add(blockMaterialData);
                }
                else {
                    customLeaves.add(blockMaterialData);
                    xp = 0; // Leaves don't grant XP
                }
            }

            customBlockMap.put(blockMaterialData, new CustomBlock(minimumDropAmount, maxiumDropAmount, itemDrop, xp));
        }
    }

    public CustomBlock getCustomBlock(MaterialData data) {
        return customBlockMap.get(data);
    }

    public boolean isCustomOre(MaterialData data) {
        return customOres.contains(data);
    }

    public boolean isCustomLog(MaterialData data) {
        return customLogs.contains(data);
    }

    public boolean isCustomLeaf(MaterialData data) {
        return customLeaves.contains(data);
    }

    public boolean isCustomAbilityBlock(MaterialData data) {
        return customAbilityBlocks.contains(data);
    }

    public boolean isCustomExcavationBlock(MaterialData data) {
        return customExcavationBlocks.contains(data);
    }

    public boolean isCustomHerbalismBlock(MaterialData data) {
        return customHerbalismBlocks.contains(data);
    }

    public boolean isCustomMiningBlock(MaterialData data) {
        return customMiningBlocks.contains(data);
    }

    public boolean isCustomWoodcuttingBlock(MaterialData data) {
        return customWoodcuttingBlocks.contains(data);
    }
}
