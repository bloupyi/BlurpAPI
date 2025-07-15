package fr.bloup.blurpapi.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlurpItem {
    private Material material;
    private String name;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Set<ItemFlag> flags = new HashSet<>();
    private boolean unbreakable = false;
    private boolean glow = false;
    private Integer customModelData = null;

    public BlurpItem from(ItemStack item) {
        BlurpItem blurp = new BlurpItem();
        blurp.material = item.getType();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return blurp;

        if (meta.hasDisplayName()) blurp.name = meta.getDisplayName();
        if (meta.hasLore()) blurp.lore = new ArrayList<>(meta.getLore());

        blurp.enchantments = new HashMap<>(item.getEnchantments());

        blurp.flags = meta.getItemFlags();
        blurp.unbreakable = meta.isUnbreakable();

        if (meta.hasCustomModelData()) blurp.customModelData = meta.getCustomModelData();

        return blurp;
    }

    public BlurpItem material(Material material) {
        this.material = material;
        return this;
    }

    public BlurpItem name(String name) {
        this.name = name;
        return this;
    }

    public BlurpItem addLore(String line) {
        this.lore.add(line);
        return this;
    }

    public BlurpItem addEnchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public BlurpItem unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public BlurpItem flags(ItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public BlurpItem customModelData(int data) {
        this.customModelData = data;
        return this;
    }

    public BlurpItem glow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (name != null) meta.setDisplayName(name);
        if (!(lore != null &&lore.isEmpty())) meta.setLore(lore);
        meta.setUnbreakable(unbreakable);
        meta.addItemFlags(flags.toArray(new ItemFlag[0]));

        if (customModelData != null) meta.setCustomModelData(customModelData);

        item.setItemMeta(meta);

        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry != null) item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (glow && (enchantments == null || enchantments.isEmpty())) {
            item.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
            meta = item.getItemMeta();
            if (meta != null) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        return item;
    }
}
