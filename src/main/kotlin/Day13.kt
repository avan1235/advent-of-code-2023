import ReflectionLine.Horizontal
import ReflectionLine.Vertical

data object Day13 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val patterns = data.groupSeparatedBy(
      separator = { it == "" },
      transform = { Pattern(it.map(String::toCharArray).toTypedArray()) }
    )

    patterns.sumOf { it.reflectionLines().single().value }.printIt()

    patterns.sumOf { pattern ->
      val initLines = pattern.reflectionLines().single()
      pattern.changedCopies().firstNotNullOf {
        val other = it.reflectionLines() - initLines
        if (other.isNotEmpty()) other.single().value else null
      }
    }.printIt()
  }
}

private class Pattern(
  private val data: Array<CharArray>,
) {
  private val xSize: Int = data.first().size
  private val ySize: Int = data.size

  override fun toString(): String = data.joinToString("\n") { it.joinToString("") }

  private inline fun isSymmetricalAt(a: Int, aSize: Int, bSize: Int, get: (b: Int, a: Int) -> Char): Boolean {
    for (currA in a downTo 0) {
      val symA = a + (a - currA) + 1
      if (symA >= aSize) return true
      if ((0..<bSize).any { currB -> get(currB, currA) != get(currB, symA) }) return false
    }
    return true
  }

  fun reflectionLines(): List<ReflectionLine> = buildList {
    for (y in 0..<ySize - 1) if (isSymmetricalAt(y, ySize, xSize) { b, a -> data[a][b] }) add(Horizontal(y))
    for (x in 0..<xSize - 1) if (isSymmetricalAt(x, xSize, ySize) { b, a -> data[b][a] }) add(Vertical(x))
  }

  fun changedCopies(): Sequence<Pattern> = sequence {
    for (y in 0..<ySize) for (x in 0..<xSize) {
      val copy = Array(data.size) { data[it].copyOf() }
      copy[y][x] = when (val c = data[y][x]) {
        '.' -> '#'
        '#' -> '.'
        else -> throw IllegalArgumentException("Unknown character: $c")
      }
      yield(Pattern(copy))
    }
  }
}

private sealed class ReflectionLine(val value: Long) {
  data class Horizontal(val idx: Int) : ReflectionLine((idx + 1L) * 100L)
  data class Vertical(val idx: Int) : ReflectionLine(idx + 1L)
}
