package com.lysanderuy.tulogs.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.ui.theme.Error500
import com.lysanderuy.tulogs.ui.theme.Ink700
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Ink900
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist200
import com.lysanderuy.tulogs.ui.theme.Mist400
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.TuLogsType

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val email = uiState.email
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Mist400,
                    modifier = Modifier.size(18.dp).clickable(onClick = onBack)
                )
                Text(text = "Settings", style = TuLogsType.statusHeadline.copy(fontSize = 20.sp, lineHeight = 24.sp))
            }

            SettingsSection(label = "Account") {
                SettingsRow(
                    label = "Email",
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = email ?: "—", style = TuLogsType.captionText.copy(color = Mist400))
                }
            }

            SettingsSection(label = "Data") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(width = 1.dp, color = Ink800, shape = RoundedCornerShape(14.dp))
                        .background(Ink900)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Export your logs",
                                style = TuLogsType.captionText.copy(color = Paper50)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Ink800)
                                    .border(width = 1.dp, color = Ink700, shape = RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "SOON", style = TuLogsType.monoLabel.copy(fontSize = 9.sp))
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Mist600,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Delete all data", style = TuLogsType.captionText.copy(color = Error500))
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Mist600,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            SettingsSection(label = "About") {
                SettingsRow(
                    label = "Version",
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "1.0.0 (MVP)", style = TuLogsType.captionText.copy(color = Mist400))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .border(width = 1.5.dp, color = Ink700, shape = RoundedCornerShape(999.dp))
                        .clickable(onClick = onSignOut)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Sign Out", style = TuLogsType.captionText.copy(color = Mist200))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text = label.uppercase(), style = TuLogsType.monoLabel)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsRow(label: String, shape: RoundedCornerShape, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(width = 1.dp, color = Ink800, shape = shape)
            .background(Ink900)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = TuLogsType.captionText.copy(color = Paper50))
        value()
    }
}
