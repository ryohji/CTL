package app.test2

import app.test2.Location.*
import ctl.*
import thread.Transition
import thread.buildGraph
import thread.on
import thread.with

fun main() = (SharedVar(0) with listOf(Nil on P0)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("x:= 1", P0 to P1) { SharedVar(1) with it.local },
        Transition("x:= 2", P1 to P2) { SharedVar(2) with it.local },
        Transition("x:= 3", P1 to P2) { SharedVar(3) with it.local },
        Transition("x:= 4", P1 to P2) { SharedVar(4) with it.local },
        Transition("x-= 1", P2 to P3) { SharedVar(it.shared.x - 1) with it.local }
    ), initial)
    val label = graph.mark(ex(ex(ex("p"))) where listOf(
        "p" denote { it.shared.x == 2 }
    ))
    println(graph.toDot(initial, label))
}

private enum class Location : thread.Location { P0, P1, P2, P3 }

private data class SharedVar(val x: Int) {
    override fun toString() = "x=$x"
}

private object Nil {
    override fun toString() = "()"
}

