import MountainMapDAG.Companion.toMountainMapDAG
import MountainMapGraph.Companion.toMountainMapGraph
import java.util.LinkedList

data object Day23 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.toMountainMapDAG().findLongestPath().printIt()
    data.toMountainMapGraph().findLongestPath().printIt()
  }
}

private enum class MapDirection {
  U, D, L, R;

  val opposite: MapDirection
    get() = when (this) {
      U -> D
      D -> U
      L -> R
      R -> L
    }
}

private data class MountainMapNode(val x: Int, val y: Int) {
  fun move(direction: MapDirection): MountainMapNode = when (direction) {
    MapDirection.U -> copy(y = y - 1)
    MapDirection.D -> copy(y = y + 1)
    MapDirection.L -> copy(x = x - 1)
    MapDirection.R -> copy(x = x + 1)
  }
}

private class MountainMapGraph private constructor(
  private val adj: Map<MountainMapNode, Set<MountainMapNode>>,
  private val start: MountainMapNode,
  private val end: MountainMapNode,
) {
  fun findLongestPath(
    point: MountainMapNode = start,
    current: Int = 0,
    visited: MutableSet<MountainMapNode> = HashSet(),
  ): Int {
    if (point in visited) return -1
    if (point == end) return current

    visited += point
    val max = adj[point]?.maxOfOrNull { neighbour ->
      findLongestPath(neighbour, current + 1, visited)
    } ?: -1
    visited -= point

    return max
  }

  companion object {
    fun List<String>.toMountainMapGraph(): MountainMapGraph {
      val adj = LazyDefaultMap<MountainMapNode, MutableSet<MountainMapNode>>(::mutableSetOf)

      for (y in indices) for (x in this[y].indices) {
        val node = MountainMapNode(x, y)
        fun MountainMapNode.addAsNeighbour() =
          runIf(y in indices && x in this@toMountainMapGraph[y].indices && this@toMountainMapGraph[y][x] != '#') {
            adj[node] += this
          }
        MapDirection.entries.forEach { node.move(it).addAsNeighbour() }
      }
      val start = MountainMapNode(x = this[0].indexOf('.'), y = 0)
      val end = MountainMapNode(x = this[lastIndex].indexOf('.'), y = lastIndex)
      return MountainMapGraph(adj, start, end)
    }
  }
}

private class MountainMapDAG private constructor(
  private val adj: Map<MountainMapNode, Set<MountainMapNode>>,
) {
  enum class NodeMark { Permanent, Temporary, None }

  private fun topologicalSort(): List<MountainMapNode> {
    val result = LinkedList<MountainMapNode>()
    val nodes = adj.toNodes()
    val areNone = nodes.toMutableSet()
    val marks = nodes.associateWith { NodeMark.None }.toMutableMap()

    fun visit(n: MountainMapNode) {
      if (marks[n] == NodeMark.Permanent) return
      if (marks[n] == NodeMark.Temporary) throw IllegalStateException("Cycle in graph")

      areNone -= n
      marks[n] = NodeMark.Temporary

      adj[n]?.forEach { m -> visit(m) }

      marks[n] = NodeMark.Permanent
      result.addFirst(n)
    }
    while (true) {
      val node = areNone.firstOrNull() ?: break
      visit(node)
    }
    return result
  }

  fun findLongestPath(): Int {
    val lengthTo = DefaultMap<MountainMapNode, Int>(0)
    val topologicalSort = topologicalSort()
    topologicalSort.forEach { v ->
      adj[v]?.forEach { w ->
        val updatedLength = lengthTo[v] + 1
        if (lengthTo[w] < updatedLength) lengthTo[w] = updatedLength
      }
    }
    return lengthTo.values.max()
  }

  companion object {
    fun List<String>.toMountainMapDAG(): MountainMapDAG {
      val adj = LazyDefaultMap<MountainMapNode, MutableSet<MountainMapNode>>(::mutableSetOf)

      val queue = ArrayDeque<Pair<MountainMapNode, MapDirection>>()
      val visited = mutableSetOf<Pair<MountainMapNode, MapDirection>>()

      tailrec fun go(nodeFromMapDirection: Pair<MountainMapNode, MapDirection>) {
        visited += nodeFromMapDirection
        val (node, fromMapDirection) = nodeFromMapDirection
        when (val nodeType = this[node]!!) {
          '.' -> sequence { for (d in MapDirection.entries) if (d != fromMapDirection.opposite) yield(d) }
          else -> sequenceOf(nodeType.toMapDirection())
        }
          .map { node.move(it) to it }
          .forEach { neighFromMapDirection ->
            if (neighFromMapDirection in visited) return@forEach

            val (neigh, formMapDirection) = neighFromMapDirection
            val neighType = this[neigh]
            if (neighType == null || neighType == '#') return@forEach
            if (neighType != '.' && neighType.toMapDirection().opposite == formMapDirection) return@forEach

            adj[node] += neigh
            queue += neighFromMapDirection
          }
        go(queue.removeFirstOrNull() ?: return)
      }
      go(Pair(MountainMapNode(1, 0), MapDirection.D))
      return MountainMapDAG(adj)
    }

    private fun Map<MountainMapNode, Set<MountainMapNode>>.toNodes(): Set<MountainMapNode> = buildSet {
      addAll(keys)
      values.forEach { addAll(it) }
    }
  }
}

private operator fun List<String>.get(node: MountainMapNode): Char? = with(node) {
  val map = this@get
  runIf(y in indices && x in map[y].indices) { map[y][x] }
}

private fun Char.toMapDirection(): MapDirection = when (this) {
  '^' -> MapDirection.U
  '>' -> MapDirection.R
  'v' -> MapDirection.D
  '<' -> MapDirection.L
  else -> throw IllegalArgumentException("Cannot convert $this to MapDirection")
}
