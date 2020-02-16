package thread

import graph.Edge
import graph.Link
import graph.Node

data class SystemState<SharedVar, LocalVar>(val shared: SharedVar, val threadStates: List<ThreadState<LocalVar>>)
data class ThreadState<LocalVar>(val location: Location, val variable: LocalVar)

infix fun <SharedVar, LocalVar> SharedVar.with(threadStates: List<ThreadState<LocalVar>>) = SystemState(this, threadStates)
infix fun <LocalVar> LocalVar.on(location: Location) = ThreadState(location, this)
infix fun <SharedVar, LocalVar> SharedVar.with(local: LocalVar) = Transition.Variable(this, local)

interface Location

data class Transition<SharedVar, LocalVar>(val name: String, val arrow: Pair<Location, Location>, val apply: (Variable<SharedVar, LocalVar>) -> Variable<SharedVar, LocalVar>?) {
    data class Variable<SharedVar, LocalVar>(val shared: SharedVar, val local: LocalVar)

    val location get() = arrow.first
    val boundTo get() = arrow.second
}

fun <SharedVar, LocalVar> buildGraph(transitions: List<Transition<SharedVar, LocalVar>>, initial: SystemState<SharedVar, LocalVar>): Set<Edge<SystemState<SharedVar, LocalVar>>> = mutableSetOf<Edge<SystemState<SharedVar, LocalVar>>>().also { graph ->
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
                graph.addAll(if (states.isEmpty()) listOf(Node(state)) else states.map {
                    Link(
                        state,
                        it
                    )
                })
                frontier.apply { addAll(states); removeAll(graph.map { it.node }) }
            }
        }
    }
}

private fun <T> List<T>.alteredAt(i: Int, x: T): List<T> = take(i) + x + drop(i + 1)
