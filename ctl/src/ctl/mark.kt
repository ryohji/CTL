package ctl

import graph.Graph

data class Label<State>(val state: State, val proposition: Proposition)

class Inspection<State>(val name: String, val matches: (State) -> Boolean)
infix fun <State> String.denote(predicate: (State) -> Boolean) = Inspection(this, predicate)

fun <State> Graph<State>.mark(proposition: Proposition): Set<Label<State>> = mark(proposition to listOf())

fun <State> Graph<State>.mark(proposition: Pair<Proposition, List<Inspection<State>>>): Set<Label<State>> =
    proposition.let { (expression, inspections) ->
        mutableSetOf<Label<State>>().also { labels ->
            expression.expanded.forEach { proposition ->
                labels += proposition.filter(this, inspections, labels).map { Label(it, proposition) }
            }
        }
    }

infix fun <State> Proposition.where(inspections: List<Inspection<State>>) = Pair(this, inspections)
