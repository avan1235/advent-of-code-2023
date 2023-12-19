import Rating.Companion.toRating
import Workflow.Rule.*
import kotlin.math.*

data object Day19 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    val (rawWorkflows, rawRatings) = data.groupSeparatedBy(separator = { it == "" }, transform = { it })

    val workflows = rawWorkflows.map { it.toWorkflow() }.associateBy { it.name }
    val ratings = rawRatings.map { it.toRating() }

    ratings.filter { it.isAccepted(workflows) }.sumOf { rating -> "xmas".sumOf { rating[it] } }.printIt()
    combinations("in", MultiState("xmas".associateWith { 1..4000 }), workflows).printIt()
  }
}

private val IntRange.size: Long get() = last - first + 1L

private data class MultiState(val data: Map<Char, IntRange>) {
  val combinations: Long by lazy { data['x']!!.size * data['m']!!.size * data['a']!!.size * data['s']!!.size }

  fun update(category: Char, bound: (IntRange) -> IntRange?): MultiState? =
    bound(data[category]!!)?.takeIf { !it.isEmpty() }?.let { MultiState(data + (category to it)) }
}

private fun combinations(p: String, state: MultiState, workflows: Map<String, Workflow>): Long = when (p) {
  "R" -> 0
  "A" -> state.combinations
  else -> sequence {
    var currState = state
    for (rule in workflows[p]!!.rules) {
      val (accepted, rejected) = rule.split(currState)
      accepted?.let { yield(combinations(rule.destination, it, workflows)) }
      currState = rejected ?: break
    }
  }.sum()
}

private fun Rating.isAccepted(workflows: Map<String, Workflow>): Boolean {
  var curr = "in"
  outer@ while (true) {
    if (curr == "A") return true
    if (curr == "R") return false

    val workflow = workflows[curr]!!
    inner@ for (rule in workflow.rules) {
      val destination = rule.send(this) ?: continue@inner
      curr = destination
      continue@outer
    }
    throw IllegalStateException("No rule found in ${workflow.rules}")
  }
}

private class Rating private constructor(private val scores: Map<Char, Int>) {
  operator fun get(v: Char): Int = scores[v]!!

  companion object {
    fun String.toRating(): Rating = Rating(buildMap {
      removeSurrounding("{", "}").split(',').forEach { single ->
        val (k, v) = single.split('=')
        put(k.single(), v.toInt())
      }
    })
  }
}

private class Workflow(
  val name: String,
  val rules: List<Rule>,
) {
  sealed class Rule(val destination: String) {
    abstract fun send(r: Rating): String?
    abstract fun split(s: MultiState): Pair<MultiState?, MultiState?>

    class Less(to: String, val category: Char, val bound: Int) : Rule(to) {
      override fun send(r: Rating) = runIf(r[category] < bound) { destination }
      override fun split(s: MultiState) = Pair(
        s.update(category) { runIf(bound > it.first) { it.first..<min(it.last + 1, bound) } },
        s.update(category) { runIf(bound > it.first) { min(it.last + 1, bound)..it.last } ?: it },
      )
    }

    class More(to: String, val category: Char, val bound: Int) : Rule(to) {
      override fun send(r: Rating) = runIf(r[category] > bound) { destination }
      override fun split(s: MultiState) = Pair(
        s.update(category) { runIf(bound < it.last) { max(it.first, bound + 1)..it.last } },
        s.update(category) { runIf(bound < it.last) { it.first..<max(it.first, bound + 1) } ?: it },
      )
    }

    class Always(to: String) : Rule(to) {
      override fun send(r: Rating) = destination
      override fun split(s: MultiState) = Pair(s, null)
    }
  }
}

private fun String.toWorkflow(): Workflow {
  val (name, rawRules) = split('{')
  val rules = rawRules.removeSuffix("}").split(',').map { it.toRule() }
  return Workflow(name, rules)
}

private fun String.toRule(): Workflow.Rule = when {
  this == "A" -> Always("A")
  this == "R" -> Always("R")
  contains("<") -> split(Regex("[<:]")).let { (category, bound, to) -> Less(to, category.single(), bound.toInt()) }
  contains(">") -> split(Regex("[>:]")).let { (category, bound, to) -> More(to, category.single(), bound.toInt()) }
  else -> Always(this)
}
