package com.gmail.nossr50.skills.salvage;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.util.ItemUtils;

public class Salvage {
    // The order of the values is extremely important, a few methods depend on it to work properly
    protected enum Tier {
        FIVE(5) {
            @Override public int getLevel() { return AdvancedConfig.getInstance().getArcaneSalvageRank5Level(); }
            @Override public double getExtractFullEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractFullEnchantsRank5(); }
            @Override public double getExtractPartialEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractPartialEnchantsRank5(); }},
        FOUR(4) {
            @Override public int getLevel() { return AdvancedConfig.getInstance().getArcaneSalvageRank4Level(); }
            @Override public double getExtractFullEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractFullEnchantsRank4(); }
            @Override public double getExtractPartialEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractPartialEnchantsRank4(); }},
        THREE(3) {
            @Override public int getLevel() { return AdvancedConfig.getInstance().getArcaneSalvageRank3Level(); }
            @Override public double getExtractFullEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractFullEnchantsRank3(); }
            @Override public double getExtractPartialEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractPartialEnchantsRank3(); }},
        TWO(2) {
            @Override public int getLevel() { return AdvancedConfig.getInstance().getArcaneSalvageRank2Level(); }
            @Override public double getExtractFullEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractFullEnchantsRank2(); }
            @Override public double getExtractPartialEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractPartialEnchantsRank2(); }},
        ONE(1) {
            @Override public int getLevel() { return AdvancedConfig.getInstance().getArcaneSalvageRank1Level(); }
            @Override public double getExtractFullEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractFullEnchantsRank1(); }
            @Override public double getExtractPartialEnchantChance() { return AdvancedConfig.getInstance().getArcaneSalvageExtractPartialEnchantsRank1(); }};

        int numerical;

        private Tier(int numerical) {
            this.numerical = numerical;
        }

        public int toNumerical() {
            return numerical;
        }

        abstract protected int getLevel();
        abstract protected double getExtractFullEnchantChance();
        abstract protected double getExtractPartialEnchantChance();
    }

    public static int salvageAnvilId = Config.getInstance().getSalvageAnvilId();

    public static int    salvageMaxPercentageLevel = AdvancedConfig.getInstance().getSalvageMaxPercentageLevel();
    public static double salvageMaxPercentage      = AdvancedConfig.getInstance().getSalvageMaxPercentage();

    public static boolean arcaneSalvageDowngrades  = AdvancedConfig.getInstance().getArcaneSalvageEnchantDowngradeEnabled();
    public static boolean arcaneSalvageEnchantLoss = AdvancedConfig.getInstance().getArcaneSalvageEnchantLossEnabled();

    public static int advancedSalvageUnlockLevel = AdvancedConfig.getInstance().getAdvancedSalvageUnlockLevel();

    /**
     * Checks if the item is salvageable.
     *
     * @param item Item to check
     * @return true if the item is salvageable, false otherwise
     */
    public static boolean isSalvageable(ItemStack item) {
        if (Config.getInstance().getSalvageTools() && (ItemUtils.isMinecraftTool(item) || ItemUtils.isStringTool(item) || item.getType() == Material.BUCKET)) {
            return true;
        }

        if (Config.getInstance().getSalvageArmor() && ItemUtils.isMinecraftArmor(item)) {
            return true;
        }

        return false;
    }

    protected static Material getSalvagedItem(ItemStack inHand) {
        if (ItemUtils.isDiamondTool(inHand) || ItemUtils.isDiamondArmor(inHand)) {
            return Material.DIAMOND;
        }
        else if (ItemUtils.isGoldTool(inHand) || ItemUtils.isGoldArmor(inHand)) {
            return Material.GOLD_INGOT;
        }
        else if (ItemUtils.isIronTool(inHand) || ItemUtils.isIronArmor(inHand)) {
            return Material.IRON_INGOT;
        }
        else if (ItemUtils.isStoneTool(inHand)) {
            return Material.COBBLESTONE;
        }
        else if (ItemUtils.isWoodTool(inHand)) {
            return Material.WOOD;
        }
        else if (ItemUtils.isLeatherArmor(inHand)) {
            return Material.LEATHER;
        }
        else if (ItemUtils.isStringTool(inHand)) {
            return Material.STRING;
        }
        else {
            return null;
        }
    }

    protected static int getSalvagedAmount(ItemStack inHand) {
        if (ItemUtils.isPickaxe(inHand) || ItemUtils.isAxe(inHand) || inHand.getType() == Material.BOW || inHand.getType() == Material.BUCKET) {
            return 3;
        }
        else if (ItemUtils.isShovel(inHand) || inHand.getType() == Material.FLINT_AND_STEEL) {
            return 1;
        }
        else if (ItemUtils.isSword(inHand) || ItemUtils.isHoe(inHand) || inHand.getType() == Material.CARROT_STICK || inHand.getType() == Material.FISHING_ROD || inHand.getType() == Material.SHEARS) {
            return 2;
        }
        else if (ItemUtils.isHelmet(inHand)) {
            return 5;
        }
        else if (ItemUtils.isChestplate(inHand)) {
            return 8;
        }
        else if (ItemUtils.isLeggings(inHand)) {
            return 7;
        }
        else if (ItemUtils.isBoots(inHand)) {
            return 4;
        }
        else {
            return 0;
        }
    }

    protected static int calculateSalvageableAmount(short currentDurability, short maxDurability, int baseAmount) {
        double percentDamaged = (double) (maxDurability - currentDurability) / maxDurability;

        return (int) Math.floor(baseAmount * percentDamaged);
    }
}
