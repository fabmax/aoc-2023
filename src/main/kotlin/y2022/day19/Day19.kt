package y2022.day19

import AocPuzzle
import extractNumbers

fun main() = Day19.runAll()

object Day19 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input.map { Blueprint(it) }.sumOf { blueprint ->
            val state = FactoryState(1, 0, 0, 0, 0, 0, 0, 0)
            val crackedGeodes = state.maximizeGeodes(24, blueprint, 15)
            blueprint.id * crackedGeodes
        }
    }

    override fun solve2(input: List<String>): Int {
        return input.map { Blueprint(it) }.take(3).fold(1) { acc, blueprint ->
            val state = FactoryState(1, 0, 0, 0, 0, 0, 0, 0)
            val crackedGeodes = state.maximizeGeodes(32, blueprint, 15)
            acc * crackedGeodes
        }
    }

    fun FactoryState.maximizeGeodes(timeRemaining: Int, blueprint: Blueprint, waitForMask: Int): Int {
        if (timeRemaining == 0) {
            return crackedGeodes
        }
        val state = this

        val canBuildOreRobot = blueprint.canBuildOreRobot(this) && waitForMask and Robot.ORE.bit != 0
        val canBuildClayRobot = blueprint.canBuildClayRobot(this) && waitForMask and Robot.CLAY.bit != 0
        val canBuildObsidianRobot = blueprint.canBuildObsidianRobot(this) && waitForMask and Robot.OBSIDIAN.bit != 0
        val canBuildGeodeRobot = blueprint.canBuildGeodeRobot(this)

        val nextTimeRemaining = timeRemaining - 1
        val nextStates = buildList {
            if (canBuildGeodeRobot) {
                add(Robot.GEODE to 15)

            } else {
                var newWaitMask = waitForMask
                if (canBuildObsidianRobot && state.numObsidianRobots < blueprint.maxObsidianCosts && timeRemaining > 1) {
                    add(Robot.OBSIDIAN to 15)
                    newWaitMask = newWaitMask xor Robot.OBSIDIAN.bit
                }
                if (canBuildOreRobot && state.numOreRobots < blueprint.maxOreCosts && timeRemaining > 2) {
                    add(Robot.ORE to 15)
                    newWaitMask = newWaitMask xor Robot.ORE.bit
                }
                if (canBuildClayRobot && state.numClayRobots < blueprint.maxClayCosts && timeRemaining > 2) {
                    add(Robot.CLAY to 15)
                    newWaitMask = newWaitMask xor Robot.CLAY.bit
                }
                add(null to newWaitMask)
            }
        }

        return nextStates.maxOf { (robot, waitMask) ->
            val nextState = when (robot) {
                Robot.ORE -> blueprint.produce(state, buildOreRobots = 1)
                Robot.CLAY -> blueprint.produce(state, buildClayRobots = 1)
                Robot.OBSIDIAN -> blueprint.produce(state, buildObsidianRobots = 1)
                Robot.GEODE -> blueprint.produce(state, buildGeodeRobots = 1)
                null -> blueprint.produce(state)
            }
            nextState.maximizeGeodes(nextTimeRemaining, blueprint, waitMask)
        }
    }

    fun Blueprint(line: String): Blueprint {
        val numbers = line.extractNumbers()
        return Blueprint(
            numbers[0],
            numbers[1],
            numbers[2],
            numbers[3],
            numbers[4],
            numbers[5],
            numbers[6],
        )
    }

    enum class Robot(val bit: Int) {
        ORE(1),
        CLAY(2),
        OBSIDIAN(4),
        GEODE(8)
    }

    data class FactoryState(
        val numOreRobots: Int,
        val numClayRobots: Int,
        val numObsidianRobots: Int,
        val numGeodeRobots: Int,

        val ore: Int,
        val clay: Int,
        val obsidian: Int,
        val crackedGeodes: Int
    )

    data class Blueprint(
        val id: Int,
        val oreRobotOre: Int,
        val clayRobotOre: Int,
        val obsidianRobotOre: Int,
        val obsidianRobotClay: Int,
        val geodeRobotOre: Int,
        val geodeRobotObsidian: Int
    ) {
        val maxOreCosts = maxOf(oreRobotOre, clayRobotOre, obsidianRobotOre, geodeRobotOre)
        val maxClayCosts = obsidianRobotClay
        val maxObsidianCosts = geodeRobotObsidian

        fun canBuildOreRobot(state: FactoryState) = state.ore >= oreRobotOre
        fun canBuildClayRobot(state: FactoryState) = state.ore >= clayRobotOre
        fun canBuildObsidianRobot(state: FactoryState) = state.ore >= obsidianRobotOre && state.clay >= obsidianRobotClay
        fun canBuildGeodeRobot(state: FactoryState) = state.ore >= geodeRobotOre && state.obsidian >= geodeRobotObsidian

        fun produce(
            state: FactoryState,
            buildOreRobots: Int = 0,
            buildClayRobots: Int = 0,
            buildObsidianRobots: Int = 0,
            buildGeodeRobots: Int = 0
        ): FactoryState {
            val oreAfterBuilding = state.ore -
                    buildOreRobots * oreRobotOre -
                    buildClayRobots * clayRobotOre -
                    buildObsidianRobots * obsidianRobotOre -
                    buildGeodeRobots * geodeRobotOre
            val clayAfterBuilding = state.clay - buildObsidianRobots * obsidianRobotClay
            val obsidianAfterBuilding = state.obsidian - buildGeodeRobots * geodeRobotObsidian

            check(oreAfterBuilding >= 0 && clayAfterBuilding >= 0 && obsidianAfterBuilding >= 0) {
                "available resources exceeded"
            }

            return FactoryState(
                numOreRobots = state.numOreRobots + buildOreRobots,
                numClayRobots = state.numClayRobots + buildClayRobots,
                numObsidianRobots = state.numObsidianRobots + buildObsidianRobots,
                numGeodeRobots = state.numGeodeRobots + buildGeodeRobots,
                ore = oreAfterBuilding + state.numOreRobots,
                clay = clayAfterBuilding + state.numClayRobots,
                obsidian = obsidianAfterBuilding + state.numObsidianRobots,
                crackedGeodes = state.crackedGeodes + state.numGeodeRobots
            )
        }
    }
}