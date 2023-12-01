data object Day1 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    data.sumOf { it.toCalibrationValue(digitRegex) }.printIt()
    data.sumOf { it.toCalibrationValue(digitByNameOrDigitRegex) }.printIt()
  }
}

private fun String.toCalibrationValue(matching: List<Regex>): Int =
  matching.flatMap { it.findAll(this) }.sortedBy { it.range.first }.let { found ->
    found.first().value.digitOrDigitNameToInt() * 10 + found.last().value.digitOrDigitNameToInt()
  }

private val digitRegex: List<Regex> = listOf(
  Regex("\\d")
)

private val digitByNameOrDigitRegex: List<Regex> = listOf(
  Regex("one"),
  Regex("two"),
  Regex("three"),
  Regex("four"),
  Regex("five"),
  Regex("six"),
  Regex("seven"),
  Regex("eight"),
  Regex("nine"),
) + digitRegex

private fun String.digitOrDigitNameToInt() = when (this) {
  "one" -> 1
  "two" -> 2
  "three" -> 3
  "four" -> 4
  "five" -> 5
  "six" -> 6
  "seven" -> 7
  "eight" -> 8
  "nine" -> 9
  else -> single().digitToInt()
}
