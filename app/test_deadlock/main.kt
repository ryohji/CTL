package app.test_deadlock

import app.test_deadlock.Location.*
import app.test_deadlock.Mutex.Free
import app.test_deadlock.Mutex.Lock
import ctl.*
import thread.*

fun main() = (SharedVar(Free, Free) with listOf(Nil on P0, Nil on Q0)).let { initial ->
    val graph = buildGraph(listOf(
        Transition("P.lock 0", P0 to P1) { if (it.shared.m0 == Free) it.locked0() else null },
        Transition("P.lock 1", P1 to P2) { if (it.shared.m1 == Free) it.locked1() else null },
        Transition("P.free 1", P2 to P3) { it.freed1() },
        Transition("P.free 0", P3 to P0) { it.freed0() },
        Transition("Q.lock 1", Q0 to Q1) { if (it.shared.m1 == Free) it.locked1() else null },
        Transition("Q.lock 0", Q1 to Q2) { if (it.shared.m0 == Free) it.locked0() else null },
        Transition("Q.free 0", Q2 to Q3) { it.freed0() },
        Transition("Q.free 1", Q3 to Q0) { it.freed1() }
    ), initial)
    // IF THERE IS ANY NEXT TRANSITION STATE, EX true is true. So `not EX true` detects deadlock.
    val label = graph.mark(not (EX(True)))
    println(graph.toDot(initial, label))
}

private enum class Location : thread.Location { P0, P1, P2, P3, Q0, Q1, Q2, Q3 }

private enum class Mutex { Free, Lock }

private data class SharedVar(val m0: Mutex, val m1: Mutex) {
    override fun toString() = "($m0, $m1)"
}

private object Nil {
    override fun toString() = "()"
}

private fun Transition.Variable<SharedVar, Nil>.locked0() = Transition.Variable(SharedVar(Lock, shared.m1), Nil)
private fun Transition.Variable<SharedVar, Nil>.freed0() = Transition.Variable(SharedVar(Free, shared.m1), Nil)
private fun Transition.Variable<SharedVar, Nil>.locked1() = Transition.Variable(SharedVar(shared.m0, Lock), Nil)
private fun Transition.Variable<SharedVar, Nil>.freed1() = Transition.Variable(SharedVar(shared.m0, Free), Nil)
