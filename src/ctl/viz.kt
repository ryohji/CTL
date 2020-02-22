package ctl

import graph.Graph
import graph.Link

fun <Node> Graph<Node>.toDot(initial: Node, label: Collection<Label<Node>>) = toDot(initial, label) { false }

fun <Node> Graph<Node>.toDot(initial: Node, label: Collection<Label<Node>>, emphasize: (Node) -> Boolean): String {
    val states = map(graph.Edge<Node>::node).toSet()
    val nodes = states.mapIndexed { i, node -> "$i [label=\"$node\"${if (emphasize(node)) " style=filled,fillcolor=palegreen" else ""}]" }
    val edges = filterIsInstance<Link<Node>>().map {
        with(it) { "${states.indexOf(node)} -> ${states.indexOf(boundTo)} [label=\"$name\"]" }
    }
    val labels = states
        .mapIndexed { i, state ->
            // remove `True` from annotation (True is given for all states).
            i to label.filterNot { it.proposition == True }.filter { state == it.state }.map { it.proposition }
        }
        .filter { it.second.isNotEmpty() }
        .map { (i, props) -> "l$i [label=\"${props.joinToString(",\\n")}\"]" to "l$i -> $i" }
        .unzip()
    return "digraph {\n" +
            "  s [shape=point];\n" +
            "  s -> ${states.indexOf(initial)};\n" +
            "  ${nodes.joinToString(";\n  ")};\n" +
            "  ${edges.joinToString(";\n  ")};\n" +
            "  node [shape=none];\n" +
            "  ${labels.first.joinToString(";\n  ")};\n" +
            "  edge [arrowhead=none];\n" +
            "  ${labels.second.joinToString(";\n  ")};\n" +
            "}"
}
