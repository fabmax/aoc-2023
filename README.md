# Advent of code 2023 (in kotlin)

Solutions to all puzzles (so  far) of [Advent of code 2023](https://adventofcode.com/2023/)

## Results

So far, all [solutions](src/main/kotlin) work for both parts. There are a few notable days:

- **[Day 5:](src/main/kotlin/day05/Day05.kt) If You Give A Seed A Fertilizer**
  
  Part2 got pretty difficult. I took the lazy option and brute-forced it.
  The result for part 2 takes about 1 minute to compute.


- **[Day 10:](src/main/kotlin/day10/Day10.kt) Pipe Maze**

  Again difficult part2. I solved it using [PNPOLY](https://wrfranklin.org/Research/Short_Notes/pnpoly.html) a super
  handy (and short) point inside polygon check algorithm.


- **[Day 12:](src/main/kotlin/day12/Day12.kt) Hot Springs**

  I struggled a lot with part2. After reading a few tips and discussions on that puzzle, I solved it eventually using a
  [memoized](https://en.wikipedia.org/wiki/Memoization) recursion (what seems to be a common technique but was completely new to me).


- **Day 14: Parabolic Reflector Dish**

  This was a fun one! For part 1 I have an [alternative solution](src/main/kotlin/day14/Day14Kool.kt), which solves
  the puzzle using physics simulation including fancy 3D graphics. Also runs in the
  [browser](https://fabmax.github.io/kool/aoc23-day14/) I used [kool](https://github.com/fabmax/kool) for 
  that, my own kotlin 3D game engine :smile:

  For my [regular solution](src/main/kotlin/day14/Day14.kt) I went for speed instead of elegance: After a few
  warmup-iterations, the part 2 result completes in under 10 ms (Java 21, Ryzen 7950X, Ubuntu).


- **[Day 19:](src/main/kotlin/day19/Day19.kt) Aplenty**
  Another straight-forward part 1 followed by a pretty difficult part 2. After struggling a bit with range bounds
  it worked out ok. Just for fun, I also implemented two rather useless brute force approaches: On
  [CPU](src/main/kotlin/day19/Day19BruteForce.kt) (estimated time to complete: 2.6 days at 1.13G part checks / second,
  Ryzen 7950X) as well as on [GPU](src/main/kotlin/day19/Day19Compute.kt) (estimated time to complete: 1h:34m at 45.1G part checks / second, RTX4080, Windows).

## Running the Puzzles

In order to run the puzzles, you need to place your puzzle input into correctly named `.txt` files in the `inputs/` directory:
The implementation expects the day's puzzle input in a file called `day[xx].txt` where `[xx]` has to be replaced by
the day number (e.g. `day01.txt` for day 1, `day10.txt` for day 10, etc.)

Moreover, test-input can be specified in separate `.txt` files in the same directory: `day01_test.txt` for day 1's
test input and so on. Multiple test inputs for the same day can be given by appending an extra number:
`day02_test1.txt`, `day02_test2.txt`, etc.

Test input files are expected to start with a single line containing the expected results (if already known):
```
test1=?; test2=?; part1=?; part2=?

[test input here]
```
Where the `?` can be replaced with the expected results for part 1 / part 2 (for test input and main puzzle). You can
also keep the `?` or remove the entire entry if the expected result is not yet known. Moreover, if the expected
test result is only specified for a single part, only that part is executed.

