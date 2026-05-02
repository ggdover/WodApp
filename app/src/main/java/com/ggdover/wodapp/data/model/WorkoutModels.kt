package com.ggdover.wodapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
}

@Serializable
data class ExerciseStep(
    val name: String = "",
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val notes: String? = null,
)

@Serializable
data class ExerciseBlock(
    val rounds: Int = 1,
    val steps: List<ExerciseStep> = emptyList(),
)

@Serializable
data class WarmUpSection(
    val blocks: List<ExerciseBlock> = emptyList(),
)

@Serializable
data class WodVariant(
    val label: String = "",
    val formatDescription: String = "",
    val timeCapSeconds: Int? = null,
    val blocks: List<ExerciseBlock> = emptyList(),
)

@Serializable
data class WodSection(
    val variants: List<WodVariant> = emptyList(),
)

@Serializable
data class StrengthLine(
    val exerciseName: String = "",
    val sets: Int = 1,
    val reps: Int = 1,
    val percentOfPb: Int? = null,
    val notes: String? = null,
)

@Serializable
data class StrengthSection(
    val lines: List<StrengthLine> = emptyList(),
)

/** Logged result for a workout moment — can mirror template with per-set tweaks. */
@Serializable
data class LoggedResult(
    val variantLabel: String? = null,
    val warmupBlocks: List<ExerciseBlock> = emptyList(),
    val wodBlocks: List<ExerciseBlock> = emptyList(),
    val strengthLines: List<StrengthLine> = emptyList(),
    val freeFormNotes: String = "",
)

fun defaultLoggedResult(): LoggedResult = LoggedResult()

fun buildSearchText(
    name: String,
    warmUp: WarmUpSection,
    wod: WodSection,
    strength: StrengthSection,
): String {
    val parts = mutableListOf<String>()
    parts.add(name)
    warmUp.blocks.forEach { block ->
        block.steps.forEach { parts.add(it.name) }
    }
    wod.variants.forEach { variant ->
        variant.blocks.forEach { block ->
            block.steps.forEach { parts.add(it.name) }
        }
    }
    strength.lines.forEach { parts.add(it.exerciseName) }
    return parts.joinToString(" ").lowercase()
}
