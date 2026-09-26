package me.simplyran.simplymines.gui;

import net.kyori.adventure.text.format.TextColor;

public enum Palette {

    ACCENT(0xffd166),
    MUTED(0x8b9bb4),
    SUCCESS(0x7bd88f),
    DANGER(0xef6f6c);

    private final TextColor color;

    Palette(int rgb) {
        this.color = TextColor.color(rgb);
    }

    public TextColor color() {
        return color;
    }
}
