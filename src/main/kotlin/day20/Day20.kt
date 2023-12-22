package day20

import AocPuzzle
import leastCommonMultiple

fun main() = Day20.runAll()

object Day20 : AocPuzzle<Long, Long>() {

    override fun solve1(input: List<String>): Long {
        val (broadcaster, _) = parseModules(input)
        Module.resetPulseCounts()
        for (i in 0 ..< 1000) {
            broadcaster.buttonPress(Pulse.LOW)
        }
        return Module.lowPulseCount * Module.highPulseCount
    }

    override fun solve2(input: List<String>): Long {
        val (broadcaster, modules) = parseModules(input)
        val output = (modules["rx"] ?: modules["output"]) as? Output ?: return 0

        // output has a single conjunction predecessor, who acts as an inverter -> all inputs need to be high
        val inverter = output.inputs.keys.first() as Conjunction

        Module.resetPulseCounts()
        while (!inverter.isCycleComplete) {
            broadcaster.buttonPress(Pulse.LOW)
        }
        return leastCommonMultiple(inverter.inputHighCycles.values)
    }
}

fun parseModules(input: List<String>): Pair<Broadcaster, Map<String, Module>> {
    var outputModule: Module? = null
    val modules = input
        .map { Module(it) }
        .associateBy { it.name }
        .toMutableMap()
        .also { modsByName ->
            modsByName.values.forEach { mod ->
                val outputs = mod.outputNames.map { modsByName[it] ?: Output(it) }
                mod.registerOutputs(outputs)

                outputs.find { it is Output }?.let { outputModule = it }
            }
        }

    // output module is only implicitly defined in input -> add it to the module map if it exists
    outputModule?.let { modules[it.name] = it }

    val broadcaster = modules["broadcaster"] as Broadcaster
    return broadcaster to modules
}

fun Module(def: String): Module {
    val (name, outputs) = def.split(" -> ")
    val outputNames = outputs.split(", ")

    return when(name[0]) {
        'b' -> Broadcaster(name, outputNames)
        '%' -> FlipFlop(name.drop(1), outputNames)
        '&' -> Conjunction(name.drop(1), outputNames)
        else -> error(name)
    }
}

sealed class Module(val name: String, val outputNames: List<String>) {
    private val outputs = mutableListOf<Module>()

    val inputs = mutableMapOf<Module, ArrayDeque<Pulse>>()

    fun registerOutputs(outs: List<Module>) {
        outputs += outs
        outs.forEach { it.registerInput(this) }
    }

    protected open fun registerInput(source: Module) {
        inputs[source] = ArrayDeque()
    }

    abstract fun process()

    fun send(pulse: Pulse) {
        outputs.forEach {
            it.inputs[this]!!.add(pulse)
            incrementPulseCount(pulse)
        }
        processOutputs(outputs)
    }

    companion object {
        private val processQueue = ArrayDeque<Module>()

        var lowPulseCount = 0L
        var highPulseCount = 0L
        var buttonCounter = 0

        fun incrementPulseCount(pulse: Pulse) {
            when (pulse) {
                Pulse.LOW -> lowPulseCount++
                Pulse.HIGH -> highPulseCount++
            }
        }

        fun resetPulseCounts() {
            lowPulseCount = 0L
            highPulseCount = 0L
            buttonCounter = 0
        }

        private fun processOutputs(outputs: List<Module>) {
            processQueue += outputs
            while (processQueue.isNotEmpty()) {
                processQueue.removeFirst().process()
            }
        }
    }
}

class FlipFlop(name: String, outputNames: List<String>) : Module(name, outputNames) {
    var state = false

    override fun process() {
        var sendFlag = false
        inputs.values.forEach { q ->
            if (q.isNotEmpty() && q.removeFirst() == Pulse.LOW) {
                state = !state
                sendFlag = true
            }
        }
        if (sendFlag) {
            send(if (state) Pulse.HIGH else Pulse.LOW)
        }
    }
}

class Conjunction(name: String, outputNames: List<String>) : Module(name, outputNames) {
    val memory = mutableMapOf<Module, Pulse>()
    val inputHighCycles = mutableMapOf<Module, Int>()

    val isCycleComplete: Boolean
        get() = inputHighCycles.size == memory.size

    override fun registerInput(source: Module) {
        super.registerInput(source)
        memory[source] = Pulse.LOW
    }

    override fun process() {
        inputs.forEach { (input, q) ->
            if (q.isNotEmpty()) {
                val inState = q.removeFirst()
                memory[input] = inState
                if (inState == Pulse.HIGH && input !in inputHighCycles) {
                    inputHighCycles[input] = buttonCounter
                }
            }
        }
        send(if (memory.values.all { it == Pulse.HIGH }) Pulse.LOW else Pulse.HIGH)
    }
}

class Broadcaster(name: String, outputNames: List<String>) : Module(name, outputNames) {
    override fun process() = error("supposed to call buttonPress(LOW) on Broadcaster")

    fun buttonPress(pulse: Pulse) {
        buttonCounter++
        incrementPulseCount(pulse)
        send(pulse)
    }
}

class Output(name: String) : Module(name, emptyList()) {
    override fun process() {
        inputs.values.forEach { it.clear() }
    }
}

enum class Pulse {
    LOW,
    HIGH
}
