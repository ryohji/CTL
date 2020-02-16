package ctl

fun not(it: Proposition) = Not(it)
fun not(proposition: String) = Not(Match(proposition))

infix fun Proposition.and(that: Proposition) = And(this, that)
infix fun Proposition.and(that: String) = this and Match(that)
infix fun String.and(that: Proposition) = Match(this) and that
infix fun String.and(that: String) = Match(this) and Match(that)

infix fun Proposition.or(that: Proposition) = Or(this, that)
infix fun Proposition.or(that: String) = this or Match(that)
infix fun String.or(that: Proposition) = Match(this) or that
infix fun String.or(that: String) = Match(this) or Match(that)

infix fun Proposition.imply(that: Proposition) = Imply(this, that)
infix fun Proposition.imply(that: String) = this imply Match(that)
infix fun String.imply(that: Proposition) = Match(this) imply that
infix fun String.imply(that: String) = Match(this) imply Match(that)

object True : R0 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> = listOf()
}

object False : R0 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> = listOf()
}

data class Match(val inspection: String) : R0 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        inspections.find { inspection == it.name }
            ?.let { inspection -> g.node.filter { inspection.matches(it) } } ?: listOf()
}

data class Not(override val proposition: Proposition) : R1 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        // 否定する述語 `proposition` でラベルづけされていない状態を抽出
        labels.labeled(proposition).let { ps -> g.node.filterNot { it in ps } }
}

data class And(override val l: Proposition, override val r: Proposition) : R2 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it in ls) and (it in rs) }
            }
        }
}

data class Or(override val l: Proposition, override val r: Proposition) : R2 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it in ls) or (it in rs) }
            }
        }
}

data class Imply(override val l: Proposition, override val r: Proposition) : R2 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it !in ls) or (it in rs) }
            }
        }
}

data class EX(override val proposition: Proposition) : R1 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> =
        labels.labeled(proposition).let { ns ->
            g.edge.filter { it.to in ns }.map { it.from }
        }
}

data class EU(override val l: Proposition, override val r: Proposition) : R2 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> {
        val edgesFromL = g.edge.filter { it.from in labels.labeled(l) }
        fun lsBoundFor(nodes: List<State>) = edgesFromL.filter { it.to in nodes }.map { it.from }
        fun loop(nodes: List<State>): List<State> = lsBoundFor(nodes).minus(nodes).let {
            if (it.isEmpty()) nodes else loop(nodes + it)
        }
        // r である状態と、これに連なる一連の l の状態を列挙する
        return loop(g.node.filter { it in labels.labeled(r) })
    }
}

data class EG(override val proposition: Proposition) : R1 {
    override fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State> {
        fun nextTo(node: State): List<State> = g.edge.filter { node == it.from }.map { it.to }
        fun outOfLinkFrom(nodes: List<State>) = nodes.filter { nextTo(it).intersect(nodes).isEmpty() }
        fun loop(nodes: List<State>): List<State> = outOfLinkFrom(nodes).let {
            if (it.isEmpty()) nodes else loop(nodes - it)
        }
        // （リンク先に proposition がない状態を順次取り除いていくことで）
        // proposition からなる閉路と、これに連なる一連の状態を列挙する
        return loop(g.node.filter { it in labels.labeled(proposition) })
    }
}

interface Proposition {
    val subProposition: List<Proposition>

    val expanded: List<Proposition>
        get() = expand(listOf())

    fun filter(g: Graph, inspections: List<Inspection>, labels: List<Label>): List<State>
}

private fun Proposition.expand(expanded: List<Proposition>): List<Proposition> =
    if (this in expanded)
        expanded
    else
        subProposition.fold(expanded) { ls, l -> l.expand(ls) } + this

private interface R0 : Proposition {
    override val subProposition: List<Proposition>
        get() = listOf()
}

private interface R1 : Proposition {
    val proposition: Proposition
    override val subProposition: List<Proposition>
        get() = listOf(proposition)
}

private interface R2 : Proposition {
    val l: Proposition
    val r: Proposition
    override val subProposition: List<Proposition>
        get() = listOf(l, r)
}

private fun List<Label>.labeled(proposition: Proposition): Set<State> =
    filter { proposition == it.proposition }.map { it.state }.toSet()
