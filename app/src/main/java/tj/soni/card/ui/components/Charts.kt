package tj.soni.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.soni.card.ui.theme.SoniColors

/** Simple bar chart built from boxes. [values] and [labels] must have equal length. */
@Composable
fun BarChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = SoniColors.Teal,
    chartHeight: Int = 100
) {
    val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val h = ((v / max) * chartHeight).toFloat().coerceAtLeast(3f)
                Box(
                    Modifier.fillMaxWidth().height(h.dp).background(
                        if (v > 0.0) barColor else SoniColors.Border,
                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
                )
                Text(
                    labels.getOrElse(i) { "" },
                    color = SoniColors.Muted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/** Horizontal progress bar; [fraction] is clamped to 0..1. */
@Composable
fun FractionBar(fraction: Float, modifier: Modifier = Modifier, color: Color = SoniColors.Teal) {
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier.fillMaxWidth().height(8.dp).background(SoniColors.Border, RoundedCornerShape(50))
    ) {
        Box(
            Modifier.fillMaxWidth(f).height(8.dp).background(color, RoundedCornerShape(50))
        )
    }
}
