package com.firebasekit.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.firebasekit.sample.resources.Jura_Bold
import com.firebasekit.sample.resources.Res
import com.firebasekit.sample.resources.firebase_kit
import com.firebasekit.sample.resources.messaging_title
import com.firebasekit.sample.resources.github
import com.firebasekit.sample.resources.open_github
import com.firebasekit.sample.resources.performance_title
import com.firebasekit.sample.resources.record
import com.firebasekit.sample.resources.record_http_metric
import com.firebasekit.sample.resources.record_trace
import com.firebasekit.sample.resources.refresh_token
import com.firebasekit.sample.resources.remote_config_title
import com.firebasekit.sample.resources.send_test_push
import com.firebasekit.sample.theme.AppTheme
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Preview
@Composable
fun App() = AppTheme {
    val remoteConfigViewModel = retain { RemoteConfigViewModel() }
    val messagingViewModel = retain { MessagingViewModel() }
    val analyticsViewModel = retain { AnalyticsViewModel() }
    val performanceViewModel = retain { PerformanceViewModel() }
    val remoteConfigData by remoteConfigViewModel.remoteConfigData.collectAsState()
    val messagingState by messagingViewModel.uiState.collectAsState()
    val performanceState by performanceViewModel.uiState.collectAsState()
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val uriHandler = LocalUriHandler.current

        Text(
            modifier = Modifier.padding(24.dp),
            text = stringResource(Res.string.firebase_kit),
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.displayMedium
        )

        SectionTitle(stringResource(Res.string.messaging_title))

        Text(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            text = messagingState.statusMessage.takeIf { isPreview.not() }
                ?: "Preview mode disables live Firebase calls.",
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = messagingState.token.takeIf { isPreview.not() } ?: "FCM token preview",
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(
                modifier = Modifier.weight(1f),
                onClick = { messagingViewModel.refreshToken() },
                enabled = messagingState.isRefreshingToken.not() && isPreview.not(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                content = { Text(stringResource(Res.string.refresh_token)) }
            )

            ElevatedButton(
                modifier = Modifier.weight(1f),
                onClick = { messagingViewModel.sendPushToSelf() },
                enabled = messagingState.canSendPush && messagingState.isSendingPush.not() && isPreview.not(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                content = { Text(stringResource(Res.string.send_test_push)) }
            )
        }

        SectionTitle(stringResource(Res.string.remote_config_title))

        Text(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            text = remoteConfigData.takeIf { isPreview.not() } ?: "Preview",
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyLarge
        )

        SectionTitle(stringResource(Res.string.performance_title))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            text = performanceState.statusMessage,
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            text = stringResource(Res.string.record),
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(
                modifier = Modifier.weight(1f),
                onClick = performanceViewModel::recordTrace,
                enabled = performanceState.isRunning.not(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                content = { Text(stringResource(Res.string.record_trace)) }
            )

            ElevatedButton(
                modifier = Modifier.weight(1f),
                onClick = performanceViewModel::recordHttpMetric,
                enabled = performanceState.isRunning.not(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                content = { Text(stringResource(Res.string.record_http_metric)) }
            )
        }

        ElevatedButton(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .widthIn(min = 200.dp),
            onClick = { uriHandler.openUri("https://github.com/RazoTRON/Firebase-KMP-Kit") },
            colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            content = {
                Icon(vectorResource(Res.drawable.github), contentDescription = null)

                Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                Text(stringResource(Res.string.open_github))
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        modifier = Modifier.padding(top = 12.dp),
        text = title,
        fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
        style = MaterialTheme.typography.headlineSmall
    )
}
