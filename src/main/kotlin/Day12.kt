data object Day12 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.repeatedData(1).sumOf { (springs, counts) ->
      ArrangementMemory().countArrangements(springs, counts)
    }.printIt()

    data.repeatedData(5).sumOf { (springs, counts) ->
      ArrangementMemory().countArrangements(springs, counts)
    }.printIt()
  }
}

private class ArrangementMemory {

  private val memory: MutableMap<Pair<List<Int>, String>, Long> = mutableMapOf()

  fun countArrangements(springs: String, counts: List<Int>): Long {
    if (springs.isEmpty()) {
      return if (counts.isEmpty()) 1 else 0
    }
    return when (val c = springs.first()) {
      '#' -> countArrangementsStartingWithDamaged(springs, counts)
        .also { memory[counts to springs] = it }

      '?' -> countArrangements(springs.drop(1), counts) +
        countArrangementsStartingWithDamaged(springs, counts)
          .also { memory[counts to springs] = it }

      '.' -> countArrangements(springs.drop(1), counts)

      else -> throw IllegalArgumentException("Unknown character $c")
    }
  }

  private fun countArrangementsStartingWithDamaged(springs: String, counts: List<Int>): Long {
    memory[counts to springs]?.let { return it }

    if (counts.isEmpty()) return 0

    val firstCount = counts.first()
    if (springs.length < firstCount) return 0

    for (idx in 0..<firstCount) {
      if (springs[idx] == '.') return 0
    }
    if (springs.length == firstCount) {
      if (counts.size == 1) return 1
      return 0
    }
    if (springs[firstCount] == '#') return 0

    return countArrangements(springs.drop(firstCount + 1), counts.drop(1))
  }
}

private fun List<String>.repeatedData(count: Int): List<Pair<String, List<Int>>> = map { line ->
  line.split(" ").let { (rawSprings, rawCounts) ->
    val springs = listOf(rawSprings).repeat(count).joinToString("?")
    val counts = rawCounts.split(',').map { it.toInt() }.repeat(count)
    Pair(springs, counts)
  }
}
