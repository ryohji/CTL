import Location.*
import ctl.*
import graph.Edge
import graph.Graph
import graph.Link
import thread.Transition
import thread.buildGraph
import thread.on
import thread.with

fun main() = (SharedVar(0, 0, 0) with listOf(Nil on P0)).let { initial ->
    buildGraph(trans, initial)
        .also { println(buildDot(it, initial)) }
        .mark(("x=1" and "y>0") or not("z=0") where listOf(
            "x=1" denote { it.shared.x == 1 },
            "y>0" denote { it.shared.y > 0 },
            "z=0" denote { it.shared.z == 0 }
        ))
        .joinToString("\n")
        .let { println(it) }
}

enum class Location : thread.Location { P0, P1, P2, P3, P4 }

data class SharedVar(val x: Int, val y: Int, val z: Int) {
    override fun toString() = "(x=$x y=$y z=$z)"
}

object Nil {
    override fun toString() = "()"
}

private val trans = listOf<Transition<SharedVar, Nil>>(
    Transition("x:= 1", P0 to P1) { it.shared.xAltered(1) with it.local },
    Transition("y:= 1", P1 to P2) { it.shared.yAltered(1) with it.local },
    Transition("z:= 1", P2 to P3) { it.shared.zAltered(1) with it.local },
    Transition("y:= 0", P3 to P4) { it.shared.yAltered(0) with it.local }
)

private fun SharedVar.xAltered(x: Int) = SharedVar(x, y, z)
private fun SharedVar.yAltered(y: Int) = SharedVar(x, y, z)
private fun SharedVar.zAltered(z: Int) = SharedVar(x, y, z)

fun <Node> buildDot(graph: Graph<Node>, initial: Node): String {
    val states = graph.map(Edge<Node>::node).toSet()
    val nodes = states.mapIndexed { index, node ->
        // TODO: "filled" でラベルつきノードを描画
        "$index [label=\"$node\" style=solid]"
    }.plus("s [label=start shape=none]").joinToString(";\n  ")
    val edges = graph.filterIsInstance<Link<Node>>().map {
        val start = states.indexOf(it.node)
        val end = states.indexOf(it.boundTo)
        "$start -> $end [label=\"${it.name}\"]"
    }.plus("s -> ${states.indexOf(initial)}").joinToString(";\n  ")
    return "digraph {\n  $nodes;\n  $edges;\n}"
}
