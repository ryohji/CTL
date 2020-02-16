package thread

data class SystemState<SharedVar>(val shared: SharedVar, val threadStates: List<ThreadState>)
data class ThreadState(val location: Location, val variable: LocalVar)

infix fun <SharedVar> SharedVar.with(threadStates: List<ThreadState>) = SystemState(this, threadStates)
infix fun LocalVar.on(location: Location) = ThreadState(location, this)
infix fun <SharedVar> SharedVar.with(local: LocalVar) = Transition.Variable(this, local)

interface LocalVar
interface Location

data class Transition<SharedVar>(val name: String, val arrow: Pair<Location, Location>, val apply: (Variable<SharedVar>) -> Variable<SharedVar>?) {
    data class Variable<SharedVar>(val shared: SharedVar, val local: LocalVar)

    val location get() = arrow.first
    val boundTo get() = arrow.second
}

interface Edge<SharedVar> {
    val state: SystemState<SharedVar>
}

data class Node<SharedVar>(override val state: SystemState<SharedVar>) : Edge<SharedVar>
data class Link<SharedVar>(override val state: SystemState<SharedVar>, val boundTo: SystemState<SharedVar>) : Edge<SharedVar>

fun <SharedVar> buildGraph(transitions: List<Transition<SharedVar>>, initial: SystemState<SharedVar>): Set<Edge<SharedVar>> = mutableSetOf<Edge<SharedVar>>().also { graph ->
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
