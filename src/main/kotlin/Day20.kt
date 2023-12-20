import ModuleState.*
import java.util.LinkedList

data object Day20 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return

    data.toModulesState().click(1000).printIt()
    data.toModulesState().estimatedPressesForRx().printIt()
  }
}

private data class Signal(val from: ModuleId, val wire: Boolean)
private data class YieldSignal(val from: ModuleId, val wire: Boolean, val to: String)

private typealias ModuleId = String

private open class ModuleState(val id: ModuleId, val destinations: Set<ModuleId>, val parents: Set<ModuleId>) {
  open fun receive(signal: Signal): Boolean? = false

  class FlipFlop(id: ModuleId, destinations: Set<ModuleId>, parents: Set<ModuleId>) :
    ModuleState(id, destinations, parents) {
    private var state: Boolean = false

    override fun receive(signal: Signal) = runIf(!signal.wire) { state.not().also { state = it } }
  }

  class Conjunction(id: ModuleId, destinations: Set<ModuleId>, parents: Set<ModuleId>) :
    ModuleState(id, destinations, parents) {
    private val states: MutableMap<ModuleId, Boolean> = parents.associateWith { false }.toMutableMap()

    override fun receive(signal: Signal): Boolean {
      states[signal.from] = signal.wire
      return !states.all { it.value }
    }
  }
}

private class ModulesState(private val states: Map<ModuleId, ModuleState>) {
  private var clicks: Long = 0L

  private fun singleClick(): Sequence<YieldSignal> = sequence {
    clicks += 1
    val signals = LinkedList<Signal>().apply { add(Signal("button", false)) }
    while (true) {
      val signal = signals.pollFirst() ?: break
      states[signal.from]?.destinations?.forEach { signalTo ->
        yield(YieldSignal(signal.from, signal.wire, signalTo))
        states[signalTo]?.receive(signal)?.let { signals.addLast(Signal(signalTo, it)) }
      }
    }
  }

  fun click(times: Int): Long {
    var lowCounter = 0L
    var highCounter = 0L
    repeat(times) { singleClick().forEach { if (it.wire) highCounter += 1 else lowCounter += 1 } }
    return lowCounter * highCounter
  }

  fun estimatedPressesForRx(): Long {
    val rx = states["rx"]!!
    val rxParent = states[rx.parents.single()]!!
    val cycles = rxParent.parents.associateWith { -1L }.toMutableMap()
    while (cycles.values.any { it == -1L }) for (signal in singleClick()) {
      if (signal.to != rxParent.id) continue
      if (!signal.wire) continue
      if (cycles[signal.from] != -1L) continue
      cycles[signal.from] = clicks
    }
    return cycles.values.reduce(::lcm)
  }
}

private typealias ConstructModuleState = (id: ModuleId, destinations: Set<ModuleId>, parents: Set<ModuleId>) -> ModuleState

private fun List<String>.toModulesState(): ModulesState {
  val constructState = mutableMapOf<ModuleId, ConstructModuleState>()
  val moduleDestinations = LazyDefaultMap<ModuleId, MutableSet<ModuleId>>(::mutableSetOf)
  val moduleParents = LazyDefaultMap<ModuleId, MutableSet<ModuleId>>(::mutableSetOf)
  val outputs = mutableSetOf<ModuleId>()

  for (line in this) {
    val (rawFrom, rawTo) = line.split(" -> ")
    val from = rawFrom.removePrefix("%").removePrefix("&")
    rawTo.split(", ").forEach { to ->
      moduleDestinations[from] += to
      moduleParents[to] += from
      outputs += to
    }
    constructState[from] = when {
      rawFrom.startsWith('%') -> ::FlipFlop
      rawFrom.startsWith('&') -> ::Conjunction
      else -> ::ModuleState
    }
  }
  constructState["button"] = ::ModuleState
  moduleDestinations["button"] += "broadcaster"
  (outputs - constructState.keys).forEach { constructState[it] = ::ModuleState }
  return ModulesState(constructState.mapValues { it.value(it.key, moduleDestinations[it.key], moduleParents[it.key]) })
}
