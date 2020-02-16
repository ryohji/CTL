import Location.*
import ctl.*
import thread.*

fun main() = buildGraph(trans, Variable(0, 0, 0) with listOf(object : LocalVar {} on P0))
    .joinToString("\n")
    .let { println(it) }

enum class Location : thread.Location { P0, P1, P2, P3, P4 }

data class Variable(val x: Int, val y: Int, val z: Int) : SharedVar

private val trans = listOf(
    Transition("x=1", P0 to P1) { it.shared.xAltered(1) with it.local },
    Transition("y=1", P1 to P2) { it.shared.yAltered(1) with it.local },
    Transition("z=1", P2 to P3) { it.shared.zAltered(1) with it.local },
    Transition("y=0", P3 to P4) { it.shared.yAltered(0) with it.local }
)

private fun SharedVar.xAltered(x: Int) = with(this as Variable) { Variable(x, y, z) }
private fun SharedVar.yAltered(y: Int) = with(this as Variable) { Variable(x, y, z) }
private fun SharedVar.zAltered(z: Int) = with(this as Variable) { Variable(x, y, z) }
