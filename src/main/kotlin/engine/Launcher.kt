package engine

import com.cozmicgames.*

private const val CONFIG_FILE = "config.txt"

fun main() {
    val configuration = Configuration()

    if (!configuration.readFromFile(CONFIG_FILE)) {
        configuration.title = "Game"
        configuration.icons = arrayOf("icons/icon.png")
    }

    Kore.start(Game, configuration) { DesktopPlatform() }

    configuration.writeToFile(CONFIG_FILE)
}
