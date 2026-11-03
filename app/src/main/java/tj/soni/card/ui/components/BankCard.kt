package tj.soni.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.soni.card.R
import tj.soni.card.data.models.DemoCard
import tj.soni.card.ui.theme.SoniColors

/**
 * Virtual DEMO card: blue for the main card, white for the others.
 * Shows ONLY a masked number: no real card data exists in the app.
 */
@Composable
fun SoniBankCard(card: DemoCard, holder: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    val dark = card.primary
    val main = if (dark) Color.White else SoniColors.Navy
    val muted = if (dark) Color(0xB3FFFFFF) else SoniColors.Muted
    val brush = if (dark) {
        Brush.linearGradient(listOf(Color(0xFF2B8CFF), Color(0xFF0A5FD6)))
    } else {
        Brush.linearGradient(listOf(Color.White, Color(0xFFEAF1FF)))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(8.dp, shape, ambientColor = Color(0x440A74F5), spotColor = Color(0x440A74F5))
            .background(brush, shape)
            .border(1.dp, if (dark) Color(0x33FFFFFF) else SoniColors.Border, shape)
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SoniWordmark(
                    modifier = Modifier.weight(1f),
                    logoSize = 28.dp,
                    textSize = 14,
                    textColor = main
                )
                DemoBadge()
            }
            Column {
                Box(
                    Modifier.size(width = 40.dp, height = 28.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFB27A), Color(0xFFFF7525))),
                            RoundedCornerShape(6.dp)
                        )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "****  ****  ****  " + card.lastFour,
                    color = main,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.card_holder), color = muted, fontSize = 10.sp)
                    Text(
                        holder.uppercase(),
                        color = main,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.card_expires), color = muted, fontSize = 10.sp)
                    Text(card.expiry, color = main, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
        if (card.frozen) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA141B34), shape),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.card_frozen),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
