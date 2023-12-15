import RockType.*

data object Day14 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val rockMap = data.toRockMap()

    rockMap.rollNorth().northLoad.printIt()
    rockMap.rollDetectingCycles(1000000000).printIt()
  }
}

private fun List<String>.toRockMap(): RockMap {
  val xSize = map { it.length }.distinct().single()
  val ySize = size
  val map = Array(ySize) { Array(xSize) { None } }
  for (y in indices) for (x in this[y].indices) when (this[y][x]) {
    'O' -> map[y][x] = Round
    '#' -> map[y][x] = Cube
  }
  return RockMap(xSize, ySize, map)
}

private data class RockPosition(val x: Int, val y: Int)

private enum class RockType {
  None, Cube, Round;

  val isFree: Boolean get() = this == None
}

private operator fun Array<Array<RockType>>.get(position: RockPosition): RockType =
  this[position.y][position.x]

private operator fun Array<Array<RockType>>.set(position: RockPosition, type: RockType) {
  this[position.y][position.x] = type
}

private class RockMap(
  private val xSize: Int,
  private val ySize: Int,
  private val map: Array<Array<RockType>>,
) {
  val northLoad: Long
    get() {
      var result = 0L
      for (y in 0..<ySize) for (x in 0..<xSize) if (map[y][x] == Round) {
        result += ySize - y
      }
      return result
    }

  fun rollDetectingCycles(cycleCount: Int): Long {
    val mapToIndexWhenHappened = mutableMapOf<RockMap, Int>()
    var map = this
    repeat(cycleCount) { cycleIdx ->
      val beforeIdx = mapToIndexWhenHappened[map]
      if (beforeIdx != null) {
        val cycleSize = cycleIdx - beforeIdx
        val restCount = cycleCount - cycleIdx
        repeat(restCount % cycleSize) { map = map.rollCycle() }
        return map.northLoad
      }
      mapToIndexWhenHappened[map] = cycleIdx
      map = map.rollCycle()
    }
    return map.northLoad
  }

  fun rollCycle(): RockMap = rollNorth().rollWest().rollSouth().rollEast()

  private inline fun roll(
    fstIndices: IntProgression,
    sndIndices: IntProgression,
    create: (fst: Int, snd: Int) -> RockPosition,
    next: (RockPosition) -> RockPosition,
    isLast: (RockPosition) -> Boolean,
  ): RockMap {
    val newMap = Array(ySize) { Array(xSize) { None } }
    for (fst in fstIndices) for (snd in sndIndices) {
      val position = create(fst, snd)
      when (map[position]) {
        None -> {}
        Cube -> newMap[position] = Cube
        Round -> {
          var curr = position
          while (!isLast(curr)) {
            curr = next(curr).takeIf { newMap[it].isFree } ?: break
          }
          newMap[curr] = Round
        }
      }
    }
    return RockMap(xSize, ySize, newMap)
  }

  fun rollNorth(): RockMap = roll(
    fstIndices = 0..<ySize,
    sndIndices = 0..<xSize,
    create = { fst, snd -> RockPosition(snd, fst) },
    next = { RockPosition(it.x, it.y - 1) },
    isLast = { it.y == 0 },
  )

  fun rollWest(): RockMap = roll(
    fstIndices = 0..<xSize,
    sndIndices = 0..<ySize,
    create = { fst, snd -> RockPosition(fst, snd) },
    next = { RockPosition(it.x - 1, it.y) },
    isLast = { it.x == 0 },
  )

  fun rollSouth(): RockMap = roll(
    fstIndices = (0..<ySize).reversed(),
    sndIndices = 0..<xSize,
    create = { fst, snd -> RockPosition(snd, fst) },
    next = { RockPosition(it.x, it.y + 1) },
    isLast = { it.y == ySize - 1 },
  )

  fun rollEast(): RockMap = roll(
    fstIndices = (0..<xSize).reversed(),
    sndIndices = 0..<ySize,
    create = { fst, snd -> RockPosition(fst, snd) },
    next = { RockPosition(it.x + 1, it.y) },
    isLast = { it.x == xSize - 1 },
  )

  override fun equals(other: Any?): Boolean =
    (other as? RockMap)?.map?.contentDeepEquals(map) ?: false

  override fun hashCode(): Int =
    map.contentDeepHashCode()
}
