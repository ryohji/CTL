package app.test4

import app.test4.Location.*
import ctl.*
import thread.*

fun main() = (SharedVar(1) with listOf(Nil on S)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("", S to S) { if (it.shared.x < 9) it.incremented() else null },
        Transition("", S to S) { if (it.shared.x == 6) it.xAltered(3) else null },
        Transition("", S to S) { if (it.shared.x == 9) it.xAltered(5) else null }
    ), initial)
    val label = graph.mark(eg(True) or eg("x<=7") or eg("x>=4") where listOf(
        "x<=7" denote { it.shared.x <= 7 },
        "x>=4" denote { it.shared.x >= 4 }
    ))
    println(graph.toDot(initial, label))
}

private enum class Location : thread.Location { S }

private data class SharedVar(val x: Int) {
    override fun toString() = "x=$x"
}

private object Nil {
    override fun toString() = "()"
}

private fun Transition.Variable<SharedVar, Nil>.xAltered(x: Int) = Transition.Variable(SharedVar(x), local)
private fun Transition.Variable<SharedVar, Nil>.incremented() = Transition.Variable(SharedVar(shared.x + 1), local)
