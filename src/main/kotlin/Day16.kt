import LightDirection.*

data object Day16 : AdventDay() {
  override fun solve() {
    val map = reads<String>() ?: return

    map.countEnergized(LightPosition(0, 0) to R).printIt()

    map.run {
      xIndices.map { LightPosition(it, yIndices.first) to D } +
        xIndices.map { LightPosition(it, yIndices.last) to U } +
        yIndices.map { LightPosition(xIndices.first, it) to R } +
        yIndices.map { LightPosition(xIndices.last, it) to L }
    }
      .maxOf { map.countEnergized(it) }
      .printIt()
  }
}

private typealias LightMap = List<String>

private val LightMap.xIndices: IntRange get() = first().indices

private val LightMap.yIndices: IntRange get() = indices

private fun LightMap.countEnergized(start: LightData): Int {
  var data = listOf(start)
  val history = mutableSetOf(start)

  while (true) {
    val newData = mutableListOf<LightData>()
    fun LightData.addNew() {
      newData += takeIf { it.first in this@countEnergized } ?: return
    }
    for ((position, direction) in data) {
      when (val c = this[position]) {
        '.' -> position.next(direction).addNew()
        '\\' -> when (direction) {
          U -> L
          D -> R
          R -> D
          L -> U
        }.let(position::next).addNew()

        '/' -> when (direction) {
          U -> R
          D -> L
          R -> U
          L -> D
        }.let(position::next).addNew()

        '|' -> when {
          direction.isVertical -> position.next(direction).addNew()
          else -> direction.perpendicular.forEach { position.next(it).addNew() }
        }

        '-' -> when {
          direction.isHorizontal -> position.next(direction).addNew()
          else -> direction.perpendicular.forEach { position.next(it).addNew() }
        }

        else -> throw IllegalArgumentException("Unknown map element: $c")
      }
    }
    val oldHistorySize = history.size
    data = newData
      .filter { position -> position !in history }
      .onEach { history += it }

    if (history.size == oldHistorySize) break
  }
  return history.mapTo(LinkedHashSet()) { it.first }.size
}

private typealias LightData = Pair<LightPosition, LightDirection>

private operator fun LightMap.contains(position: LightPosition): Boolean =
  position.y in indices && position.x in this[position.y].indices

private operator fun LightMap.get(position: LightPosition): Char =
  this[position.y][position.x]

private fun LightPosition.next(direction: LightDirection): LightData =
  when (direction) {
    U -> LightPosition(x, y - 1)
    D -> LightPosition(x, y + 1)
    R -> LightPosition(x + 1, y)
    L -> LightPosition(x - 1, y)
  } to direction

private val UD: List<LightDirection> = listOf(U, D)
private val RL: List<LightDirection> = listOf(R, L)

private val LightDirection.perpendicular: List<LightDirection>
  get() = when (this) {
    U, D -> RL
    R, L -> UD
  }

private val LightDirection.isVertical: Boolean get() = this in UD

private val LightDirection.isHorizontal: Boolean get() = this in RL

private enum class LightDirection { U, D, R, L }

private data class LightPosition(val x: Int, val y: Int)
