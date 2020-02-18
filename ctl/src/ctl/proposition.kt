package ctl

import graph.*

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
    override fun toString() = "True"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> = listOf()
}

object False : R0 {
    override fun toString() = "False"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> = listOf()
}

data class Match(val inspection: String) : R0 {
    override fun toString() = inspection
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        inspections.find { inspection == it.name }
            ?.let { inspection -> g.node.filter { inspection.matches(it) } } ?: listOf()
}

data class Not(override val proposition: Proposition) : R1 {
    override fun toString() = "not($proposition)"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        // 否定する述語 `proposition` でラベルづけされていない状態を抽出
        labels.labeled(proposition).let { ps -> g.node.filterNot { it in ps } }
}

data class And(override val l: Proposition, override val r: Proposition) : R2 {
    override fun toString() = "($l and $r)"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it in ls) and (it in rs) }
            }
        }
}

data class Or(override val l: Proposition, override val r: Proposition) : R2 {
    override fun toString() = "($l or $r)"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it in ls) or (it in rs) }
            }
        }
}

data class Imply(override val l: Proposition, override val r: Proposition) : R2 {
    override fun toString() = "($l -> $r)"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        labels.labeled(l).let { ls ->
            labels.labeled(r).let { rs ->
                g.node.filter { (it !in ls) or (it in rs) }
            }
        }
}

data class EX(override val proposition: Proposition) : R1 {
    override fun toString() = "EX $proposition"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> =
        labels.labeled(proposition).let { ns ->
            g.link.filter { it.boundTo in ns }.map { it.node }
        }
}

data class EU(override val l: Proposition, override val r: Proposition) : R2 {
    override fun toString() = "E $l U $r"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> {
        val edgesFromL = g.link.filter { it.node in labels.labeled(l) }
        fun lsBoundFor(nodes: List<S>) = edgesFromL.filter { it.boundTo in nodes }.map { it.node }
        fun loop(nodes: List<S>): List<S> = lsBoundFor(nodes).minus(nodes).let {
            if (it.isEmpty()) nodes else loop(nodes + it)
        }
        // r である状態と、これに連なる一連の l の状態を列挙する
        return loop(g.node.filter { it in labels.labeled(r) })
    }
}

data class EG(override val proposition: Proposition) : R1 {
    override fun toString() = "EG $proposition"
    override fun <S> filter(g: Graph<S>, inspections: List<Inspection<S>>, labels: List<Label<S>>): List<S> {
        fun nextTo(node: S): List<S> = g.link.filter { node == it.node }.map { it.boundTo }
        fun outOfLinkFrom(nodes: List<S>) = nodes.filter { nextTo(it).intersect(nodes).isEmpty() }
        fun loop(nodes: List<S>): List<S> = outOfLinkFrom(nodes).let {
            if (it.isEmpty()) nodes else loop(nodes - it)
        }
        // （リンク先に proposition がない状態を順次取り除いていくことで）
        // proposition からなる閉路と、これに連なる一連の状態を列挙する
        return loop(g.node.filter { it in labels.labeled(proposition) })
    }
}

private val <S> Graph<S>.node get() = map { it.node }
private val <S> Graph<S>.link get() = filterIsInstance<Link<S>>()

interface Proposition {
    val subProposition: List<Proposition>

    val expanded: List<Proposition>
        get() = expand(listOf())

    fun <State> filter(g: Graph<State>, inspections: List<Inspection<State>>, labels: List<Label<State>>): List<State>
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

private fun <State> List<Label<State>>.labeled(proposition: Proposition): Set<State> =
    filter { proposition == it.proposition }.map { it.state }.toSet()
