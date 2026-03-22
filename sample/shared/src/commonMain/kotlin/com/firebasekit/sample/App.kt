package com.firebasekit.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import com.firebasekit.sample.resources.github
import com.firebasekit.sample.resources.open_github
import com.firebasekit.sample.theme.AppTheme
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Preview
@Composable
fun App() = AppTheme {
    val viewModel = retain { AppViewModel() }
    val data by viewModel.remoteConfigData.collectAsState()

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

        Text(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            text = data.takeIf { LocalInspectionMode.current.not() } ?: "Preview",
            fontFamily = FontFamily(Font(Res.font.Jura_Bold)),
            style = MaterialTheme.typography.bodyLarge
        )

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
