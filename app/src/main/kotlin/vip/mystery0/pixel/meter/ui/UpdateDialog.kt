package de.teddybaerd.nowbarmeter.ui

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.teddybaerd.nowbarmeter.R

@Composable
fun UpdateDialog(viewModel: MainViewModel) {
    val context = LocalContext.current
    val updateInfo by viewModel.updateAvailable.collectAsState()
        updateInfo?.let { info ->
            val targetUrl = info.apkDownloadUrl ?: info.htmlUrl
            AlertDialog(
                onDismissRequest = { viewModel.clearUpdateDialog() },
                title = { Text(stringResource(R.string.update_available_title, info.versionName)) },
                text = {
                    Column {
                        Text(stringResource(R.string.update_available_message, info.versionName))
                        if (info.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.releaseNotes.take(400),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, targetUrl.toUri())
                        context.startActivity(intent)
                        viewModel.clearUpdateDialog()
                    }) {
                        Text(stringResource(if (info.apkDownloadUrl != null) R.string.action_download_apk else R.string.action_update))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.skipUpdate(info.versionName)
                    }) {
                        Text(stringResource(R.string.action_skip_version))
                    }
                }
            )
        }

}
