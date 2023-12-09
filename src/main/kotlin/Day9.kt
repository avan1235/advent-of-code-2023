data object Day9 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val histories = data.map { line -> line.split(Regex("\\s+")).map { it.toLong() } }

    histories.analyzeHistory(
      extract = List<Long>::last,
      fold = List<Long>::sum,
    ).printIt()

    histories.analyzeHistory(
      extract = List<Long>::first,
      fold = { it.asReversed().fold(0L) { acc, curr -> curr - acc } }
    ).printIt()
  }
}

private inline fun List<List<Long>>.analyzeHistory(
  extract: (List<Long>) -> Long,
  fold: (List<Long>) -> Long,
): Long = sumOf { history ->
  val elements = mutableListOf<Long>()
  var curr = history
  while (curr.any { it != 0L }) {
    elements += extract(curr)
    curr = curr.windowed(size = 2).map { (l, r) -> r - l }
  }
  fold(elements)
}
