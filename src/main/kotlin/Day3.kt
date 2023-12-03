data object Day3 : AdventDay() {
  override fun solve() {
    val data = reads<String>()?.toSchematicData() ?: return


    data.numbers
      .filter { number -> number.adjacentPositions().any { data.isSymbol[it] } }
      .sumOf { it.value }
      .printIt()

    data.gears
      .sumOf { gearPosition ->
        gearPosition.adjacentPositions()
          .mapNotNull { data.numbersPositions[it] }
          .toSet()
          .takeIf { it.size >= 2 }
          ?.fold(1L) { acc, num -> acc * num.value }
          ?: 0L
      }
      .printIt()
  }
}

private fun List<String>.toSchematicData(): SchematicData {
  val isSymbol = DefaultMap<SchematicPosition, Boolean>(false)
  val isDigit = DefaultMap<SchematicPosition, Boolean>(false)
  val numbersPositions = DefaultMap<SchematicPosition, SchematicNumber?>(null)
  val numbers = mutableSetOf<SchematicNumber>()
  val gears = mutableSetOf<SchematicPosition>()

  mapIndexed { y, line ->
    val collected = mutableListOf<Pair<Int, SchematicPosition>>()

    fun collectCurrentSchematicNumber() {
      if (collected.isEmpty()) return

      val collectedDigitsPositions = collected.mapTo(LinkedHashSet()) { it.second }
      val schematicNumber = SchematicNumber(collected.toNumber(), collectedDigitsPositions)
      numbers += schematicNumber
      collectedDigitsPositions.forEach { numbersPositions[it] = schematicNumber }

      collected.clear()
    }

    line.forEachIndexed char@{ x, char ->
      val p = SchematicPosition(x, y)
      if (char.isDigit()) {
        collected += Pair(char.digitToInt(), p)
        isDigit[p] = true
        return@char
      }
      collectCurrentSchematicNumber()

      if (char == '.') return@char
      isSymbol[p] = true

      if (char != '*') return@char
      gears += p
    }

    collectCurrentSchematicNumber()
  }
  return SchematicData(
    numbers = numbers,
    gears = gears,
    isSymbol = isSymbol,
    numbersPositions = numbersPositions,
  )
}

private class SchematicData(
  val numbers: Set<SchematicNumber>,
  val gears: Set<SchematicPosition>,
  val isSymbol: DefaultMap<SchematicPosition, Boolean>,
  val numbersPositions: DefaultMap<SchematicPosition, SchematicNumber?>,
)

private data class SchematicPosition(val x: Int, val y: Int) {
  override fun toString(): String = "($x, $y)"
}

private data class SchematicNumber(
  val value: Int,
  val positions: Set<SchematicPosition>,
)

private fun <T> List<Pair<Int, T>>.toNumber(): Int =
  fold(0) { acc, p -> acc * 10 + p.first }

private fun SchematicPosition.adjacentPositions(): Sequence<SchematicPosition> = sequence {
  for (cx in -1..1) for (cy in -1..1) {
    if (cx == 0 && cy == 0) continue
    yield(SchematicPosition(x + cx, y + cy))
  }
}

private fun SchematicNumber.adjacentPositions(): List<SchematicPosition> =
  positions.flatMap { it.adjacentPositions() }.distinct()
