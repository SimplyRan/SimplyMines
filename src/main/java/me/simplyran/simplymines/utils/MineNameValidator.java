package me.simplyran.simplymines.utils;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Validates mine names before they reach storage. Names become file names
 * (JSON backend) and permission nodes, so only a safe character set is
 * allowed — this is the primary defense against path traversal.
 */
public final class MineNameValidator {

    private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    private MineNameValidator() {
    }

    public static boolean isValid(@NotNull String name) {
        return VALID.matcher(name).matches();
    }
}
