package app.test_query

import app.test_query.Location.*
import ctl.*
import thread.*

fun main() = (SharedVar(0) with listOf(LocalVar(0) on P0, LocalVar(0) on P0, LocalVar(0) on P0)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("read", P0 to P1) { from(it.shared.x, it.shared.x) },
        Transition("write", P1 to P2) { from(it.local.t + 1, it.local.t) }
    ), initial)
    val proposition = not(EU(True, (not(EX(True)) and not("x=3"))))
    val label = graph.mark(proposition where listOf(
        "x=1" denote { it.shared.x == 1 },
        "x=2" denote { it.shared.x == 2 },
        "x=3" denote { it.shared.x == 3 }
    ))
    val targetStates = label.filter { it.proposition == proposition }.map { it.state }
    println(graph.toDot(initial, label) { state -> state in targetStates })
}

private enum class Location : thread.Location { P0, P1, P2 }

private data class SharedVar(val x: Int) {
    override fun toString() = "x=$x"
}

private data class LocalVar(val t: Int) {
    override fun toString() = "t=$t"
}

private fun from(x: Int, t: Int) = Transition.Variable(SharedVar(x), LocalVar(t))
