package ctl

interface Graph<State> {
    data class Edge<State>(val from: State, val to: State)

    val node: List<State>
    val edge: List<Edge<State>>
}

data class Label<State>(val state: State, val proposition: Proposition)

class Inspection<State>(val name: String, val matches: (State) -> Boolean)

fun <State> mark(proposition: Pair<Proposition, List<Inspection<State>>>, g: Graph<State>): List<Label<State>> =
    proposition.let { (expression, inspections) ->
        mutableListOf<Label<State>>().also { labels ->
            expression.expanded.forEach { proposition ->
                labels += proposition.filter(g, inspections, labels).map { Label(it, proposition) }
            }
        }
    }

infix fun <State> Proposition.where(inspections: List<Inspection<State>>) = Pair(this, inspections)
infix fun <State> String.denote(predicate: (State) -> Boolean) = Inspection(this, predicate)
