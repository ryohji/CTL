package thread

data class SystemState(val shared: SharedVar, val threadStates: List<ThreadState>)
data class ThreadState(val location: Location, val variable: LocalVar)

infix fun SharedVar.with(threadStates: List<ThreadState>) = SystemState(this, threadStates)
infix fun LocalVar.on(location: Location) = ThreadState(location, this)
infix fun SharedVar.with(local: LocalVar) = Transition.Variable(this, local)

interface SharedVar
interface LocalVar
interface Location

data class Transition(val name: String, val arrow: Pair<Location, Location>, val apply: (Variable) -> Variable?) {
    data class Variable(val shared: SharedVar, val local: LocalVar)

    val location get() = arrow.first
    val boundTo get() = arrow.second
}

interface Edge {
    val state: SystemState
}

data class Node(override val state: SystemState) : Edge
data class Link(override val state: SystemState, val boundTo: SystemState) : Edge

fun buildGraph(transitions: List<Transition>, initial: SystemState): Set<Edge> = mutableSetOf<Edge>().also { graph ->
    val frontier = mutableSetOf(initial)
    while (frontier.isNotEmpty()) {
        frontier.first().let { state ->
            state.threadStates.mapIndexed { i, t ->
                transitions.filter { t.location == it.location }.mapNotNull { transition ->
                    with(transition) {
                        apply(state.shared with t.variable)?.let {
                            it.shared with state.threadStates.alteredAt(i, it.local on boundTo)
                        }
                    }
                }
            }.flatten().also { states ->
                graph.addAll(if (states.isEmpty()) listOf(Node(state)) else states.map { Link(state, it) })
                frontier.apply { addAll(states); removeAll(graph.map { it.state }) }
            }
        }
    }
}

private fun <T> List<T>.alteredAt(i: Int, x: T): List<T> = take(i) + x + drop(i + 1)
