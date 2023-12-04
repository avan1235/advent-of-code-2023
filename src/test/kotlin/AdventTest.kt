import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AdventTest {

  @Test
  fun `test days outputs`() {
    expectedOutputs.forEachIndexed { idx, expect ->
      val out = catchSystemOut { AdventDay.all[idx].solve() }
      assertEquals(expect, out)
    }
    println("Passed tests for ${expectedOutputs.size} days")
  }

  private val expectedOutputs = listOf(
    "54601\n54078\n",
    "2449\n63981\n",
    "550934\n81997870\n",
    "24160\n5659035\n",
  )
}
