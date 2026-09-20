package id.wahidiyah.miladiyyah.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = Surface, tonalElevation = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSizes.screenHorizontal,
                        vertical = AppSizes.headerVertical
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = BrandPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = subtitle,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                actions()
            }

            HorizontalDivider(color = Border)
        }
    }
}

@Composable
fun AppSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 1.8.sp,
        modifier = modifier
    )
}
