import Direction.*

data object Day18 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.map { it.toMoveCountPart1() }.volume().printIt()
    data.map { it.toMoveCountPart2() }.volume().printIt()
  }
}

private fun List<MoveCount>.volume(): Long {
  val isRightTurn = sequence {
    forEach { yield(it.direction) }
    yield(first().direction)
  }
    .windowed(size = 2) { (fst, snd) -> snd.isOnRight(fst) }
    .toList()

  val centres = fold(listOf(DigCoordinate(0, 0))) { acc, i -> acc + acc.last().move(i) }
  val full =
    centres.volume() +
      sumOf { it.count - 1 } * 0.5 +
      isRightTurn.count { it } * 0.75 +
      isRightTurn.count { !it } * 0.25

  return full.toLong()
}

private fun Direction.isOnRight(o: Direction): Boolean = when (o) {
  R -> this == D
  L -> this == U
  U -> this == R
  D -> this == L
}

private fun List<DigCoordinate>.volume(): Double =
  windowed(size = 2) { (c1, c2) -> c2.x * c1.y - c1.x * c2.y }.sum() / 2.0

private fun DigCoordinate.move(moveCount: MoveCount): DigCoordinate = when (moveCount.direction) {
  R -> DigCoordinate(x + moveCount.count, y)
  L -> DigCoordinate(x - moveCount.count, y)
  U -> DigCoordinate(x, y + moveCount.count)
  D -> DigCoordinate(x, y - moveCount.count)
}

private data class DigCoordinate(val x: Long, val y: Long)

private fun String.toMoveCountPart1(): MoveCount {
  val (dir, count, _) = split(" ")
  return MoveCount(
    direction = Direction.valueOf(dir),
    count = count.toLong(),
  )
}

private fun String.toMoveCountPart2(): MoveCount {
  val (_, _, fullColor) = split(" ")
  val color = fullColor.removeSurrounding("(#", ")")
  return MoveCount(
    direction = Direction.entries[color.last().digitToInt()],
    count = color.dropLast(1).toLong(radix = 16),
  )
}

private enum class Direction { R, D, L, U }

private data class MoveCount(
  val direction: Direction,
  val count: Long,
)
