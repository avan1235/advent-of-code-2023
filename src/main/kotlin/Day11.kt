import kotlin.math.abs

data object Day11 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.collectGalaxies(expandFactor = 2).sumDistances().printIt()
    data.collectGalaxies(expandFactor = 1000000).sumDistances().printIt()
  }
}

private infix fun Galaxy.distanceTo(other: Galaxy): Long = abs(x - other.x) + abs(y - other.y)

private data class Galaxy(
  val x: Long,
  val y: Long,
)

private fun List<String>.collectGalaxies(expandFactor: Int): List<Galaxy> {
  val data = this
  val expandX = data.first().indices.filter { idx -> data.all { it[idx] == '.' } }.toSet()
  val expandY = data.mapIndexedNotNull { idx, line -> idx.takeIf { line.all { it == '.' } } }.toSet()
  return buildList {
    var extraY = 0L
    for (y in data.indices) {
      if (y in expandY) {
        extraY += expandFactor - 1
        continue
      }
      var extraX = 0L
      for (x in data[y].indices) {
        if (x in expandX) {
          extraX += expandFactor - 1
          continue
        }
        if (data[y][x] != '#') continue

        add(Galaxy(x + extraX, y + extraY))
      }
    }
  }
}

private fun List<Galaxy>.sumDistances(): Long {
  var sum = 0L
  for (fst in 0..<lastIndex) for (snd in fst + 1..lastIndex) {
    sum += this[fst] distanceTo this[snd]
  }
  return sum
}
