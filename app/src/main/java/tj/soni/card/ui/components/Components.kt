package tj.soni.card.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.soni.card.R
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TxType
import tj.soni.card.domain.formatDate
import tj.soni.card.domain.formatMoney
import tj.soni.card.ui.icon
import tj.soni.card.ui.labelRes
import tj.soni.card.ui.theme.SoniColors

@Composable
fun SoniLogoMark(logoSize: Dp = 40.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(logoSize)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(SoniColors.Teal, cornerRadius = CornerRadius(w * 0.28f, w * 0.28f))
        drawRoundRect(
            Color.White,
            topLeft = Offset(w * 0.2f, h * 0.3f),
            size = Size(w * 0.6f, h * 0.4f),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
        )
        drawRect(
            SoniColors.Mint,
            topLeft = Offset(w * 0.2f, h * 0.41f),
            size = Size(w * 0.6f, h * 0.08f)
        )
        drawRoundRect(
            SoniColors.Teal,
            topLeft = Offset(w * 0.27f, h * 0.57f),
            size = Size(w * 0.2f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
        )
    }
}

@Composable
fun SoniWordmark(modifier: Modifier = Modifier, logoSize: Dp = 32.dp, textSize: Int = 16) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        SoniLogoMark(logoSize)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = SoniColors.Navy,
            fontWeight = FontWeight.Bold,
            fontSize = textSize.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoniColors.Teal,
            contentColor = Color.White,
            disabledContainerColor = SoniColors.Teal.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SoniColors.Teal),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoniColors.Teal)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = SoniColors.Navy
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SoniColors.Navy,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = SoniColors.MintSoft
    ) {
        Text(
            text = stringResource(R.string.demo_badge),
            color = SoniColors.Teal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun DemoBanner(text: String = stringResource(R.string.demo_banner), modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SoniColors.MintSoft, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = SoniColors.Navy, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val base = modifier
        .fillMaxWidth()
        .background(SoniColors.Surface, shape)
        .border(1.dp, SoniColors.Border, shape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(clickable.padding(16.dp), content = content)
}

@Composable
fun SectionTitle(text: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = SoniColors.Navy,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = SoniColors.Teal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(72.dp).background(SoniColors.MintSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(message, color = SoniColors.Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SoniColors.Teal)
    }
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Row(modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = SoniColors.Error, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(message, color = SoniColors.Error, fontSize = 13.sp)
        }
    }
}

@Composable
fun SoniTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    prefix: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            prefix = prefix?.let { pre -> { Text(pre) } },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SoniColors.Teal,
                unfocusedBorderColor = SoniColors.Border,
                focusedLabelColor = SoniColors.Teal,
                focusedContainerColor = SoniColors.Surface,
                unfocusedContainerColor = SoniColors.Surface,
                errorContainerColor = SoniColors.Surface
            ),
            modifier = Modifier.fillMaxWidth()
        )
        ErrorText(error)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoniColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, color = SoniColors.Navy, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = SoniColors.Navy) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = SoniColors.Teal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText, color = SoniColors.Muted) }
        }
    )
}

/** Selectable pill used for filters and option lists. */
@Composable
fun SoniChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .background(if (selected) SoniColors.Teal else SoniColors.Surface, shape)
            .border(1.dp, if (selected) SoniColors.Teal else SoniColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else SoniColors.Navy,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TxRow(tx: Transaction, onClick: () -> Unit) {
    val incoming = tx.type == TxType.INCOMING
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(
                if (incoming) SoniColors.MintSoft else Color(0xFFF3F6F7),
                RoundedCornerShape(14.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                tx.category.icon(),
                contentDescription = null,
                tint = if (incoming) SoniColors.Success else SoniColors.Navy,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.counterparty, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                stringResource(tx.category.labelRes()) + " • " + formatDate(tx.timestamp),
                color = SoniColors.Muted,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (incoming) "+" else "-") + formatMoney(tx.amount),
                color = if (incoming) SoniColors.Success else SoniColors.Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            DemoBadge()
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(SoniColors.MintSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = SoniColors.Muted, fontSize = 12.sp)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = SoniColors.Muted)
        }
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(color = SoniColors.Border)
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SoniColors.Muted, fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
        Text(value, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}

@Composable
fun FlowSummaryTile(incoming: Boolean, label: String, amount: Double, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).background(
                    if (incoming) SoniColors.MintSoft else Color(0xFFFBEAEA),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (incoming) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = if (incoming) SoniColors.Success else SoniColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(label, color = SoniColors.Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(formatMoney(amount), color = SoniColors.Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
