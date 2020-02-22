package thread

import graph.Edge
import graph.Graph
import graph.Link
import graph.Node

data class SystemState<SharedVar, LocalVar>(val shared: SharedVar, val threadStates: List<ThreadState<LocalVar>>) {
    override fun toString() = "$shared, $threadStates"
}
infix fun <SharedVar, LocalVar> SharedVar.with(threadStates: List<ThreadState<LocalVar>>) = SystemState(this, threadStates)

data class ThreadState<LocalVar>(val location: Location, val variable: LocalVar) {
    override fun toString() = "$variable@$location"
}
infix fun <LocalVar> LocalVar.on(location: Location) = ThreadState(location, this)

interface Location

data class Transition<SharedVar, LocalVar>(val name: String, val location: Location, val boundTo: Location, val apply: (Variable<SharedVar, LocalVar>) -> Variable<SharedVar, LocalVar>?) {
    data class Variable<SharedVar, LocalVar>(val shared: SharedVar, val local: LocalVar)
    constructor(name: String, arrow: Pair<Location, Location>, apply: (Variable<SharedVar, LocalVar>) -> Variable<SharedVar, LocalVar>?): this(name, arrow.first, arrow.second, apply)
}
infix fun <SharedVar, LocalVar> SharedVar.with(local: LocalVar) = Transition.Variable(this, local)

fun <SharedVar, LocalVar> buildGraph(transitions: List<Transition<SharedVar, LocalVar>>, initial: SystemState<SharedVar, LocalVar>): Graph<SystemState<SharedVar, LocalVar>> = mutableSetOf<Edge<SystemState<SharedVar, LocalVar>>>().also { graph ->
    val frontier = mutableSetOf(initial)
    while (frontier.isNotEmpty()) {
        frontier.first().let { state ->
            state.threadStates.mapIndexed { i, t ->
                transitions.filter { t.location == it.location }.mapNotNull { transition ->
                    with(transition) {
                        apply(state.shared with t.variable)?.let { (shared, local) ->
                            (shared with state.threadStates.alteredAt(i, local on boundTo)).also {
                                graph.add(Link(state, it, name))
                            }
                        }
                    }
                }
            }.flatten().also { states ->
                if (states.isEmpty()) graph.add(Node(state))
                frontier.apply { addAll(states); removeAll(graph.map { it.node }) }
            }
        }
    }
}

private fun <T> List<T>.alteredAt(i: Int, x: T): List<T> = take(i) + x + drop(i + 1)
