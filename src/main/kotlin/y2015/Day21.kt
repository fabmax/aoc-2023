package y2015

import AocPuzzle
import extractNumbers

fun main() = Day21.runAll()

object Day21 : AocPuzzle<Int, Int>() {
    val weapons = listOf(
        Weapon(8, 4),
        Weapon(10, 5),
        Weapon(25, 6),
        Weapon(40, 7),
        Weapon(74, 8),
    )
    val armors = listOf(
        null,
        Armor(13, 1),
        Armor(31, 2),
        Armor(53, 3),
        Armor(75, 4),
        Armor(102, 5),
    )
    val rings = listOf(
        null, null,
        Ring(25, 1, 0),
        Ring(50, 2, 0),
        Ring(100, 3, 0),
        Ring(20, 0, 1),
        Ring(40, 0, 2),
        Ring(80, 0, 3),
    )

    override fun solve1(input: List<String>): Int {
        val (hitPoints, damage, armor) = input.joinToString().extractNumbers()
        val boss = Character(hitPoints, damage, armor)

        return weapons
            .flatMap { weapon -> armors.map { weapon to it } }
            .flatMap { (weapon, armor) ->
                (rings.indices)
                    .flatMap { ia -> (rings.indices).map { ia to it } }
                    .filter { (ia, ib) -> ia != ib }
                    .map { (ia, ib) -> Inventory(weapon, armor, rings[ia], rings[ib]) }
            }
            .filter { fight(it.makeCharacter(100), boss) }
            .minOf { it.cost }
    }

    override fun solve2(input: List<String>): Int {
        val (hitPoints, damage, armor) = input.joinToString().extractNumbers()
        val boss = Character(hitPoints, damage, armor)

        return weapons
            .flatMap { weapon -> armors.map { weapon to it } }
            .flatMap { (weapon, armor) ->
                (rings.indices)
                    .flatMap { ia -> (rings.indices).map { ia to it } }
                    .filter { (ia, ib) -> ia != ib }
                    .map { (ia, ib) -> Inventory(weapon, armor, rings[ia], rings[ib]) }
            }
            .filter { !fight(it.makeCharacter(100), boss) }
            .maxOf { it.cost }
    }

    fun fight(player: Character, boss: Character): Boolean {
        var playerHp = player.hitPoints
        var bossHp = boss.hitPoints

        while (playerHp > 0 && bossHp > 0) {
            bossHp -= (player.damage - boss.armor).coerceAtLeast(1)
            if (bossHp > 0) {
                playerHp -= (boss.damage - player.armor).coerceAtLeast(1)
            }
        }
        return playerHp > 0
    }

    data class Character(val hitPoints: Int, val damage: Int, val armor: Int)

    data class Weapon(val cost: Int, val damage: Int)
    data class Armor(val cost: Int, val armor: Int)
    data class Ring(val cost: Int, val damage: Int, val armor: Int)
    data class Inventory(val weapon: Weapon, val armor: Armor?, val ringA: Ring?, val ringB: Ring?) {
        val totalDamage: Int get() = weapon.damage + (ringA?.damage ?: 0) + (ringB?.damage ?: 0)
        val totalArmor: Int get() = (armor?.armor ?: 0) + (ringA?.armor ?: 0) + (ringB?.armor ?: 0)
        val cost: Int get() = weapon.cost + (armor?.cost ?: 0) + (ringA?.cost ?: 0) + (ringB?.cost ?: 0)

        fun makeCharacter(hitPoints: Int) = Character(hitPoints, totalDamage, totalArmor)
    }
}