package me.simplyran.simplymines.utils;

import com.google.common.collect.Multimap;
import me.simplyran.simplymines.gui.Numbers;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads tool attributes. A key is either a vanilla item attribute ({@code minecraft:attack_damage}, summed from the
 * tool's modifiers) or a custom number stored on the item by another plugin ({@code myplugin:pickaxe_speed}).
 */
public final class AttributeUtils {

    private AttributeUtils() {}

    /** The vanilla attribute behind a key, or null when the key is not one (i.e. it names a custom item value). */
    @Nullable
    public static Attribute vanilla(@NotNull String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) return null;
        return Registry.ATTRIBUTE.get(namespacedKey);
    }

    /** Parses {@code namespace:key}; null when there is no namespace or the characters are not allowed. */
    @Nullable
    public static NamespacedKey parseKey(@NotNull String input) {
        String trimmed = input.trim().toLowerCase();
        if (trimmed.indexOf(':') <= 0) return null;
        return NamespacedKey.fromString(trimmed);
    }

    @NotNull
    public static List<Attribute> allVanilla() {
        List<Attribute> attributes = new ArrayList<>();
        for (Attribute attribute : Registry.ATTRIBUTE) {
            attributes.add(attribute);
        }
        attributes.sort(Comparator.comparing(attribute -> attribute.getKey().asString()));
        return attributes;
    }

    /** The value of {@code key} on {@code item}; 0 when the item does not have it. */
    public static double valueOf(@NotNull ItemStack item, @Nullable Attribute vanilla, @Nullable NamespacedKey custom) {
        if (item.isEmpty()) return 0;
        if (vanilla != null) return vanillaValue(item, vanilla);
        if (custom != null) return customValue(item, custom);
        return 0;
    }

    public static double valueOf(@NotNull ItemStack item, @NotNull String key) {
        Attribute vanilla = vanilla(key);
        return valueOf(item, vanilla, vanilla == null ? parseKey(key) : null);
    }

    /** Every numeric custom value on the item, keyed by its namespaced key. */
    @NotNull
    public static Map<String, Double> customValues(@NotNull ItemStack item) {
        Map<String, Double> values = new LinkedHashMap<>();
        if (item.isEmpty()) return values;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return values;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (NamespacedKey key : container.getKeys()) {
            Double value = readNumber(container, key);
            if (value != null) values.put(key.asString(), value);
        }
        return values;
    }

    /** {@code minecraft:attack_damage} -> {@code Attack Damage}. */
    @NotNull
    public static String displayName(@NotNull String key) {
        return EnchantmentUtils.displayName(key);
    }

    /** {@code Attack Damage >= 5}. */
    @NotNull
    public static String describe(@NotNull String key, double minimum) {
        return displayName(key) + " >= " + Numbers.plain(minimum);
    }

    private static double vanillaValue(ItemStack item, Attribute attribute) {
        Collection<AttributeModifier> modifiers = null;

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasAttributeModifiers()) {
            Multimap<Attribute, AttributeModifier> custom = meta.getAttributeModifiers(EquipmentSlot.HAND);
            modifiers = custom.get(attribute);
        } else {
            modifiers = item.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND).get(attribute);
        }
        if (modifiers.isEmpty()) return 0;

        // Same order the game applies them in: flat additions, then the additive scalar, then each multiplier.
        double flat = 0;
        double additiveScalar = 0;
        double multiplier = 1;
        for (AttributeModifier modifier : modifiers) {
            switch (modifier.getOperation()) {
                case ADD_NUMBER -> flat += modifier.getAmount();
                case ADD_SCALAR -> additiveScalar += modifier.getAmount();
                case MULTIPLY_SCALAR_1 -> multiplier *= 1 + modifier.getAmount();
            }
        }
        return flat * (1 + additiveScalar) * multiplier;
    }

    private static double customValue(ItemStack item, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        Double value = readNumber(meta.getPersistentDataContainer(), key);
        return value == null ? 0 : value;
    }

    @Nullable
    private static Double readNumber(PersistentDataContainer container, NamespacedKey key) {
        if (container.has(key, PersistentDataType.DOUBLE)) return container.get(key, PersistentDataType.DOUBLE);
        if (container.has(key, PersistentDataType.INTEGER)) return container.get(key, PersistentDataType.INTEGER).doubleValue();
        if (container.has(key, PersistentDataType.LONG)) return container.get(key, PersistentDataType.LONG).doubleValue();
        if (container.has(key, PersistentDataType.FLOAT)) return container.get(key, PersistentDataType.FLOAT).doubleValue();
        if (container.has(key, PersistentDataType.SHORT)) return container.get(key, PersistentDataType.SHORT).doubleValue();
        if (container.has(key, PersistentDataType.BYTE)) return container.get(key, PersistentDataType.BYTE).doubleValue();
        if (container.has(key, PersistentDataType.STRING)) {
            try {
                double parsed = Double.parseDouble(container.get(key, PersistentDataType.STRING).trim());
                return Double.isFinite(parsed) ? parsed : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
