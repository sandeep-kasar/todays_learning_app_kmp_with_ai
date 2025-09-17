@file:OptIn(KoinExperimentalAPI::class)

package com.todays.learning.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.todays.learning.domain.utils.Constants.KEY_THEME
import com.todays.learning.ui.components.appbars.AppBar
import com.todays.learning.ui.components.preferences.DialogPreferenceSelection
import com.todays.learning.ui.components.preferences.PreferencesGroup
import com.todays.learning.ui.components.preferences.TextPreference
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import todayslearning.shared.generated.resources.Res
import todayslearning.shared.generated.resources.change_theme
import todayslearning.shared.generated.resources.themes
import todayslearning.shared.generated.resources.title_me
import todayslearning.shared.generated.resources.title_personalisation

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
    mainPaddingValues: PaddingValues
) {
    val settingsUiState = viewModel.settingsUiState.collectAsState().value

    val themeLabels = stringArrayResource(Res.array.themes)
    val showThemeDialog = remember { mutableStateOf(false) }
    val themeLabel = themeLabels[settingsUiState.selectedTheme]

    Scaffold(
        modifier = Modifier.padding(mainPaddingValues),
        topBar = { AppBar(stringResource(Res.string.title_me)) },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PreferencesGroup(title = stringResource(Res.string.title_personalisation)) {
                TextPreference(
                    icon = Icons.Rounded.Lightbulb,
                    title = stringResource(Res.string.change_theme),
                    subTitle = themeLabel,
                    onClick = { showThemeDialog.value = !showThemeDialog.value }
                )

                if (showThemeDialog.value) {
                    ChangeTheme(
                        viewModel = viewModel,
                        showDialog = showThemeDialog,
                        currentValue = themeLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeTheme(
    viewModel: SettingsViewModel,
    showDialog: MutableState<Boolean>,
    currentValue: String?
) {
    DialogPreferenceSelection(
        showDialog = showDialog.value,
        title = stringResource(Res.string.change_theme),
        currentValue = currentValue ?: "Default",
        labels = stringArrayResource(Res.array.themes),
        onNegativeClick = { showDialog.value = false }
    ) { theme ->
        viewModel.savePreferenceSelection(key = KEY_THEME, selection = theme)
    }
}
