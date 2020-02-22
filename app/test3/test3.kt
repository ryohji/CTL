package app.test3

import app.test3.Location.*
import ctl.*
import thread.*

fun main() = (SharedVar(1) with listOf(Nil on S)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("", S to S) { if (it.shared.x < 16) it.doubled() else null },
        Transition("", S to S) { if (it.shared.x < 16) it.doubled().incremented() else null }
    ), initial)
    val label = graph.mark(eu("x=1" or "mod x 2=0","x>=16" and "mod x 4=0") where listOf(
        "p" denote { it.shared.x == 1 || it.shared.x % 2 == 0 },
        "q" denote { it.shared.x >= 16 && it.shared.x % 4 == 0 },
        "x=1" denote { it.shared.x == 1 },
        "mod x 2=0" denote { it.shared.x % 2 == 0 },
        "x>=16" denote { it.shared.x >= 16 },
        "mod x 4=0" denote { it.shared.x % 4 == 0}
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

private fun Transition.Variable<SharedVar, Nil>.doubled() = Transition.Variable(SharedVar(shared.x * 2), local)
private fun Transition.Variable<SharedVar, Nil>.incremented() = Transition.Variable(SharedVar(shared.x + 1), local)
