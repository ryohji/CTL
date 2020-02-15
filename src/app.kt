import Location.*
import ctl.*
import thread.*

fun main() = buildGraph(rules, thread.State(Shared(0, 0, 0), listOf(P0.toThreadLocal())))
    .joinToString("\n")
    .let { println(it) }

enum class Location : thread.Location { P0, P1, P2, P3, P4 }

data class Shared(val x: Int, val y: Int, val z: Int) : thread.Shared

fun Location.toThreadLocal() = this.let { location ->
    object : ThreadLocal {
        override fun toString() = location.name
        override val ir = location
    }
}

fun Shared.xAltered(x: Int) = Shared(x, y, z)
fun Shared.yAltered(y: Int) = Shared(x, y, z)
fun Shared.zAltered(z: Int) = Shared(x, y, z)
fun Thread.alter(location: Location, convert: (Shared) -> Shared): Thread =
    Thread(convert(shared as Shared), location.toThreadLocal())

private val rules = listOf(
    object : Rule {
        override val name = "x=1"
        override fun applicable(t: Thread) = t.local.ir == P0
        override fun convert(t: Thread) = t.alter(P1) { it.xAltered(1) }
    },
    object : Rule {
        override val name = "y=1"
        override fun applicable(t: Thread) = t.local.ir == P1
        override fun convert(t: Thread) =  t.alter(P2) { it.yAltered(1) }
    },
    object : Rule {
        override val name = "z=1"
        override fun applicable(t: Thread) = t.local.ir == P2
        override fun convert(t: Thread) = t.alter(P3) { it.zAltered(1) }
    },
    object : Rule {
        override val name = "y=0"
        override fun applicable(t: Thread) = t.local.ir == P3
        override fun convert(t: Thread) =  t.alter(P4) { it.yAltered(0) }
    }
)
