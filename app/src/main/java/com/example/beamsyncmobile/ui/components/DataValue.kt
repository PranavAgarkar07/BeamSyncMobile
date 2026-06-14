package com.example.beamsyncmobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncTextStyles
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

// Large data value (monospace) with small label underneath
// This pairing creates rapid hierarchical parsing —
// periphery catches the number, fovea confirms the label

@Composable
fun DataValue(
    value: String,
    label: String,
    valueColor: Color = BeamsyncColors.textAccentOrange,
    labelColor: Color = BeamsyncColors.textSecondary,
    modifier: Modifier = Modifier,
    valueSize: androidx.compose.ui.unit.TextUnit = 16.sp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = if (valueSize == 16.sp) BeamsyncTextStyles.dataBase
                    else BeamsyncTextStyles.dataLg,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
            maxLines = 1,
        )
    }
}

// Row of DataValue pairs for telemetry displays
@Composable
fun DataValueRow(
    values: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        values.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Spacer(Modifier.width(BeamsyncSpacing.space6))
            }
            DataValue(
                value = value,
                label = label,
            )
        }
    }
}
