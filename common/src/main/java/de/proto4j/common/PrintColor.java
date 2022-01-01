package de.proto4j.common;//@date 24.11.2021

import java.awt.*;

public enum PrintColor {
    BLACK(30),
    DARK_RED(31),
    DARK_GREEN(32),
    DARK_YELLOW(33),
    DARK_BLUE(34),
    DARK_MAGENTA(35),
    DARK_CYAN(36),
    LIGHT_GREY(37),
    BRIGHT_BLACK(90),
    BRIGHT_RED(91),
    BRIGHT_GREEN(92),
    BRIGHT_YELLOW(93),
    BRIGHT_BLUE(94),
    BRIGHT_MAGENTA(95),
    BRIGHT_CYAN(96),
    WHITE(100),
    BOLD(1),
    UNDERLINE(4);

    private final Color x;
    private final String ccode;

    PrintColor(Color x) {
        this(x, 0);
    }

    PrintColor(int ccode) {
        this(null, ccode);
    }

    PrintColor(Color x, int ccode) {
        this.x     = x;
        this.ccode = "[" + ccode + "m";
    }

    public Color getX() {
        return x;
    }

    public String getColorCode() {
        return ccode;
    }
}
