data object Day6 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val times = data[0].removePrefix("Time:").trim().split(Regex("\\s+")).map { it.toInt() }
    val distances = data[1].removePrefix("Distance:").trim().split(Regex("\\s+")).map { it.toInt() }

    times.zip(distances).fold(1L) { acc, (time, distance) ->
      acc * countBeating(
        time = time.toLong(),
        distance = distance.toLong()
      )
    }.printIt()

    countBeating(
      time = times.joinToLong(),
      distance = distances.joinToLong()
    ).printIt()
  }
}

private fun List<Int>.joinToLong(): Long =
  buildString { for (e in this@joinToLong) append(e) }.toLong()

private fun countBeating(time: Long, distance: Long): Long {
  var result = 0L
  for (holdTime in 0..time) {
    val reachedDistance = (time - holdTime) * holdTime
    if (reachedDistance > distance) result += 1
  }
  return result
}
