package com.ggdover.wodapp.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.model.ExerciseBlock
import com.ggdover.wodapp.data.model.ExerciseStep
import com.ggdover.wodapp.data.model.LoggedResult
import com.ggdover.wodapp.data.model.StrengthLine
import com.ggdover.wodapp.data.model.StrengthSection
import com.ggdover.wodapp.data.model.WarmUpSection
import com.ggdover.wodapp.data.model.WodSection
import com.ggdover.wodapp.data.model.WodVariant

@Composable
fun WarmUpSectionCard(section: WarmUpSection, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.section_warm_up), style = MaterialTheme.typography.titleMedium)
            if (section.blocks.isEmpty()) {
                Text(stringResource(R.string.empty_section), style = MaterialTheme.typography.bodyMedium)
            } else {
                section.blocks.forEachIndexed { bi, block ->
                    BlockView(blockIndex = bi, block = block)
                }
            }
        }
    }
}

@Composable
fun WodSectionCard(section: WodSection, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.section_wod), style = MaterialTheme.typography.titleMedium)
            if (section.variants.isEmpty()) {
                Text(stringResource(R.string.empty_section), style = MaterialTheme.typography.bodyMedium)
            } else {
                section.variants.forEach { variant ->
                    WodVariantView(variant)
                }
            }
        }
    }
}

@Composable
fun StrengthSectionCard(section: StrengthSection, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.section_strength), style = MaterialTheme.typography.titleMedium)
            if (section.lines.isEmpty()) {
                Text(stringResource(R.string.empty_section), style = MaterialTheme.typography.bodyMedium)
            } else {
                section.lines.forEach { line ->
                    StrengthLineView(line)
                }
            }
        }
    }
}

@Composable
fun LoggedResultCard(result: LoggedResult, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.logged_result), style = MaterialTheme.typography.titleMedium)
            result.variantLabel?.takeIf { it.isNotBlank() }?.let {
                Text(stringResource(R.string.variant_label_fmt, it), style = MaterialTheme.typography.bodyLarge)
            }
            if (result.warmupBlocks.isNotEmpty()) {
                Text(stringResource(R.string.warmup_logged), style = MaterialTheme.typography.labelLarge)
                result.warmupBlocks.forEachIndexed { i, b -> BlockView(i, b) }
            }
            if (result.wodBlocks.isNotEmpty()) {
                Text(stringResource(R.string.wod_logged), style = MaterialTheme.typography.labelLarge)
                result.wodBlocks.forEachIndexed { i, b -> BlockView(i, b) }
            }
            if (result.strengthLines.isNotEmpty()) {
                Text(stringResource(R.string.strength_logged), style = MaterialTheme.typography.labelLarge)
                result.strengthLines.forEach { StrengthLineView(it) }
            }
            if (result.freeFormNotes.isNotBlank()) {
                Text(result.freeFormNotes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun WodVariantView(variant: WodVariant) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            variant.label.ifBlank { stringResource(R.string.unnamed_variant) },
            style = MaterialTheme.typography.titleSmall,
        )
        val capStr = variant.timeCapSeconds?.let { stringResource(R.string.time_cap_fmt, it) }
        val meta = buildList {
            if (variant.formatDescription.isNotBlank()) add(variant.formatDescription)
            capStr?.let { add(it) }
        }
        if (meta.isNotEmpty()) {
            Text(meta.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
        variant.blocks.forEachIndexed { i, b -> BlockView(i, b) }
    }
}

@Composable
private fun BlockView(blockIndex: Int, block: ExerciseBlock) {
    Column(Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.block_rounds_fmt, blockIndex + 1, block.rounds),
            style = MaterialTheme.typography.labelMedium,
        )
        block.steps.forEach { step -> StepView(step) }
    }
}

@Composable
private fun StepView(step: ExerciseStep) {
    val repsStr = step.reps?.let { stringResource(R.string.reps_fmt, it) }
    val secStr = step.durationSeconds?.let { stringResource(R.string.seconds_fmt, it) }
    val parts = buildList {
        add(step.name.ifBlank { "—" })
        repsStr?.let { add(it) }
        secStr?.let { add(it) }
        if (step.notes?.isNotBlank() == true) add("(${step.notes})")
    }
    Text(parts.joinToString(" "), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun StrengthLineView(line: StrengthLine) {
    val text = buildString {
        append(line.sets)
        append("×")
        append(line.reps)
        append(" ")
        append(line.exerciseName)
        line.percentOfPb?.let { append(" @ "); append(it); append("% PB") }
        line.notes?.takeIf { it.isNotBlank() }?.let { append(" — "); append(it) }
    }
    Text(text, style = MaterialTheme.typography.bodyMedium)
}
