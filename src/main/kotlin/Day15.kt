import LabeledOperation.*

data object Day15 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val inputs = data.single().split(',')

    inputs.sumOf { it.hash() }.printIt()

    val operations = inputs.map { it.toLabeledOperation() }
    val boxes = LensesHashMap()
    boxes.executeAll(operations)
    boxes.focalPower.printIt()
  }
}

private const val LensesHashMapBuckets: Int = 256

private typealias LensesHashMap = Array<MutableList<Lens>>

private fun LensesHashMap(): LensesHashMap = Array(LensesHashMapBuckets) { mutableListOf() }

private val Array<MutableList<Lens>>.focalPower: Long
  get() {
    var result = 0L
    forEachIndexed { boxIdx, bucket ->
      bucket.forEachIndexed { slotIdx, lens ->
        result += (boxIdx + 1L) * (slotIdx + 1L) * lens.focalLength
      }
    }
    return result
  }

private fun Array<MutableList<Lens>>.executeAll(operations: List<LabeledOperation>) {
  for (op in operations) {
    val bucket = this[op.box]
    val idx = bucket.indexOfFirst { it.label == op.label }
    when (op) {
      is Dash -> if (idx != -1) bucket.removeAt(idx)
      is Equals -> {
        val lens = Lens(op.label, op.focalLength)
        if (idx == -1) bucket.add(lens) else bucket[idx] = lens
      }
    }
  }
}

private fun String.hash(): Int =
  fold(0) { acc, c -> (acc + c.code) * 17 % LensesHashMapBuckets }

private fun String.toLabeledOperation(): LabeledOperation {
  val label = takeWhile { it.isLetter() }
  val rest = dropWhile { it.isLetter() }
  return when {
    rest == "-" -> Dash(label)
    EQUALS_DIGITS_REGEX.matches(rest) -> Equals(label, rest.removePrefix("=").toInt())
    else -> throw IllegalArgumentException("Unknown operation: $this")
  }
}

private val EQUALS_DIGITS_REGEX = Regex("=\\d+")

private sealed class LabeledOperation(val label: String) {
  val box: Int = label.hash()

  class Dash(label: String) : LabeledOperation(label)
  class Equals(label: String, val focalLength: Int) : LabeledOperation(label)
}

private data class Lens(val label: String, val focalLength: Int)
