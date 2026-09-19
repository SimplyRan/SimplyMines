package me.simplyran.simplymines.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EnchantmentUtils {

    private static final String[] ROMAN_NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private EnchantmentUtils() {}

    @Nullable
    public static Enchantment fromKey(@NotNull String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) return null;
        return Registry.ENCHANTMENT.get(namespacedKey);
    }

    @NotNull
    public static List<Enchantment> allEnchantments() {
        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            enchantments.add(enchantment);
        }
        enchantments.sort(Comparator.comparing(enchantment -> enchantment.getKey().asString()));
        return enchantments;
    }

    @NotNull
    public static String displayName(@NotNull String key) {
        String path = key.substring(key.indexOf(':') + 1);
        StringBuilder name = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    @NotNull
    public static String displayName(@NotNull String key, int level) {
        return displayName(key) + " " + toRoman(level);
    }

    @NotNull
    public static String toRoman(int level) {
        if (level > 0 && level < ROMAN_NUMERALS.length) return ROMAN_NUMERALS[level];
        return String.valueOf(level);
    }
}
