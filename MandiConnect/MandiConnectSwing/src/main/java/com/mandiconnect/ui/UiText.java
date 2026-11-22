package com.mandiconnect.ui;

/**
 * Shared number formatting for tables and the algorithm console.
 */
final class UiText {

    private UiText() {
    }

    static String money(double value) {
        if (Double.isFinite(value) && value == Math.rint(value)) {
            return String.format("%.0f", value);
        }
        return String.format("%.2f", value);
    }
}
