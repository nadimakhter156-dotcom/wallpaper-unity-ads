package com.aizoid.unityadsdemo.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aizoid.unityadsdemo.PACKAGE_NAME
import com.aizoid.unityadsdemo.SdkStatus
import com.aizoid.unityadsdemo.TEST_MODE
import com.aizoid.unityadsdemo.ui.theme.*
import java.util.Locale

@Composable
fun DeviceSessionCard(
    sessionSeconds: Long,
    sdkStatus: SdkStatus,
    modifier: Modifier = Modifier
) {
    val minutes = sessionSeconds / 60
    val seconds = sessionSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    val appVersion = "1.0.0 (Production Release)"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Device",
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device & Session Telemetry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (TEST_MODE) Color(0x33F59E0B) else Color(0x3310B981))
                        .border(1.dp, if (TEST_MODE) Color(0x80F59E0B) else Color(0x8010B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (TEST_MODE) "TEST MODE" else "LIVE PRODUCTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (TEST_MODE) Color(0xFFFBBF24) else Color(0xFF34D399)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardInner)
                    .padding(14.dp)
            ) {
                DeviceInfoRow(label = "Device Hardware", value = deviceModel)
                DeviceInfoRow(label = "OS Platform", value = androidVersion)
                DeviceInfoRow(label = "App Version", value = appVersion)
                DeviceInfoRow(
                    label = "SDK State",
                    value = if (sdkStatus is SdkStatus.Initialized) "Active / Operational" else "Standby"
                )
                DeviceInfoRow(
                    label = "Active Session Timer",
                    value = formattedTime,
                    isMonospace = true,
                    highlightColor = Color(0xFF38BDF8)
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = highlightColor
        )
    }
}

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Target Sdk 34 • Min Sdk 24 • Unity Ads 4.21.0",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Package: $PACKAGE_NAME | ID: com.aizoid.unityadsdemo",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
    }
}