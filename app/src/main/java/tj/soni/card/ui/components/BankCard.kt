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

/** White-silver virtual card. Shows ONLY a masked number: no real card data exists in the app. */
@Composable
fun SoniBankCard(card: DemoCard, holder: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(8.dp, shape, ambientColor = Color(0x33102D3A), spotColor = Color(0x33102D3A))
            .background(
                Brush.linearGradient(listOf(SoniColors.SilverLight, Color(0xFFE9EEF1), SoniColors.SilverDark)),
                shape
            )
            .border(1.dp, Color.White, shape)
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SoniWordmark(modifier = Modifier.weight(1f), logoSize = 28.dp, textSize = 14)
                DemoBadge()
            }
            Column {
                Box(
                    Modifier.size(width = 40.dp, height = 28.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFE6D8A8), Color(0xFFBFA95F))),
                            RoundedCornerShape(6.dp)
                        )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "****  ****  ****  " + card.lastFour,
                    color = SoniColors.Navy,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.card_holder), color = SoniColors.Muted, fontSize = 10.sp)
                    Text(
                        holder.uppercase(),
                        color = SoniColors.Navy,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.card_expires), color = SoniColors.Muted, fontSize = 10.sp)
                    Text(card.expiry, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
        if (card.frozen) {
            Box(
                Modifier.fillMaxSize().background(Color(0xAA102D3A), shape),
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
