import java.util.*

data object Day4 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val cardsGroups = data.map { it.toCardsGroups() }

    cardsGroups.sumOf { if (it.matching > 0) 1 shl it.matching - 1 else 0 }.printIt()

    cardsGroups.fold(CardsCounter()) { (cardsCount, cardsCopiesCounters), cards ->
      val currCount = 1 + (cardsCopiesCounters.removeFirstOrNull() ?: 0)
      CardsCounter(
        cardsCount = cardsCount + currCount,
        cardsCopiesCounters = LinkedList<Int>().apply {
          cardsCopiesCounters.forEachIndexed { idx, copiesCount ->
            addLast(if (idx < cards.matching) copiesCount + currCount else copiesCount)
          }
          repeat(cards.matching - cardsCopiesCounters.size) { addLast(currCount) }
        }
      )
    }.cardsCount.printIt()
  }
}

private data class CardsCounter(
  val cardsCount: Int = 0,
  val cardsCopiesCounters: LinkedList<Int> = LinkedList(),
)

private fun String.toCardsGroups(): CardsGroups {
  val (win, my) = removePrefix("Card")
    .trim()
    .dropWhile { it.isDigit() }
    .removePrefix(":")
    .trim()
    .split("|").map {
      it
        .trim()
        .split(Regex("\\s+"))
        .map2Set { number -> number.trim().toInt() }
    }
  return CardsGroups(win, my)
}

private data class CardsGroups(
  val win: Set<Int>,
  val my: Set<Int>,
) {
  val matching: Int = win.intersect(my).size
}
