data object Day22 : AdventDay() {
  override fun solve() {
    val data = reads<String>() ?: return
    val bricks = data.toBricks()

    val (stableBricks, _) = bricks.fallBricks()
    val causedMovementOfCount = stableBricks.map { toBeDisintegrated ->
      val (_, movedCount) = (stableBricks - toBeDisintegrated).fallBricks()
      movedCount
    }

    causedMovementOfCount.count { it == 0 }.printIt()
    causedMovementOfCount.sum().printIt()
  }
}

private fun List<String>.toBricks(): List<Brick> =
  map { line -> line.split('~').map { it.toVec3() }.let { (from, to) -> Brick(from, to) } }

private data class Brick(val blocks: Set<V3>) {

  val minZ: Int by lazy { blocks.minOf { it.z } }

  fun isStableOnAnyOf(blocks: Set<V3>): Boolean {
    for (block in this.blocks) {
      if (block.z == 1) return true
      if (block.moveDown() in blocks) return true
    }
    return false
  }

  fun moveDown(): Brick = Brick(blocks.map2Set { it.moveDown() })

  companion object {
    operator fun invoke(from: V3, to: V3): Brick = when {
      from == to -> setOf(to)
      from.y == to.y && from.z == to.z -> (from.x directedTo to.x).map2Set { V3(it, to.y, to.z) }
      from.z == to.z && from.x == to.x -> (from.y directedTo to.y).map2Set { V3(to.x, it, to.z) }
      from.x == to.x && from.y == to.y -> (from.z directedTo to.z).map2Set { V3(to.x, to.y, it) }
      else -> throw IllegalArgumentException("Cannot find blocks between $from and $to")
    }.let(::Brick)
  }
}

private fun String.toVec3(): V3 =
  split(',').let { (x, y, z) -> V3(x.toInt(), y.toInt(), z.toInt()) }

private data class V3(val x: Int, val y: Int, val z: Int) {
  fun moveDown(): V3 = copy(z = z - 1)
}

private fun List<Brick>.fallBricks(): Pair<List<Brick>, Int> {
  var movedCount = 0
  val fallen = buildList {
    val fallen = mutableSetOf<V3>()
    val sortedBricks = this@fallBricks.sortedBy { it.minZ }
    for (brick in sortedBricks) {
      val stoppedBrick = generateSequence(brick) { it.moveDown() }.first { it.isStableOnAnyOf(fallen) }
      this += stoppedBrick
      fallen += stoppedBrick.blocks
      if (stoppedBrick != brick) movedCount += 1
    }
  }
  return fallen to movedCount
}
