package com.example.beamsyncmobile.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beamsyncmobile.ui.theme.BeamsyncColors
import com.example.beamsyncmobile.ui.theme.BeamsyncSpacing

private val screenIcons: Map<Screen, ImageVector> = mapOf(
    Screen.Scan to Icons.Default.Home,
    Screen.Downloads to Icons.Default.CloudDownload,
    Screen.Uploads to Icons.Default.CloudUpload,
    Screen.Settings to Icons.Default.Settings,
)

@Composable
fun BeamsyncBottomNav(
    items: List<BottomNavItem>,
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BeamsyncColors.surfaceBase)
            .border(1.dp, BeamsyncColors.strokeDefault, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BeamsyncSpacing.space1),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = item.screen == selectedScreen
                val icon = screenIcons[item.screen] ?: Icons.Default.Home

                Column(
                    modifier = Modifier
                        .clickable { onScreenSelected(item.screen) }
                        .padding(
                            horizontal = BeamsyncSpacing.space4,
                            vertical = BeamsyncSpacing.space2,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        tint = if (isSelected) BeamsyncColors.accentPrimary else BeamsyncColors.textSecondary,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) BeamsyncColors.accentPrimary else BeamsyncColors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
