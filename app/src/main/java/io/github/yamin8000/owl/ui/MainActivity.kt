/*
 *     freeDictionaryApp/freeDictionaryApp.app.main
 *     MainActivity.kt Copyrighted by Yamin Siahmargooei at 2024/8/18
 *     MainActivity.kt Last modified at 2024/8/18
 *     This file is part of freeDictionaryApp/freeDictionaryApp.app.main.
 *     Copyright (C) 2024  Yamin Siahmargooei
 *
 *     freeDictionaryApp/freeDictionaryApp.app.main is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     freeDictionaryApp/freeDictionaryApp.app.main is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with freeDictionaryApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.yamin8000.owl.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.yamin8000.owl.common.ui.navigation.Nav
import io.github.yamin8000.owl.common.ui.theme.AppTheme
import io.github.yamin8000.owl.core.LocalTTS
import io.github.yamin8000.owl.core.favouritesDataStore
import io.github.yamin8000.owl.core.historyDataStore
import io.github.yamin8000.owl.core.settingsDataStore
import io.github.yamin8000.owl.data.DataStoreRepository
import io.github.yamin8000.owl.feature_home.ui.HomeScreen
import io.github.yamin8000.owl.ui.content.AboutContent
import io.github.yamin8000.owl.ui.content.favourites.FavouritesContent
import io.github.yamin8000.owl.ui.content.favourites.FavouritesViewModel
import io.github.yamin8000.owl.ui.content.history.HistoryContent
import io.github.yamin8000.owl.ui.content.history.HistoryViewModel
import io.github.yamin8000.owl.ui.content.settings.SettingsContent
import io.github.yamin8000.owl.ui.content.settings.SettingsViewModel
import io.github.yamin8000.owl.ui.content.settings.ThemeSetting
import io.github.yamin8000.owl.util.Constants
import io.github.yamin8000.owl.util.TTS
import io.github.yamin8000.owl.util.log
import io.github.yamin8000.owl.util.viewModelFactory
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.runBlocking
import java.util.Locale

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {

    private var outsideInput: String? = null

    private var theme: ThemeSetting = ThemeSetting.System

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        outsideInput = handleOutsideInputIntent()

        try {
            runBlocking { theme = getCurrentTheme() }
        } catch (e: InterruptedException) {
            log(e.stackTraceToString())
        }

        setContent {
            var currentTheme by remember { mutableStateOf(theme) }
            MainContent(
                currentTheme = currentTheme,
                content = {
                    Scaffold {
                        val onThemeChanged: (ThemeSetting) -> Unit = remember {
                            {
                                currentTheme = it
                            }
                        }
                        MainNav(onThemeChanged = onThemeChanged)
                    }
                }
            )
        }
    }

    private suspend fun getCurrentTheme() = ThemeSetting.valueOf(
        DataStoreRepository(settingsDataStore).getString(Constants.THEME)
            ?: ThemeSetting.System.name
    )

    private fun handleOutsideInputIntent(): String? {
        //widget
        //return intent.extras?.getString("search")
        return if (intent.type == "text/plain") {
            when (intent.action) {
                Intent.ACTION_TRANSLATE, Intent.ACTION_DEFINE, Intent.ACTION_SEND -> {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                }

                Intent.ACTION_PROCESS_TEXT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                    else null
                }

                else -> {
                    null
                }
            }
        } else {
            null
        }
    }

    @Composable
    private fun MainContent(
        currentTheme: ThemeSetting,
        content: @Composable () -> Unit
    ) {
        AppTheme(
            isDarkTheme = isDarkTheme(currentTheme, isSystemInDarkTheme()),
            isOledTheme = currentTheme == ThemeSetting.Darker,
            isDynamicColor = currentTheme == ThemeSetting.System,
            content = content
        )
    }

    private fun isDarkTheme(
        themeSetting: ThemeSetting,
        isSystemInDarkTheme: Boolean
    ) = when (themeSetting) {
        ThemeSetting.Light -> false
        ThemeSetting.System -> isSystemInDarkTheme
        ThemeSetting.Dark, ThemeSetting.Darker -> true
    }

    @Composable
    private fun MainNav(
        onThemeChanged: (ThemeSetting) -> Unit
    ) {
        val context = LocalContext.current
        val settingsVM: SettingsViewModel = viewModel(factory = viewModelFactory {
            initializer {
                SettingsViewModel(DataStoreRepository(context.settingsDataStore))
            }
        })

        val historyVM: HistoryViewModel = viewModel(factory = viewModelFactory {
            initializer {
                HistoryViewModel(context.historyDataStore)
            }
        })

        val favouritesVM: FavouritesViewModel = viewModel(factory = viewModelFactory {
            initializer {
                FavouritesViewModel(context.favouritesDataStore)
            }
        })

        val ttsTag by settingsVM.ttsLang.collectAsStateWithLifecycle()
        val ttsHelper = remember(ttsTag) { TTS(context, Locale.forLanguageTag(ttsTag)) }
        val tts: MutableState<TextToSpeech?> = remember { mutableStateOf(null) }
        LaunchedEffect(Unit) { tts.value = ttsHelper.getTts() }

        CompositionLocalProvider(LocalTTS provides tts.value) {
            val start = "${Nav.Route.Home}/{${Nav.Arguments.Search}}"
            val navController = rememberNavController()
            val onBackClick: () -> Unit = remember { { navController.navigateUp() } }
            NavHost(
                navController = navController,
                startDestination = start,
                enterTransition = { fadeIn(animationSpec = tween(100)) },
                exitTransition = { fadeOut(animationSpec = tween(100)) },
                builder = {
                    composable(start) {
                        val searchTerm = it.arguments?.getString(Nav.Arguments.Search())
                        if (searchTerm == null && outsideInput != null)
                            it.savedStateHandle[Nav.Arguments.Search()] = outsideInput.toString()
                        val addToHistory: (String) -> Unit = remember {
                            { item -> historyVM.add(item) }
                        }
                        val addToFavourite: (String) -> Unit = remember {
                            { item -> favouritesVM.add(item) }
                        }
                        HomeScreen(
                            navController = navController,
                            onAddToHistory = addToHistory,
                            onAddToFavourite = addToFavourite
                        )
                    }

                    composable(Nav.Route.About.toString()) {
                        AboutContent(
                            onBackClick = onBackClick
                        )
                    }

                    composable(Nav.Route.Favourites()) {
                        val onFavouritesItemClick: (String) -> Unit = remember {
                            { favourite -> navController.navigate("${Nav.Route.Home}/${favourite}") }
                        }
                        FavouritesContent(
                            onFavouritesItemClick = onFavouritesItemClick,
                            onBackClick = onBackClick,
                            favourites = favouritesVM.favourites.collectAsStateWithLifecycle().value.toPersistentList(),
                            onRemoveAll = remember { { favouritesVM.removeAll() } },
                            onRemove = remember { { favouritesVM.remove(it) } }
                        )
                    }

                    composable(Nav.Route.History()) {
                        val onHistoryItemClick: (String) -> Unit = remember {
                            { history -> navController.navigate("${Nav.Route.Home}/${history}") }
                        }
                        HistoryContent(
                            onHistoryItemClick = onHistoryItemClick,
                            onBackClick = onBackClick,
                            history = historyVM.history.collectAsStateWithLifecycle().value.toPersistentList(),
                            onRemoveAll = remember { { historyVM.removeAll() } },
                            onRemove = remember { { historyVM.remove(it) } }
                        )
                    }

                    composable(Nav.Route.Settings()) {
                        SettingsContent(
                            isVibrating = settingsVM.isVibrating.collectAsStateWithLifecycle().value,
                            onVibratingChange = remember { { settingsVM.updateVibrationSetting(it) } },
                            isStartingBlank = settingsVM.isStartingBlank.collectAsStateWithLifecycle().value,
                            onStartingBlankChange = remember { { settingsVM.updateStartingBlank(it) } },
                            themeSetting = settingsVM.themeSetting.collectAsStateWithLifecycle().value,
                            onSystemThemeChange = onThemeChanged,
                            onThemeSettingChange = remember { { settingsVM.updateThemeSetting(it) } },
                            ttsTag = settingsVM.ttsLang.collectAsStateWithLifecycle().value,
                            onTtsTagChange = remember { { settingsVM.updateTtsLang(it) } },
                            onBackClick = onBackClick
                        )
                    }
                }
            )
        }
    }
}