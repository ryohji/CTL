package app.test_safety2

import app.test_safety2.Location.*
import ctl.*
import thread.*

fun main() = (SharedVar(0) with listOf(LocalVar on P0, LocalVar on Q0, LocalVar on R0)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("P.inc", P0 to P1) { if (it.shared.x < 2) from(it.shared.x + 1) else null },
        Transition("P.dec", P1 to P2) { from(it.shared.x - 1) },
        Transition("Q.inc", Q0 to Q1) { if (it.shared.x < 2) from(it.shared.x + 1) else null },
        Transition("Q.dec", Q1 to Q2) { from(it.shared.x - 1) },
        Transition("R.inc", R0 to R1) { if (it.shared.x < 2) from(it.shared.x + 1) else null },
        Transition("R.dec", R1 to R2) { from(it.shared.x - 1) }
    ), initial)
    val proposition = not(eu(True, "x=3"))
    val label = graph.mark(proposition where listOf(
        "x=3" denote { it.shared.x == 3 }
    ))
    val targetStates = label.filter { it.proposition == proposition }.map { it.state }
    println(graph.toDot(initial, label) { state -> state in targetStates })
}

private enum class Location : thread.Location { P0, P1, P2, Q0, Q1, Q2, R0, R1, R2 }

private data class SharedVar(val x: Int) {
    override fun toString() = "x=$x"
}

private object LocalVar {
    override fun toString() = "()"
}

private fun from(x: Int) = Transition.Variable(SharedVar(x), LocalVar)
