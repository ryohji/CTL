package ctl

interface State

interface Graph {
    data class Edge(val from: State, val to: State)

    val node: List<State>
    val edge: List<Edge>
}

data class Label(val state: State, val proposition: Proposition)

class Inspection(val name: String, val matches: (State) -> Boolean)

fun mark(proposition: Pair<Proposition, List<Inspection>>, g: Graph): List<Label> = proposition.let { (expression, inspections) ->
    mutableListOf<Label>().also { labels ->
        expression.expanded.forEach { proposition ->
            labels += proposition.filter(g, inspections, labels).map { Label(it, proposition) }
        }
    }
}

infix fun Proposition.where(inspections: List<Inspection>) = Pair(this, inspections)
infix fun String.denote(predicate: (State) -> Boolean) = Inspection(this, predicate)
