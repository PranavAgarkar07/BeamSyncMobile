package com.example.beamsyncmobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.components.BeamsyncButton
import com.example.beamsyncmobile.ui.components.BeamsyncButtonSize
import com.example.beamsyncmobile.ui.components.BeamsyncButtonVariant
import com.example.beamsyncmobile.ui.components.BeamsyncChip
import com.example.beamsyncmobile.ui.components.BeamsyncChipVariant
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

@Composable
fun SettingsScreen() {
    var transferMode by remember { mutableStateOf("Ask First") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeamsyncColors.surfaceBase)
            .padding(BeamsyncSpacing.space8)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "SETTINGS",
            color = BeamsyncColors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Transfer mode section
        Text(
            text = "TRANSFER MODE",
            color = BeamsyncColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))

        val modes = listOf("Accept All", "Ask First", "Block All")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeamsyncSpacing.space2),
        ) {
            modes.forEach { mode ->
                val isSelected = transferMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            if (isSelected) BeamsyncColors.accentPrimary else BeamsyncColors.surfaceRaised,
                            shape = RoundedCornerShape(0.dp),
                        )
                        .border(
                            1.dp,
                            if (isSelected) BeamsyncColors.accentPrimary else BeamsyncColors.strokeDefault,
                            RoundedCornerShape(0.dp),
                        )
                        .clickable { transferMode = mode },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mode.uppercase(),
                        color = if (isSelected) BeamsyncColors.surfaceBase else BeamsyncColors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // Save path section
        Text(
            text = "SAVE LOCATION",
            color = BeamsyncColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .clickable { /* TODO: open SAF directory picker */ }
                .padding(horizontal = BeamsyncSpacing.space4),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "/Downloads/BeamSync",
                    color = BeamsyncColors.textPrimary,
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
                Text(
                    text = "CHANGE",
                    color = BeamsyncColors.accentPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        Spacer(Modifier.height(BeamsyncSpacing.space6))

        // About section
        Text(
            text = "ABOUT",
            color = BeamsyncColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(BeamsyncSpacing.space3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BeamsyncColors.surfaceRaised)
                .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(0.dp))
                .padding(BeamsyncSpacing.space4),
        ) {
            Column {
                Text(
                    text = "BeamSync Mobile",
                    color = BeamsyncColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(BeamsyncSpacing.space1))
                Text(
                    text = "v1.0.0",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(BeamsyncSpacing.space1))
                Text(
                    text = "Secure peer-to-peer file transfer",
                    color = BeamsyncColors.textSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
