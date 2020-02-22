package app.test_liveness

import ctl.*
import thread.*

fun main() = (SharedVar(0, listOf()) with listOf(LocalVar on "P0", LocalVar on "Q0", LocalVar on "R0")).let { initial ->
    val sched = ::sched0_queue
    fun mkTrans(name: String, id: Id): List<Transition<SharedVar, LocalVar>> =
        listOf(
            Transition("${name}.req", L("${name}0") to L("${name}1"))
            { it.shared.schedWith(id, sched) with LocalVar },
            Transition("${name}.lock", L("${name}1") to L("${name}2"))
            { if (it.shared.m == 0 && it.shared.w.firstOrNull() == id) it.shared.exec() with LocalVar else null },
            Transition("${name}.unlock", L("${name}2") to L("${name}0"))
            { it.shared.done() with LocalVar }
        )

    val graph = buildGraph(mkTrans("P", 1) + mkTrans("Q", 2) + mkTrans("R", 3), initial)
    val proposition = not(EU(True, "req" and EG(not("acq"))))
    val label = graph.mark(proposition where listOf(
        "req" denote { 3 in it.shared.w },
        "acq" denote { 3 == it.shared.m }
    ))
    val targetStates = label.filter { it.proposition == proposition }.map { it.state }
    println(graph.toDot(initial, label) { state -> state in targetStates })
}

private data class L(val name: String) : Location {
    override fun toString() = name
}

private typealias Id = Int

private data class SharedVar(val m: Id, val w: List<Id>) {
    override fun toString() = "m=$m w=$w"
}

private fun SharedVar.schedWith(id: Id, sched: (List<Id>, Id) -> List<Id>) = SharedVar(m, sched(w, id))
private fun SharedVar.exec() = SharedVar(w.first(), w.drop(1))
private fun SharedVar.done() = SharedVar(0, w)

private object LocalVar {
    override fun toString() = "()"
}

private infix fun LocalVar.on(location: String) = LocalVar on L(location)

private fun sched0_queue(w: List<Id>, id: Id) = w + id
private fun sched1_stack(w: List<Id>, id: Id) = listOf(id) + w
private fun sched2_priority(w: List<Id>, id: Id) = (w + id).sorted()
private fun sched3_insert(w: List<Id>, id: Id) = w.take(1) + id + w.drop(1)
