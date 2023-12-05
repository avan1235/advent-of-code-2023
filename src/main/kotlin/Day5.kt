import Overlapping.*
import java.util.*

data object Day5 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    val maps = data.drop(2).toMaps()

    maps.fold(data.first().toSeeds()) { seeds, map ->
      seeds.map { seed ->
        for ((sourceRange, destinationStart) in map.mappings) {
          if (seed !in sourceRange) continue
          return@map seed - sourceRange.first + destinationStart
        }
        seed
      }
    }
      .min()
      .printIt()

    maps.fold(data.first().toSeedsRanges()) { seedsRanges, map ->
      val mapped = LinkedList<LongRange>()
      val unmapped = LinkedList(seedsRanges)
      mapSingleSeedRange@ while (unmapped.isNotEmpty()) {
        val unmappedSeedRange = unmapped.removeFirst()

        for ((sourceRange, destinationStart) in map.mappings) {
          when (val overlap = sourceRange.isOverlapping(unmappedSeedRange)) {
            None -> {}

            Same -> {
              mapped += destinationStart..<destinationStart + unmappedSeedRange.size
              continue@mapSingleSeedRange
            }

            Outside -> {
              mapped += (unmappedSeedRange.first - sourceRange.first + destinationStart).let { it..<it + unmappedSeedRange.size }
              continue@mapSingleSeedRange
            }

            is Split -> {
              mapped += (overlap.inside.first - sourceRange.first + destinationStart).let { it..<it + overlap.inside.size }
              unmapped += overlap.outside
              continue@mapSingleSeedRange
            }

            is Inside -> {
              mapped += (overlap.inside.first - sourceRange.first + destinationStart).let { it..<it + overlap.inside.size }
              unmapped += overlap.outsideLeft
              unmapped += overlap.outsideRight
              continue@mapSingleSeedRange
            }
          }
        }
        mapped += unmappedSeedRange
      }
      mapped
    }
      .minOf { it.first }
      .printIt()
  }
}

private data class MapMapping(val sourceRange: LongRange, val destinationStart: Long)
private data class Map(val mappings: List<MapMapping>)

private fun List<String>.toMaps(): List<Map> = groupSeparatedBy(separator = { it == "" }) { group ->
  group.drop(1).map { line ->
    val (destinationStart, sourceStart, rangeLength) = line.trim().split(Regex("\\s+")).map { it.toLong() }
    val sourceRange = sourceStart..<sourceStart + rangeLength
    MapMapping(sourceRange, destinationStart)
  }.let(::Map)
}

private fun String.toSeeds(): List<Long> = removePrefix("seeds: ")
  .split(" ")
  .map { it.toLong() }

private fun String.toSeedsRanges(): List<LongRange> = removePrefix("seeds: ")
  .split(" ")
  .map { it.toLong() }
  .windowed(size = 2, step = 2)
  .map { (start, length) -> start..<start + length }

private val LongRange.size: Long get() = last - first + 1

private sealed interface Overlapping {
  data object None : Overlapping
  data object Same : Overlapping
  data class Inside(val outsideLeft: LongRange, val inside: LongRange, val outsideRight: LongRange) : Overlapping
  data object Outside : Overlapping
  data class Split(val inside: LongRange, val outside: LongRange) : Overlapping
}

private fun LongRange.isOverlapping(o: LongRange): Overlapping {
  if (last < o.first || first > o.last) return None
  if (first == o.first && last == o.last) return Same
  if (first > o.first && last < o.last) return Inside(
    outsideLeft = o.first..<first,
    inside = first..last,
    outsideRight = last + 1..o.last,
  )
  if (first <= o.first && last >= o.last) return Outside
  if (last < o.last && first <= o.first) return Split(
    inside = o.first..last,
    outside = last + 1..o.last,
  )
  if (first > o.first && last >= o.last) return Split(
    inside = first..o.last,
    outside = o.first..<first,
  )
  throw IllegalStateException("All cases should be handled: $this.isOverlapping($o)")
}
