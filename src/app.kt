import Location.*
import ctl.*
import thread.*

fun main() = buildGraph(trans, SharedVar(0, 0, 0) with listOf(object : LocalVar {} on P0))
    .let { graph ->
        mark(("x=1" and "y>0") or not("z=0") where listOf(
            "x=1" denote { it.shared.x == 1 },
            "y>0" denote { it.shared.y > 0 },
            "z=0" denote { it.shared.z == 0 }
        ), convert(graph))
    }
    .joinToString("\n")
    .let { println(it) }

enum class Location : thread.Location { P0, P1, P2, P3, P4 }

data class SharedVar(val x: Int, val y: Int, val z: Int)

private val trans = listOf<Transition<SharedVar>>(
    Transition("x=1", P0 to P1) { it.shared.xAltered(1) with it.local },
    Transition("y=1", P1 to P2) { it.shared.yAltered(1) with it.local },
    Transition("z=1", P2 to P3) { it.shared.zAltered(1) with it.local },
    Transition("y=0", P3 to P4) { it.shared.yAltered(0) with it.local }
)

private fun SharedVar.xAltered(x: Int) = SharedVar(x, y, z)
private fun SharedVar.yAltered(y: Int) = SharedVar(x, y, z)
private fun SharedVar.zAltered(z: Int) = SharedVar(x, y, z)

private fun convert(graph: Collection<Edge<SharedVar>>): Graph<SystemState<SharedVar>> = object : Graph<SystemState<SharedVar>> {
    override val node = graph.map { it.state }
    override val edge = graph.filterIsInstance<Link<SharedVar>>().map { Graph.Edge(it.state, it.boundTo) }
}
