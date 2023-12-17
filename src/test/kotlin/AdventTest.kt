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
    "836040384\n10834440\n",
    "220320\n34454850\n",
    "248105065\n249515436\n",
    "21797\n23977527174353\n",
    "1842168671\n903\n",
    "6682\n353\n",
    "9957702\n512240933238\n",
    "7084\n8414003326821\n",
    "39939\n32069\n",
    "113525\n101292\n",
    "513172\n237806\n",
    "7067\n7324\n",
    "970\n1149\n",
  )
}
