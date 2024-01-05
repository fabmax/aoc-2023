package y2015

import AocPuzzle

fun main() = Day07.runAll()

object Day07 : AocPuzzle<UShort, UShort>() {
    override fun solve1(input: List<String>): UShort {
        val gates = input.map { Gate(it) }.associateBy { it.name }
        gates.computeAllStates()
        return gates.getStateOf(if (isTestRun()) "i" else "a")
    }

    override fun solve2(input: List<String>): UShort {
        var gates = input.map { Gate(it) }.associateBy { it.name }.toMutableMap()
        gates.computeAllStates()
        val aState = gates.getStateOf("a")

        gates = input.map { Gate(it) }.associateBy { it.name }.toMutableMap()
        gates["b"] = Provider("b", aState)
        gates.computeAllStates()
        return gates.getStateOf("a")
    }

    fun Gate(txt: String): Gate {
        val (def, name) = txt.split(" -> ")
        return when {
            "AND" in def -> And(name, def.substringBefore(" AND"), def.substringAfter("AND "))
            "OR" in def -> Or(name, def.substringBefore(" OR"), def.substringAfter("OR "))
            "LSHIFT" in def -> LShift(name, def.substringBefore(" LSHIFT"), def.substringAfter("LSHIFT ").toInt())
            "RSHIFT" in def -> RShift(name, def.substringBefore(" RSHIFT"), def.substringAfter("RSHIFT ").toInt())
            "NOT" in def -> Not(name, def.substringAfter("NOT "))
            def.toUShortOrNull() != null -> Provider(name, def.toUShort())
            else -> Bridge(name, def)
        }
    }

    fun Map<String, Gate>.computeAllStates() {
        val open = values.toMutableSet()
        while (open.isNotEmpty()) {
            val next = open.first { it.canCompute(this) }
            open -= next
            next.computeState(this)
        }
    }

    fun Map<String, Gate>.getStateOf(name: String): UShort {
        val value = name.toUShortOrNull()
        if (value != null) {
            return value
        }
        return checkNotNull(get(name)?.state)
    }

    sealed class Gate(val dependencies: List<String>) {
        abstract val name: String

        var state: UShort? = null
        val isComputed: Boolean
            get() = state != null

        fun canCompute(gates: Map<String, Gate>): Boolean = dependencies.all { gates[it]?.isComputed != false }

        fun computeState(gates: Map<String, Gate>): UShort {
            return state ?: compute(gates).also { state = it }
        }

        protected abstract fun compute(gates: Map<String, Gate>): UShort
    }

    data class Bridge(override val name: String, val x: String) : Gate(listOf(x)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(x)
    }

    data class Provider(override val name: String, val value: UShort) : Gate(emptyList()) {
        override fun compute(gates: Map<String, Gate>): UShort = value
    }

    data class And(override val name: String, val lt: String, val rt: String) : Gate(listOf(lt, rt)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(lt) and gates.getStateOf(rt)
    }

    data class Or(override val name: String, val lt: String, val rt: String) : Gate(listOf(lt, rt)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(lt) or gates.getStateOf(rt)
    }

    data class LShift(override val name: String, val x: String, val places: Int) : Gate(listOf(x)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(x) shl places
    }

    data class RShift(override val name: String, val x: String, val places: Int) : Gate(listOf(x)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(x) shr places
    }

    data class Not(override val name: String, val x: String) : Gate(listOf(x)) {
        override fun compute(gates: Map<String, Gate>): UShort = gates.getStateOf(x).inv()
    }

    infix fun UShort.shl(bitCount: Int): UShort = (toInt() shl bitCount).toUShort()
    infix fun UShort.shr(bitCount: Int): UShort = (toInt() shr bitCount).toUShort()
}