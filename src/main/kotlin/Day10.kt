import Graph.SearchType.DFS
import Pipe.*
import Pipe.End.*

data object Day10 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    val rawMap = data.toRawPipeMap()
    val start = rawMap.toPipeStart()
    val map = PipeMap(rawMap, start)
    val graph = map.toPipeGraph(start)
    val distances = graph.distances()

    graph.adj.keys.maxOf { distances[it] ?: -1 }.printIt()

    val supplementaryGraph = map.toSupplementaryPipeGraph(pipe = distances.keys)
    start.location.supplementaryPipeGraphLocations().mapNotNull {
      supplementaryGraph.countEnclosedReachable(from = it, map)
    }
      .distinct()
      .single()
      .printIt()
  }
}

private fun List<String>.toRawPipeMap(): RawPipeMap =
  map { line -> line.map { it.toPipe() }.toTypedArray() }.toTypedArray()

private fun RawPipeMap.toPipeStart(): PipeStart {
  fun pipeAt(x: Int, y: Int): Pipe? = getOrNull(y)?.getOrNull(x)

  for (y in indices) for (x in this[y].indices) if (pipeAt(x, y) == Start) {
    val location = PipeLocation(x, y)
    val u = pipeAt(x, y - 1)
    val d = pipeAt(x, y + 1)
    val l = pipeAt(x - 1, y)
    val r = pipeAt(x + 1, y)
    val pipe = when {
      u?.hasDown == true && d?.hasUp == true -> V
      l?.hasRight == true && r?.hasLeft == true -> H
      u?.hasDown == true && r?.hasLeft == true -> NE
      u?.hasDown == true && l?.hasRight == true -> NW
      d?.hasUp == true && l?.hasRight == true -> SW
      d?.hasUp == true && r?.hasLeft == true -> SE
      else -> throw IllegalArgumentException("Cannot determine pipe type for start in $this")
    }
    return PipeStart(location, pipe)
  }
  throw IllegalArgumentException("Map without Start: $this")
}

private fun SupplementaryPipeGraph.countEnclosedReachable(from: SupplementaryPipeGraphLocation, map: PipeMap): Int? {
  val reachable = search(from, type = DFS)
  if (SupplementaryPipeGraphLocation(0, 0) in reachable) return null

  var count = 0
  for ((x, y) in map.locations) {
    if (SupplementaryPipeGraphLocation(x, y) !in reachable) continue
    if (SupplementaryPipeGraphLocation(x, y + 1) !in reachable) continue
    if (SupplementaryPipeGraphLocation(x + 1, y) !in reachable) continue
    if (SupplementaryPipeGraphLocation(x + 1, y + 1) !in reachable) continue

    count += 1
  }
  return count
}

private fun PipeLocation.supplementaryPipeGraphLocations(): List<SupplementaryPipeGraphLocation> = listOf(
  SupplementaryPipeGraphLocation(x, y),
  SupplementaryPipeGraphLocation(x, y + 1),
  SupplementaryPipeGraphLocation(x + 1, y),
  SupplementaryPipeGraphLocation(x + 1, y + 1),
)

private class SupplementaryPipeGraph(
  val adj: DefaultMap<SupplementaryPipeGraphLocation, List<SupplementaryPipeGraphLocation>>,
) : Graph<SupplementaryPipeGraphLocation> {
  override fun neighbours(node: SupplementaryPipeGraphLocation): Iterable<SupplementaryPipeGraphLocation> = adj[node]
}

private class PipeGraph(
  val start: PipeStart,
  val adj: DefaultMap<PipeLocation, List<PipeLocation>>,
) : Graph<PipeLocation> {

  override fun neighbours(node: PipeLocation): Iterable<PipeLocation> =
    adj[node]

  fun distances(from: PipeLocation = start.location): Map<PipeLocation, Int> = buildMap {
    search(from, Graph.SearchType.BFS) { location, distance ->
      this[location] = distance
    }
  }
}


private fun PipeMap.toPipeGraph(start: PipeStart): PipeGraph = buildMap {
  for (location in locations) {
    val currPipeType = this@toPipeGraph[location]!!
    this[location] = location.getConnectedPipeNeighbours(currPipeType, this@toPipeGraph) ?: when (currPipeType) {
      Ground -> continue
      Start -> location.getConnectedPipeNeighbours(currPipeType, this@toPipeGraph)
      else -> null
    } ?: throw IllegalArgumentException("Unhandled map element: $currPipeType at $location")
  }
}
  .toDefaultMap(emptyList())
  .let { PipeGraph(start, it) }


private fun PipeMap.toSupplementaryPipeGraph(pipe: Set<PipeLocation>): SupplementaryPipeGraph = buildMap {
  for (y in yIndices.run { first..last + 1 }) for (x in xIndices.run { first..last + 1 }) {
    val curr = SupplementaryPipeGraphLocation(x, y)
    this[curr] = curr.getWaterConnectedNeighbours(this@toSupplementaryPipeGraph, pipe)
  }
}
  .toDefaultMap(emptyList())
  .let(::SupplementaryPipeGraph)


private typealias RawPipeMap = Array<Array<Pipe>>

private class PipeMap(
  private val map: RawPipeMap,
  val start: PipeStart,
) {
  val xIndices: IntRange = map.first().indices
  val yIndices: IntRange = map.indices

  val locations: Sequence<PipeLocation>
    get() = sequence { for (y in yIndices) for (x in xIndices) yield(PipeLocation(x, y)) }

  operator fun get(location: PipeLocation): Pipe? = when (location) {
    start.location -> start.pipe
    else -> location.run { if (y in yIndices && x in xIndices) map[y][x] else null }
  }
}


private fun PipeLocation.isConnected(end: End, map: PipeMap): Boolean = when (end) {
  L -> map[this]?.hasLeft
  R -> map[this]?.hasRight
  U -> map[this]?.hasUp
  D -> map[this]?.hasDown
} == true

private data class SupplementaryPipeGraphLocation(val x: Int, val y: Int) {

  fun getWaterConnectedNeighbours(map: PipeMap, pipe: Set<PipeLocation>): List<SupplementaryPipeGraphLocation> =
    listOfNotNull(
      SupplementaryPipeGraphLocation(x, y + 1).takeUnless {
        val l = PipeLocation(x - 1, y)
        val r = PipeLocation(x, y)
        l in pipe && r in pipe && l.isConnected(R, map) && r.isConnected(L, map)
      },
      SupplementaryPipeGraphLocation(x, y - 1).takeUnless {
        val l = PipeLocation(x - 1, y - 1)
        val r = PipeLocation(x, y - 1)
        l in pipe && r in pipe && l.isConnected(R, map) && r.isConnected(L, map)
      },
      SupplementaryPipeGraphLocation(x + 1, y).takeUnless {
        val u = PipeLocation(x, y - 1)
        val d = PipeLocation(x, y)
        u in pipe && d in pipe && u.isConnected(D, map) && d.isConnected(U, map)
      },
      SupplementaryPipeGraphLocation(x - 1, y).takeUnless {
        val u = PipeLocation(x - 1, y - 1)
        val d = PipeLocation(x - 1, y)
        u in pipe && d in pipe && u.isConnected(D, map) && d.isConnected(U, map)
      },
    )
      .filter { it.isOnMap(map) }

  private fun isOnMap(map: PipeMap): Boolean =
    y in map.yIndices.run { first..last + 1 } && x in map.xIndices.run { first..last + 1 }
}

private data class PipeLocation(val x: Int, val y: Int) {
  fun getConnectedPipeNeighbours(type: Pipe, map: PipeMap): List<PipeLocation>? = when (type) {
    V -> listOf(Pair(0, 1), Pair(0, -1))
    H -> listOf(Pair(1, 0), Pair(-1, 0))
    NE -> listOf(Pair(0, -1), Pair(1, 0))
    NW -> listOf(Pair(0, -1), Pair(-1, 0))
    SW -> listOf(Pair(0, 1), Pair(-1, 0))
    SE -> listOf(Pair(0, 1), Pair(1, 0))
    else -> null
  }
    ?.mapNotNull { it.toConnectedPipeNeighbour(map) }

  private fun Pair<Int, Int>.toConnectedPipeNeighbour(map: PipeMap): PipeLocation? {
    val location = PipeLocation(x + first, y + second)
    val isConnected = when {
      first == 0 && second == 1 -> map[location] in setOf(V, NE, NW)
      first == 0 && second == -1 -> map[location] in setOf(V, SE, SW)
      first == 1 && second == 0 -> map[location] in setOf(H, NW, SW)
      first == -1 && second == 0 -> map[location] in setOf(H, NE, SE)
      else -> throw IllegalArgumentException("Invalid diff = $this")
    }
    return location.takeIf { isConnected }
  }
}

private class PipeStart(
  val location: PipeLocation,
  val pipe: Pipe,
)

private enum class Pipe(vararg ends: End) {
  V(U, D),
  H(L, R),
  NE(U, R),
  NW(U, L),
  SW(D, L),
  SE(D, R),
  Ground,
  Start,
  ;

  val hasUp: Boolean = U in ends
  val hasDown: Boolean = D in ends
  val hasLeft: Boolean = L in ends
  val hasRight: Boolean = R in ends

  enum class End { L, R, U, D }
}

private fun Char.toPipe(): Pipe = when (this) {
  '|' -> V
  '-' -> H
  'L' -> NE
  'J' -> NW
  '7' -> SW
  'F' -> SE
  '.' -> Ground
  'S' -> Start
  else -> throw IllegalArgumentException("Unknown map element: $this")
}
