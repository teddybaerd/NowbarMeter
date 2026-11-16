package de.teddybaerd.nowbarmeter.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.teddybaerd.nowbarmeter.R
import de.teddybaerd.nowbarmeter.data.model.AppThemeMode
import de.teddybaerd.nowbarmeter.ui.theme.PixelPulseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Offline texts only; no browser, WebView or network access. */
class LicensesActivity : ComponentActivity() {
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val oled by viewModel.isOledThemeEnabled.collectAsState(initial = false)
            val mode by viewModel.appThemeMode.collectAsState(initial = AppThemeMode.Dynamic.value)
            val color by viewModel.appThemeColor.collectAsState(initial = AppThemeMode.DEFAULT_THEME_COLOR)
            val texts by produceState<List<String>>(emptyList()) {
                value = withContext(Dispatchers.IO) { readNotices() }
            }
            PixelPulseTheme(isOledTheme = oled, themeMode = mode, themeColor = color) {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.settings_licenses)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.content_description_back))
                            }
                        }
                    )
                }) { padding ->
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                        item { Text(stringResource(R.string.licenses_app), Modifier.padding(16.dp)) }
                        items(texts) { text ->
                            SelectionContainer {
                                Text(text, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun readNotices(): List<String> = buildList {
        add(assets.open("PROJECT_NOTICES.txt").bufferedReader().use { it.readText() })
        add("Apache License 2.0")
        addAll(assets.open("APACHE-2.0.txt").bufferedReader().use { it.readText() }.chunked(4000))
        val json = JSONObject(resources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() })
        add(getString(R.string.licenses_components))
        val libraries = json.getJSONArray("libraries")
        for (index in 0 until libraries.length()) {
            val library = libraries.getJSONObject(index)
            val developers = library.optJSONArray("developers")
            val names = if (developers == null) emptyList() else (0 until developers.length()).map {
                developers.getJSONObject(it).optString("name")
            }.filter { it.isNotBlank() }
            add(listOf(
                library.optString("name"),
                library.getString("uniqueId") + ":" + library.optString("artifactVersion"),
                library.optJSONObject("organization")?.optString("name").orEmpty(),
                names.joinToString(", "),
                library.optString("website"),
                library.getJSONArray("licenses").let { licenses ->
                    (0 until licenses.length()).joinToString(", ") { licenses.getString(it) }
                }
            ).filter { it.isNotBlank() }.joinToString("\n"))
        }
        val licenses = json.getJSONObject("licenses")
        for (id in licenses.keys().asSequence().sorted()) {
            val license = licenses.getJSONObject(id)
            add(license.optString("name", id))
            addAll(license.getString("content").chunked(4000))
        }
        add(getString(R.string.licenses_notices))
        addAll(assets.open("THIRD_PARTY_NOTICES.txt").bufferedReader().use { it.readText() }.chunked(4000))
    }
}
