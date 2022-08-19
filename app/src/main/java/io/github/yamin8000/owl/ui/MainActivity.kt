/*
 *     Owl: an android app for Owlbot Dictionary API
 *     MainActivity.kt Created by Yamin Siahmargooei at 2022/6/16
 *     This file is part of Owl.
 *     Copyright (C) 2022  Yamin Siahmargooei
 *
 *     Owl is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Owl is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Owl.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.yamin8000.owl.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orhanobut.logger.Logger
import io.github.yamin8000.owl.R
import io.github.yamin8000.owl.model.Definition
import io.github.yamin8000.owl.model.Word
import io.github.yamin8000.owl.network.APIs
import io.github.yamin8000.owl.network.Web
import io.github.yamin8000.owl.network.Web.asyncResponse
import io.github.yamin8000.owl.network.Web.getAPI
import io.github.yamin8000.owl.ui.composable.AddDefinitionCard
import io.github.yamin8000.owl.ui.composable.AddWordCard
import io.github.yamin8000.owl.ui.theme.OwlTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainContent() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
    @Composable
    private fun MainContent() {
        OwlTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                val focusManager = LocalFocusManager.current
                var searchText by remember { mutableStateOf("") }
                var searchResult by remember { mutableStateOf<List<Definition>>(emptyList()) }
                var rawBody by remember { mutableStateOf<Word?>(null) }
                var isSearching by remember { mutableStateOf(false) }
                val gridState = rememberLazyGridState()

                Scaffold(
                    topBar = {
                        MainTopBar(
                            MainTopAppBarCallbacks(
                                onHistoryClick = {

                                },
                                onFavouritesClick = {

                                },
                                onRandomWordClick = {
                                    isSearching = true
                                    onRandomWordClick {
                                        searchText = it
                                        createSearchWordRequest(searchText) { word ->
                                            isSearching = false
                                            searchResult = word.definitions
                                            rawBody = word
                                        }
                                    }
                                },
                                onSettingsClick = {

                                },
                                onInfoClick = {

                                }
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            isSearching = true
                            createSearchWordRequest(searchText) { word ->
                                isSearching = false
                                searchResult = word.definitions
                                rawBody = word
                            }
                            focusManager.clearFocus()
                        }) { Icon(Icons.Filled.Search, stringResource(id = R.string.search)) }
                    },
                    bottomBar = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isSearching)
                                CircularProgressIndicator()
                            MainBottomBar(
                                MainBottomBarCallbacks(
                                    onSearch = {
                                        searchText = it
                                        isSearching = true
                                        createSearchWordRequest(searchText) { word ->
                                            isSearching = false
                                            searchResult = word.definitions
                                            rawBody = word
                                        }
                                        focusManager.clearFocus()
                                    },
                                    onTextChanged = {
                                        searchText = it
                                    }
                                )
                            )
                        }
                    }) { contentPadding ->

                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                        ) {
                            rawBody?.let { AddWordCard(it) }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            state = gridState

                        ) {
                            items(searchResult) { definition ->
                                AddDefinitionCard(definition)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onRandomWordClick(onResponse: (String) -> Unit) {
        val retrofit = Web.createCustomUrlRetrofit(Web.ninjaApiBaseUrl)
        retrofit.getAPI<APIs.NinjaAPI>().getRandomWord()
            .asyncResponse(this, {
                val body = it.body()
                body?.let { randomWord ->
                    onResponse(randomWord.word)
                }
                if (body == null) onResponse("")
            }, { handleNullResponseBody(999) })
    }

    private fun createSearchWordRequest(input: String, callback: (Word) -> Unit) {
        Web.retrofit.getAPI<APIs.OwlBotWordAPI>().searchWord(input.trim()).asyncResponse(this, {
            val body = it.body()
            if (body != null) callback(body)
            else handleNullResponseBody(it.code())
        }, { throwable -> handleException(throwable) })
    }

    private fun handleException(throwable: Throwable) {
        Logger.d(throwable.stackTraceToString())
        Toast.makeText(this, getString(R.string.general_net_error), Toast.LENGTH_LONG).show()
    }

    private fun handleNullResponseBody(code: Int) {
        val message = when (code) {
            401 -> getString(R.string.api_authorization_error)
            404 -> getString(R.string.definition_not_found)
            429 -> getString(R.string.api_throttled)
            else -> getString(R.string.general_net_error)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}