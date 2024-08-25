/*
 *     freeDictionaryApp/freeDictionaryApp.feature_home.main
 *     GetRandomWordUseCase.kt Copyrighted by Yamin Siahmargooei at 2024/8/25
 *     GetRandomWordUseCase.kt Last modified at 2024/8/25
 *     This file is part of freeDictionaryApp/freeDictionaryApp.feature_home.main.
 *     Copyright (C) 2024  Yamin Siahmargooei
 *
 *     freeDictionaryApp/freeDictionaryApp.feature_home.main is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     freeDictionaryApp/freeDictionaryApp.feature_home.main is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with freeDictionaryApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.yamin8000.owl.feature_home.domain.usecase

import io.github.yamin8000.owl.feature_home.data.datasource.local.entity.TermEntity
import io.github.yamin8000.owl.feature_home.domain.repository.local.util.BaseRepository

class GetRandomWordUseCase(
    private val repository: BaseRepository<TermEntity>
) {
    suspend operator fun invoke(): String {
        return repository.all().map { it.word }.shuffled().firstOrNull() ?: "free"
    }
}