package com.ksanur.posterboard.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * User: bobacadodl
 * Date: 2/15/14
 * Time: 3:56 PM
 */
public class ItemBuilder {
    int amount = 1;
    Material material = Material.AIR;
    short data = 0;
    List<String> lore = null;
    String name = null;


    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(ItemStack itemStack) {
        parse(itemStack);
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder withData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withLore(String... lore) {
        this.lore = Arrays.asList(lore);
        return this;
    }

    public ItemBuilder withLore(List<String> lore) {
        this.lore = lore;
        return this;

    }

    public ItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public short getData() {
        return data;
    }

    public Material getMaterial() {
        return material;
    }

    public void parse(ItemStack itemStack) {
        material = itemStack.getType();
        amount = itemStack.getAmount();
        data = itemStack.getDurability();
        ItemMeta im = itemStack.getItemMeta();
        lore = im.getLore();
        name = im.getDisplayName();
    }

    public ItemStack build() {
        ItemStack ret = new ItemStack(material, amount, data);
        ItemMeta im = ret.getItemMeta();
        if (lore != null) {
            im.setLore(lore);
        }
        if (name != null) {
            im.setDisplayName(name);
        }
        ret.setItemMeta(im);
        return ret;
    }
}
