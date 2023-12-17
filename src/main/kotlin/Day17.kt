import MoveDirection.*
import java.util.*

data object Day17 : AdventDay() {
  override fun solve() {
    val map = reads<String>() ?: return

    map.toWeightedGraph().shortestPathLength(max = 3).printIt()
    map.toWeightedGraph().shortestPathLength(min = 4, max = 10).printIt()
  }
}

private fun List<String>.toWeightedGraph(): HeatMap = map { line ->
  line.map { it.digitToInt() }
}.let { data ->
  val xSize = data.first().size
  val ySize = data.size

  LazyDefaultMap<HeatMapNode, MutableList<HeatMapEdge>>(::mutableListOf).also { adj ->
    for (x in 0 until xSize) for (y in 0 until ySize)
      for (d in MoveDirection.entries) {
        val (tx, ty) = d.move(x, y)
        if (tx !in 0..<xSize || ty !in 0..<ySize) continue
        adj[x `#` y] += HeatMapEdge(tx `#` ty, data[ty][tx], d)
      }
  }.let { HeatMap(xSize, ySize, it) }
}

private enum class MoveDirection { N, S, E, W }

private fun MoveDirection.move(x: Int, y: Int): Pair<Int, Int> = when (this) {
  N -> Pair(x, y - 1)
  S -> Pair(x, y + 1)
  E -> Pair(x + 1, y)
  W -> Pair(x - 1, y)
}

private fun MoveDirection.isReversed(other: MoveDirection): Boolean = when (this) {
  N -> other == S
  S -> other == N
  E -> other == W
  W -> other == E
}

private data class HeatMapNode(val x: Int, val y: Int)
private data class HeatMapEdge(val to: HeatMapNode, val w: Int, val d: MoveDirection)

private infix fun Int.`#`(v: Int) = HeatMapNode(this, v)

private class HeatMap(
  xSize: Int,
  ySize: Int,
  private val adj: Map<HeatMapNode, List<HeatMapEdge>>,
) {
  private val start: HeatMapNode = 0 `#` 0
  private val end: HeatMapNode = xSize - 1 `#` ySize - 1

  fun shortestPathLength(max: Int, min: Int = 1): Long {
    data class State(val heatMapNode: HeatMapNode, val dir: MoveDirection, val dirCount: Int)
    data class StateWithDistance(val state: State, val dist: Long)

    val dist = DefaultMap<State, Long>(Long.MAX_VALUE)
    val queue = PriorityQueue(compareBy(StateWithDistance::dist))

    listOf(
      State(start, E, 0),
      State(start, S, 0),
    ).forEach {
      dist[it] = 0L
      queue += StateWithDistance(it, 0L)
    }

    while (queue.isNotEmpty()) {
      val u = queue.remove()

      if (u.state.heatMapNode == end && u.state.dirCount >= min) {
        return u.dist
      }
      adj[u.state.heatMapNode]?.forEach neigh@{ edge ->
        if (u.state.dir.isReversed(edge.d)) return@neigh
        if (edge.d != u.state.dir && u.state.dirCount < min) return@neigh

        val dirCount = when (u.state.dir) {
            edge.d -> (u.state.dirCount + 1).also { if (it > max) return@neigh }
            else -> 1
        }
        val state = State(edge.to, edge.d, dirCount)
        val alt = dist[u.state] + edge.w
        if (alt >= dist[state]) return@neigh

        dist[state] = alt
        queue += StateWithDistance(state, alt)
      }
    }
    throw IllegalStateException("Path to $end not found")
  }
}
