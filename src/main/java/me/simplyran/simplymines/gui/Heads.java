package me.simplyran.simplymines.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Custom-texture player heads for menu buttons. */
public final class Heads {

    // Base64 skin values (textures.minecraft.net): the "Green Plus" and "Red Minus" heads from minecraft-heads.com.
    private static final String PLUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19";
    private static final String MINUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=";

    private static ItemStack plus;
    private static ItemStack minus;

    private Heads() {}

    public static ItemStack plus() {
        if (plus == null) plus = create(PLUS);
        return plus.clone();
    }

    public static ItemStack minus() {
        if (minus == null) minus = create(MINUS);
        return minus.clone();
    }

    private static ItemStack create(String texture) {
        // A fixed id per texture keeps identical heads stacking and the profile stable across restarts.
        PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)));
        profile.setProperty(new ProfileProperty("textures", texture));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }
}
