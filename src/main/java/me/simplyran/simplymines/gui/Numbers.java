package me.simplyran.simplymines.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Number formatting for menu text, so 0.3 * 100 never shows as 30.000000000000004. */
public final class Numbers {

    private Numbers() {}

    /** Plain decimal without trailing zeros: 5, 0.25, 12.5. */
    public static String plain(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /** A 0-1 fraction shown as a percent with up to two decimals: 0.0025 -> 0.25. */
    public static String percent(double fraction) {
        return BigDecimal.valueOf(fraction * 100).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
