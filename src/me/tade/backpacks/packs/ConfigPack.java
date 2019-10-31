package me.tade.backpacks.packs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

public class ConfigPack {

    private String name;
    private int size;
    private List<String> recipe;
    private ItemStack itemStack;

    public ConfigPack(String name, int size, List<String> recipe, ItemStack itemStack) {
        this.name = name;
        this.size = size;
        this.recipe = recipe;
        this.itemStack = itemStack;

        initializeRecipe();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public List<String> getRecipe() {
        return recipe;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    private void initializeRecipe() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ShapedRecipe recipe = new ShapedRecipe(getItemStack());
        recipe = recipe.shape("ABC", "DEF", "GHI");
        int number = 0;
        for (String line : getRecipe()) {
            String[] derived = line.split(" ");
            for (String materialName : derived) {
                recipe.setIngredient(alphabet.charAt(number), Material.matchMaterial(materialName));
                number++;
            }
        }

        Bukkit.addRecipe(recipe);
    }
}
