package ctl

interface State

interface Graph {
    data class Edge(val from: State, val to: State)

    val node: List<State>
    val edge: List<Edge>
}

data class Label(val state: State, val proposition: Proposition)

class Inspection(val name: String, val matches: (State) -> Boolean)

fun mark(proposition: Proposition, g: Graph, inspections: List<Inspection>): List<Label> =
    mutableListOf<Label>().also { labels ->
        proposition.expanded.forEach { prop ->
            labels += prop.filter(g, inspections, labels).map { Label(it, prop) }
        }
    }