import CardsAndBid.PartOne
import CardsAndBid.PartTwo
import CardsAndBid.Type.*

data object Day7 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    data.map { it.toCardsAndBid(::PartOne) }.totalWinnings().printIt()
    data.map { it.toCardsAndBid(::PartTwo) }.totalWinnings().printIt()
  }
}

private fun <T : CardsAndBid<T>> List<T>.totalWinnings(): Long =
  sorted().foldIndexed(0L) { idx, acc, cardsAndBid ->
    (idx + 1) * cardsAndBid.bid + acc
  }

private sealed class CardsAndBid<T : Comparable<T>>(val bid: Long) : Comparable<T> {
  class PartOne(cards: String, bid: Long) : CardsAndBid<PartOne>(bid) {
    val type: Type = cards.toType()
    val score: Long = cards.score { it.score }
    override fun compareTo(other: PartOne): Int = COMPARATOR.compare(this, other)

    companion object {
      val COMPARATOR: Comparator<PartOne> =
        compareByDescending<PartOne> { it.type }.thenComparing(compareBy { it.score })

      private val Char.score: Long
        get() = when {
          isDigit() -> digitToInt() - 2L
          this == 'T' -> 8L
          this == 'J' -> 9L
          this == 'Q' -> 10L
          this == 'K' -> 11L
          this == 'A' -> 12L
          else -> throw IllegalArgumentException("Unknown card representation: $this")
        }

      private fun String.toType(): Type {
        val counts = groupingBy { it }.eachCount().values
        return when {
          5 in counts -> Five
          4 in counts -> Four
          3 in counts && 2 in counts -> Full
          3 in counts -> Three
          counts.count { it == 2 } == 2 -> Two
          2 in counts -> One
          else -> High
        }
      }
    }
  }

  class PartTwo(cards: String, bid: Long) : CardsAndBid<PartTwo>(bid) {
    val type: Type = cards.toType()
    val score: Long = cards.score { it.score }
    override fun compareTo(other: PartTwo): Int = COMPARATOR.compare(this, other)

    companion object {
      val COMPARATOR: Comparator<PartTwo> =
        compareByDescending<PartTwo> { it.type }.thenComparing(compareBy { it.score })

      private val Char.score: Long
        get() = when {
          this == 'J' -> 0L
          isDigit() -> digitToInt() - 1L
          this == 'T' -> 9L
          this == 'Q' -> 10L
          this == 'K' -> 11L
          this == 'A' -> 12L
          else -> throw IllegalArgumentException("Unknown card representation: $this")
        }
    }

    private fun String.toType(): Type {
      val jokersCount = count { it == 'J' }
      val otherCounts = filterNot { it == 'J' }.groupingBy { it }.eachCount().values
      return when {
        (4 <= jokersCount)
          || (3 == jokersCount && 2 in otherCounts)
          || (2 == jokersCount && 3 in otherCounts)
          || (1 == jokersCount && 4 in otherCounts)
          || (5 in otherCounts)
        -> Five

        (3 <= jokersCount)
          || (2 == jokersCount && 2 in otherCounts)
          || (1 == jokersCount && 3 in otherCounts)
          || (4 in otherCounts)
        -> Four

        (2 == jokersCount && 2 in otherCounts && 1 in otherCounts)
          || (1 == jokersCount && otherCounts.count { it == 2 } == 2)
          || (3 in otherCounts && 2 in otherCounts)
        -> Full

        (2 <= jokersCount)
          || (1 == jokersCount && 2 in otherCounts)
          || (3 in otherCounts)
        -> Three

        (1 == jokersCount && 2 in otherCounts && 1 in otherCounts)
          || otherCounts.count { it == 2 } == 2
        -> Two

        1 == jokersCount
          || 2 in otherCounts
        -> One

        else -> High
      }
    }
  }

  companion object {
    private fun String.score(charScore: (Char) -> Long): Long =
      fold(1L) { score, char -> score * 13L + charScore(char) }
  }

  enum class Type {
    Five, Four, Full, Three, Two, One, High;
  }
}

private fun <T : CardsAndBid<T>> String.toCardsAndBid(create: (cards: String, bid: Long) -> T): T {
  val (cards, bid) = split(" ")
  return create(cards, bid.toLong())
}
