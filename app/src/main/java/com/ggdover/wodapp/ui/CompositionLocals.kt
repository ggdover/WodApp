package com.ggdover.wodapp.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ggdover.wodapp.data.repository.WodRepository

val LocalWodRepository =
    staticCompositionLocalOf<WodRepository> { error("WodRepository not provided") }
