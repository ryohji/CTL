package graph

interface Edge<Node> {
    val node: Node
}

data class Node<Node>(override val node: Node) : Edge<Node>

data class Link<Node>(override val node: Node, val boundTo: Node, val name: String) : Edge<Node>

typealias Graph<Node> = Set<Edge<Node>>
