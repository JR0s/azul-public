package entity

/**
 * Enum to distinguish between the tiles colors in Azul game:
 * red, yellow, blue, black, white, start marker
 */
enum class Color {
    RED,
    YELLOW,
    BLUE,
    BLACK,
    WHITE,
    START_MARKER,
    INVISIBLE,   // only used as placeholder for GUI
}