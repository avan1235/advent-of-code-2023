import java.io.ByteArrayOutputStream
import java.io.PrintStream

inline fun <T: Any> runIf(c: Boolean, action: () -> T): T? = if (c) action() else null

inline fun <reified T> String.value(): T = when (T::class) {
  String::class -> this as T
  Long::class -> toLongOrNull() as T
  Int::class -> toIntOrNull() as T
  else -> TODO("Add support to read ${T::class.java.simpleName}")
}

inline fun <reified T> String.separated(by: String): List<T> = split(by).map { it.value() }

fun <T> T.printIt() = also { println(it) }

fun <U, V> List<U>.groupSeparatedBy(
  separator: (U) -> Boolean,
  includeSeparator: Boolean = false,
  transform: (List<U>) -> V,
): List<V> = sequence {
  var curr = mutableListOf<U>()
  forEach {
    if (separator(it) && curr.isNotEmpty()) yield(transform(curr))
    if (separator(it)) curr = if (includeSeparator) mutableListOf(it) else mutableListOf()
    else curr += it
  }
  if (curr.isNotEmpty()) yield(transform(curr))
}.toList()

fun <T> List<List<T>>.transpose(): List<List<T>> {
  val n = map { it.size }.toSet().singleOrNull()
    ?: throw IllegalArgumentException("Invalid data to transpose: $this")
  return List(n) { y -> List(size) { x -> this[x][y] } }
}

infix fun Int.directedTo(o: Int) = if (this <= o) this..o else this downTo o

class DefaultMap<K, V>(
  private val default: V,
  private val map: MutableMap<K, V> = HashMap(),
) : MutableMap<K, V> by map {
  override fun get(key: K): V = map.getOrDefault(key, default).also { map[key] = it }
  operator fun plus(kv: Pair<K, V>): DefaultMap<K, V> = (map + kv).toDefaultMap(default)
  override fun toString() = map.toString()
  override fun hashCode() = map.hashCode()
  override fun equals(other: Any?) = map == other
}

fun <K, V> Map<K, V>.toDefaultMap(default: V) = DefaultMap(default, toMutableMap())

class LazyDefaultMap<K, V>(
  private val default: () -> V,
  private val map: MutableMap<K, V> = HashMap(),
) : MutableMap<K, V> by map {
  override fun get(key: K): V = map.getOrDefault(key, default()).also { map[key] = it }
  operator fun plus(kv: Pair<K, V>): LazyDefaultMap<K, V> = (map + kv).toLazyDefaultMap(default)
  override fun toString() = map.toString()
  override fun hashCode() = map.hashCode()
  override fun equals(other: Any?) = map == other
}

fun <K, V> Map<K, V>.toLazyDefaultMap(default: () -> V) = LazyDefaultMap(default, toMutableMap())

fun catchSystemOut(action: () -> Unit) = ByteArrayOutputStream().also {
  val originalOut = System.out
  System.setOut(PrintStream(it))
  action()
  System.setOut(originalOut)
}.toString()

interface Graph<Node> {
  enum class SearchType { DFS, BFS }

  fun neighbours(node: Node): Iterable<Node>

  fun search(
    from: Node,
    type: SearchType,
    visit: (Node, Node) -> Boolean = { _, _ -> true },
    action: (node: Node, distance: Int) -> Unit = { _, _ -> },
  ): Set<Node> {
    val visited = mutableSetOf<Node>()
    val queue = ArrayDeque<Pair<Node, Int>>()
    tailrec fun go(curr: Pair<Node, Int>) {
      visited += curr.also { action(it.first, it.second) }.first
      neighbours(curr.first).forEach { if (it !in visited && visit(curr.first, it)) queue += Pair(it, curr.second + 1) }
      when (type) {
        SearchType.DFS -> go(queue.removeLastOrNull() ?: return)
        SearchType.BFS -> go(queue.removeFirstOrNull() ?: return)
      }
    }
    return visited.also { go(Pair(from, 0)) }
  }
}

fun <T> List<T>.repeat(count: Int): List<T> = List(size * count) { this[it % size] }

tailrec fun gcd(a: Long, b: Long): Long =
  if (b == 0L) a else gcd(b, a % b)

fun lcm(a: Long, b: Long): Long =
  a / gcd(a, b) * b
