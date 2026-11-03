package tj.big.card.ui.components

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
import androidx.compose.ui.draw.shadow
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
import tj.big.card.R
import tj.big.card.data.models.Transaction
import tj.big.card.data.models.TxType
import tj.big.card.domain.formatDate
import tj.big.card.domain.formatMoney
import tj.big.card.ui.icon
import tj.big.card.ui.labelRes
import tj.big.card.ui.theme.BigColors

@Composable
fun BigLogoMark(logoSize: Dp = 40.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(logoSize)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(BigColors.Teal, cornerRadius = CornerRadius(w * 0.28f, w * 0.28f))
        drawRoundRect(
            Color.White,
            topLeft = Offset(w * 0.2f, h * 0.3f),
            size = Size(w * 0.6f, h * 0.4f),
            cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
        )
        drawRect(
            BigColors.Orange,
            topLeft = Offset(w * 0.2f, h * 0.41f),
            size = Size(w * 0.6f, h * 0.08f)
        )
        drawRoundRect(
            BigColors.Teal,
            topLeft = Offset(w * 0.27f, h * 0.57f),
            size = Size(w * 0.2f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
        )
    }
}

@Composable
fun BigWordmark(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
    textSize: Int = 16,
    textColor: Color = BigColors.Navy
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        BigLogoMark(logoSize)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = textColor,
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
    loading: Boolean = false,
    containerColor: Color = BigColors.Orange
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
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
            Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, BigColors.Teal),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BigColors.Teal)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Sub-screens (with a back button) get the blue header; tab screens get a plain title. */
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val blue = onBack != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (blue) Modifier.background(BigColors.Teal) else Modifier)
            .padding(horizontal = 8.dp, vertical = if (blue) 10.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = if (blue) Color.White else BigColors.Navy,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}

@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = BigColors.MintSoft
    ) {
        Text(
            text = stringResource(R.string.demo_badge),
            color = BigColors.Teal,
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
            .background(BigColors.MintSoft, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = BigColors.Teal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = BigColors.Navy, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val base = modifier
        .fillMaxWidth()
        .shadow(3.dp, shape, ambientColor = Color(0x330A74F5), spotColor = Color(0x330A74F5))
        .background(BigColors.Surface, shape)
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
            color = BigColors.Navy,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = BigColors.Teal, fontWeight = FontWeight.SemiBold)
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
            Modifier.size(72.dp).background(BigColors.MintSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BigColors.Teal, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = BigColors.Navy, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(message, color = BigColors.Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BigColors.Teal)
    }
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Row(modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = BigColors.Error, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(message, color = BigColors.Error, fontSize = 13.sp)
        }
    }
}

@Composable
fun BigTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    prefix: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            prefix = prefix?.let { pre -> { Text(pre) } },
            trailingIcon = trailing,
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BigColors.Teal,
                unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = BigColors.Teal,
                focusedContainerColor = BigColors.Field,
                unfocusedContainerColor = BigColors.Field,
                errorContainerColor = BigColors.Field
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
        containerColor = BigColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, color = BigColors.Navy, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = BigColors.Navy) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = BigColors.Teal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText, color = BigColors.Muted) }
        }
    )
}

/** Selectable pill used for filters and option lists. */
@Composable
fun BigChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .background(if (selected) BigColors.Teal else BigColors.Surface, shape)
            .border(1.dp, if (selected) BigColors.Teal else BigColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else BigColors.Navy,
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
                if (incoming) BigColors.MintSoft else Color(0xFFF3F6F7),
                RoundedCornerShape(14.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                tx.category.icon(),
                contentDescription = null,
                tint = if (incoming) BigColors.Success else BigColors.Navy,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.counterparty, color = BigColors.Navy, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                stringResource(tx.category.labelRes()) + " • " + formatDate(tx.timestamp),
                color = BigColors.Muted,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (incoming) "+" else "-") + formatMoney(tx.amount),
                color = if (incoming) BigColors.Success else BigColors.Navy,
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
            Modifier.size(40.dp).background(BigColors.Field, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BigColors.Teal, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = BigColors.Navy, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = BigColors.Muted, fontSize = 12.sp)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = BigColors.Muted)
        }
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(color = BigColors.Border)
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = BigColors.Muted, fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
        Text(value, color = BigColors.Navy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}

@Composable
fun FlowSummaryTile(incoming: Boolean, label: String, amount: Double, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).background(
                    if (incoming) BigColors.MintSoft else Color(0xFFFBEAEA),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (incoming) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = if (incoming) BigColors.Success else BigColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(label, color = BigColors.Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(formatMoney(amount), color = BigColors.Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
