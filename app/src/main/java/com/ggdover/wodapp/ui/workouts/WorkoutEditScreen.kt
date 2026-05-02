package com.ggdover.wodapp.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.model.ExerciseBlock
import com.ggdover.wodapp.data.model.StrengthLine
import com.ggdover.wodapp.data.model.StrengthSection
import com.ggdover.wodapp.data.model.WarmUpSection
import com.ggdover.wodapp.data.model.WodSection
import com.ggdover.wodapp.data.model.WodVariant
import com.ggdover.wodapp.data.repository.WodRepository
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.components.BlockEditor
import com.ggdover.wodapp.ui.components.StrengthLineEditor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    workoutId: String?,
    onDone: () -> Unit,
) {
    val repo = LocalWodRepository.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    val warmBlocks = remember { mutableStateListOf<ExerciseBlock>() }
    val wodVariants = remember { mutableStateListOf<WodVariant>() }
    val strengthLines = remember { mutableStateListOf<StrengthLine>() }

    LaunchedEffect(workoutId) {
        if (workoutId == null) return@LaunchedEffect
        val w = repo.getWorkout(workoutId) ?: return@LaunchedEffect
        name = w.name
        warmBlocks.clear()
        warmBlocks.addAll(WodRepository.parseWarmUp(w.warmUpJson).blocks)
        wodVariants.clear()
        wodVariants.addAll(WodRepository.parseWod(w.wodJson).variants)
        strengthLines.clear()
        strengthLines.addAll(WodRepository.parseStrength(w.strengthJson).lines)
    }

    fun save() {
        scope.launch {
            val id = workoutId ?: WodRepository.newId()
            repo.upsertWorkout(
                id = id,
                name = name,
                warmUp = WarmUpSection(blocks = warmBlocks.toList()),
                wod = WodSection(variants = wodVariants.toList()),
                strength = StrengthSection(lines = strengthLines.toList()),
            )
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (workoutId == null) {
                            stringResource(R.string.new_workout)
                        } else {
                            stringResource(R.string.edit_workout)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    Button(onClick = { save() }) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.workout_name)) },
                singleLine = true,
            )

            Text(stringResource(R.string.section_warm_up), style = MaterialTheme.typography.titleMedium)
            warmBlocks.forEachIndexed { bi, block ->
                BlockEditor(
                    title = stringResource(R.string.block_title_fmt, bi + 1),
                    block = block,
                    onChange = { warmBlocks[bi] = it },
                    onRemove = { warmBlocks.removeAt(bi) },
                )
            }
            FilledTonalButton(
                onClick = { warmBlocks.add(ExerciseBlock()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(stringResource(R.string.add_exercise_block))
            }

            Text(stringResource(R.string.section_wod), style = MaterialTheme.typography.titleMedium)
            wodVariants.forEachIndexed { vi, variant ->
                WodVariantEditor(
                    index = vi,
                    variant = variant,
                    onChange = { wodVariants[vi] = it },
                    onRemove = { wodVariants.removeAt(vi) },
                )
            }
            FilledTonalButton(
                onClick = {
                    wodVariants.add(
                        WodVariant(label = "Variant ${wodVariants.size + 1}"),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(stringResource(R.string.add_wod_variant))
            }

            Text(stringResource(R.string.section_strength), style = MaterialTheme.typography.titleMedium)
            strengthLines.forEachIndexed { li, line ->
                StrengthLineEditor(
                    line = line,
                    onChange = { strengthLines[li] = it },
                    onRemove = { strengthLines.removeAt(li) },
                )
            }
            FilledTonalButton(
                onClick = { strengthLines.add(StrengthLine()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(stringResource(R.string.add_strength_line))
            }
        }
    }
}

@Composable
private fun WodVariantEditor(
    index: Int,
    variant: WodVariant,
    onChange: (WodVariant) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.wod_variant_title_fmt, index + 1), style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove))
            }
        }
        OutlinedTextField(
            value = variant.label,
            onValueChange = { onChange(variant.copy(label = it)) },
            label = { Text(stringResource(R.string.variant_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = variant.formatDescription,
            onValueChange = { onChange(variant.copy(formatDescription = it)) },
            label = { Text(stringResource(R.string.format_emom_amrap)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = variant.timeCapSeconds?.toString().orEmpty(),
            onValueChange = { onChange(variant.copy(timeCapSeconds = it.toIntOrNull())) },
            label = { Text(stringResource(R.string.time_cap_seconds_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        variant.blocks.forEachIndexed { bi, block ->
            BlockEditor(
                title = stringResource(R.string.block_title_fmt, bi + 1),
                block = block,
                onChange = { nb ->
                    onChange(variant.copy(blocks = variant.blocks.mapIndexed { i, b -> if (i == bi) nb else b }))
                },
                onRemove = {
                    onChange(variant.copy(blocks = variant.blocks.filterIndexed { i, _ -> i != bi }))
                },
            )
        }
        FilledTonalButton(
            onClick = { onChange(variant.copy(blocks = variant.blocks + ExerciseBlock())) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.add_exercise_block))
        }
    }
}
