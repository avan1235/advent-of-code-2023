import GardenMap.Companion.toGardenMap

data object Day21 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.toGardenMap(infinite = false).countPositionsAtDistance(distance = 64).printIt()
    data.toGardenMap(infinite = true).countPositionsAtDistance(distance = 26501365).printIt()
  }
}

private class GardenMap private constructor(
  private val map: List<String>,
  val start: Position,
  private val infinite: Boolean,
  private val xSize: Int,
  private val ySize: Int,
) : Graph<GardenMap.Position> {
  data class Position(val x: Int, val y: Int)

  override fun neighbours(node: Position): Sequence<Position> = map.neighbours(node, infinite)

  fun countPositionsAtDistance(distance: Int): Long {
    var prevD = 0
    val atDistance = LazyDefaultMap<Int, MutableSet<Position>>(::mutableSetOf)
    val size = lcm(xSize, ySize)
    val half = size / 2
    if (infinite) {
      assert(size % 2 == 1)
      assert((distance - half) % size == 0)
    }
    class InfiniteResultException(val result: Long) : RuntimeException()
    try {
      search(
        from = start,
        type = Graph.SearchType.BFS,
        visit = { _, _, d -> d <= distance },
        checkIfVisited = false,
        checkIfOnQueue = true,
        action = action@{ node, d ->
          atDistance[d] += node
          if (d <= prevD) return@action

          if (prevD == size * 2 + half) {
            val f0 = atDistance[prevD - size - size].size
            val f1 = atDistance[prevD - size].size
            val f2 = atDistance[prevD].size

            val a = (f2 - 2 * f1 + f0) / 2
            val c = f0
            val b = (4 * f1 - f2 - 3 * f0) / 2

            val x = ((distance - half) / size).toLong()
            throw InfiniteResultException(a * x * x + b * x + c)
          }
          prevD = d
        }
      )
    } catch (e: InfiniteResultException) {
      return e.result
    }
    return atDistance[distance].size.toLong()
  }

  companion object {
    private fun List<String>.findStart(): Position {
      for ((y, line) in withIndex()) for ((x, c) in line.withIndex()) if (c == 'S') return Position(x, y)
      throw IllegalArgumentException("Start not found in ${joinToString("\n")}")
    }

    private fun Position.canMoveOnMap(map: List<String>, infinite: Boolean): Boolean = when (infinite) {
      true -> map[y.mod(map.size)][x.mod(map[y.mod(map.size)].length)].let { it == '.' || it == 'S' }
      false -> y in map.indices && x in map[y].indices && map[y][x].let { it == '.' || it == 'S' }
    }

    private suspend fun SequenceScope<Position>.yieldOnMap(position: Position, map: List<String>, infinite: Boolean) {
      if (position.canMoveOnMap(map, infinite)) yield(position)
    }

    private fun List<String>.neighbours(position: Position, infinite: Boolean): Sequence<Position> = sequence {
      yieldOnMap(position.copy(x = position.x + 1), this@neighbours, infinite)
      yieldOnMap(position.copy(x = position.x - 1), this@neighbours, infinite)
      yieldOnMap(position.copy(y = position.y + 1), this@neighbours, infinite)
      yieldOnMap(position.copy(y = position.y - 1), this@neighbours, infinite)
    }

    fun List<String>.toGardenMap(infinite: Boolean): GardenMap {
      val start = findStart()
      return GardenMap(this, start, infinite, xSize = first().length, ySize = size)
    }
  }
}


