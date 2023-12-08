import Instruction.*

data object Day8 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val instructions = data.first().map { Instruction.valueOf("$it") }
    val network = data.drop(2).toNetwork()

    instructions
      .countSteps(start = "AAA", network) { it == "ZZZ" }
      .printIt()
    network.starts
      .map { start -> instructions.countSteps(start, network) { it.endsWith('Z') } }
      .fold(1L, ::lcm)
      .printIt()
  }
}

private enum class Instruction { L, R }

private tailrec fun gcd(a: Long, b: Long): Long =
  if (b == 0L) a else gcd(b, a % b)

private fun lcm(a: Long, b: Long): Long =
  a / gcd(a, b) * b

private class Network(
  private val l: Map<String, String>,
  private val r: Map<String, String>,
) {
  val starts: List<String> = l.keys.filter { it.endsWith('A') } + r.keys.filter { it.endsWith('A') }
  operator fun get(instruction: Instruction): Map<String, String> = when (instruction) {
    L -> l
    R -> r
  }
}

private fun List<String>.toNetwork(): Network {
  val mapL = mutableMapOf<String, String>()
  val mapR = mutableMapOf<String, String>()
  forEach { line ->
    line.split(" = ").let { (from, to) ->
      to.split(", ").let { (l, r) ->
        mapL[from] = l.removePrefix("(")
        mapR[from] = r.removeSuffix(")")
      }
    }
  }
  return Network(mapL, mapR)
}

private fun List<Instruction>.countSteps(start: String, network: Network, finished: (curr: String) -> Boolean): Long {
  var curr = start
  var count = 0L
  var idx = 0
  while (!finished(curr)) {
    curr = network[this[idx]][curr]!!
    idx = (idx + 1) % size
    count += 1
  }
  return count
}
