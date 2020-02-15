package thread

interface Shared

interface ThreadLocal {
    val ir: Location
}

interface Location

data class Thread(val shared: Shared, val local: ThreadLocal)

data class State(val shared: Shared, val locals: List<ThreadLocal>)

data class Link(val name: String, val from: State, val to: State)

interface Rule {
    val name: String
    fun applicable(t: Thread): Boolean
    fun convert(t: Thread): Thread
}

fun buildGraph(rules: List<Rule>, initialState: State): Set<Link> = mutableSetOf<Link>().apply {
    val frontier = mutableSetOf(initialState)
    while (frontier.isNotEmpty()) {
        val from = frontier.first().also { frontier.remove(it) } // pop front
        val next = from.locals.mapIndexed { index, local ->
            rules.mapNotNull { it.apply(Thread(from.shared, local)) }.map { (name, thread) ->
                State(thread.shared, swapAt(index, from.locals, thread.local))
                    .also { this += Link(name, from, it) } // 次の状態を算出するとともにグラフにリンクを追加
            }
        }.flatten()
        this.map(Link::from).toSet().let { visited -> with(frontier) { addAll(next); removeAll(visited) } }
    }
}

private fun Rule.apply(t: Thread): Pair<String, Thread>? = if (applicable(t)) name to convert(t) else null

private fun <T> swapAt(n: Int, list: List<T>, item: T): List<T> = list.take(n) + item + list.drop(n + 1)
