/*
 *     Owl: an android app for Owlbot Dictionary API
 *     HomeComposables.kt Created by Yamin Siahmargooei at 2022/9/20
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

package io.github.yamin8000.owl.content.home

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.yamin8000.owl.R
import io.github.yamin8000.owl.model.Definition
import io.github.yamin8000.owl.ui.composable.ClickableIcon
import io.github.yamin8000.owl.ui.composable.CopyAbleRippleTextWithIcon
import io.github.yamin8000.owl.ui.composable.SpeakableRippleTextWithIcon
import io.github.yamin8000.owl.ui.composable.TtsAwareComposable
import io.github.yamin8000.owl.ui.util.DynamicThemePrimaryColorsFromImage
import io.github.yamin8000.owl.ui.util.rememberDominantColorState
import io.github.yamin8000.owl.util.ImmutableHolder
import io.github.yamin8000.owl.util.speak

@Composable
internal fun WordDefinitionsList(
    localeTag: String,
    listState: LazyListState,
    searchResult: ImmutableHolder<List<Definition>>
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            items(searchResult.item) { definition ->
                key(definition.hashCode()) {
                    DynamicColorDefinitionCard(localeTag, definition)
                }
            }
        })
}

@Composable
internal fun WordCard(
    localeTag: String,
    word: String,
    pronunciation: String?,
    onAddToFavourite: () -> Unit,
    onShareWord: () -> Unit
) {
    OutlinedCard(
        shape = CutCornerShape(15.dp),
        modifier = Modifier
            .indication(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WordText(word, localeTag)
                if (pronunciation != null) {
                    PronunciationText(
                        localeTag,
                        pronunciation,
                        word
                    )
                }
            }
            Row {
                ClickableIcon(
                    imageVector = Icons.TwoTone.Favorite,
                    contentDescription = stringResource(R.string.favourites),
                    onClick = onAddToFavourite
                )
                ClickableIcon(
                    imageVector = Icons.TwoTone.Share,
                    contentDescription = stringResource(R.string.share),
                    onClick = onShareWord
                )
            }
        }
    }
}

@Composable
internal fun WordText(
    word: String,
    localeTag: String
) {
    SpeakableRippleTextWithIcon(
        word,
        Icons.TwoTone.ShortText,
        localeTag
    )
}

@Composable
internal fun PronunciationText(
    localeTag: String,
    pronunciation: String,
    word: String
) {
    TtsAwareComposable(
        ttsLanguageLocaleTag = localeTag,
        content = { ttsEngine ->
            CopyAbleRippleTextWithIcon(
                text = pronunciation,
                imageVector = Icons.TwoTone.RecordVoiceOver,
                onClick = { ttsEngine.speak(word) }
            )
        }
    )
}

@Composable
internal fun WordEmojiText(
    emoji: String,
    localeTag: String
) {
    Icons.TwoTone
    SpeakableRippleTextWithIcon(
        emoji,
        Icons.TwoTone.EmojiEmotions,
        localeTag
    )
}

@Composable
internal fun WordExampleText(
    example: String,
    localeTag: String
) {
    SpeakableRippleTextWithIcon(
        example,
        Icons.TwoTone.TextSnippet,
        localeTag
    )
}

@Composable
internal fun WordDefinitionText(
    definition: String,
    localeTag: String
) {
    SpeakableRippleTextWithIcon(
        definition,
        Icons.TwoTone.ShortText,
        localeTag
    )
}

@Composable
internal fun WordTypeText(
    type: String,
    localeTag: String
) {
    SpeakableRippleTextWithIcon(
        type,
        Icons.TwoTone.Category,
        localeTag
    )
}

@Composable
internal fun DynamicColorDefinitionCard(
    localeTag: String,
    definition: Definition
) {
    val dominantColorState = rememberDominantColorState()
    if (definition.imageUrl != null) {
        DynamicThemePrimaryColorsFromImage(dominantColorState) {
            LaunchedEffect(definition) {
                dominantColorState.updateColorsFromImageUrl(definition.imageUrl)
            }
            DefinitionCard(
                localeTag,
                definition = definition,
                cardColors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    } else {
        dominantColorState.reset()
        DefinitionCard(localeTag, definition)
    }
}

@Composable
internal fun DefinitionCard(
    localeTag: String,
    definition: Definition,
    cardColors: CardColors = CardDefaults.cardColors()
) {
    Card(
        shape = CutCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors
    ) {
        Column {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (definition.type != null)
                    WordTypeText(definition.type, localeTag)
                WordDefinitionText(definition.definition, localeTag)
                if (definition.example != null)
                    WordExampleText(definition.example, localeTag)
                if (definition.emoji != null)
                    WordEmojiText(definition.emoji, localeTag)
            }
            if (!definition.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = definition.imageUrl,
                    contentDescription = definition.definition,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}