import GameColor.*
import kotlin.math.max

data object Day2 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val games = data.map { it.toGameSet() }

    games.sumOf { it.takeIf { it.canBePlayed }?.id ?: 0 }.printIt()
    games.sumOf { it.maxes.values.fold(1, Int::times) }.printIt()
  }
}

private fun String.toGameSet(): GameSet {
  val noPrefix = removePrefix("Game ")
  val id = noPrefix.takeWhile { it.isDigit() }.toInt()
  val counts = noPrefix
    .dropWhile { it.isDigit() }
    .removePrefix(": ")
    .split("; ").map {
      it.split(", ")
        .associate { part ->
          part.split(" ").let { (count, type) -> GameColor.valueOf(type) to count.toInt() }
        }.toDefaultMap(0)
    }
  return GameSet(id, counts)
}

private class GameSet(
  val id: Int,
  counts: List<DefaultMap<GameColor, Int>>,
) {
  val canBePlayed: Boolean = counts
    .all { it[red] <= 12 && it[green] <= 13 && it[blue] <= 14 }

  val maxes: DefaultMap<GameColor, Int> = counts
    .fold(DefaultMap(0)) { acc, count ->
      acc.also { count.forEach { (k, v) -> it[k] = max(it[k], v) } }
    }
}

private enum class GameColor {
  blue, red, green,
}
