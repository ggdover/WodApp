package com.ggdover.wodapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.model.ExerciseBlock
import com.ggdover.wodapp.data.model.ExerciseStep
import com.ggdover.wodapp.data.model.StrengthLine

@Composable
fun BlockEditor(
    title: String,
    block: ExerciseBlock,
    onChange: (ExerciseBlock) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove))
            }
        }
        OutlinedTextField(
            value = block.rounds.toString(),
            onValueChange = {
                val n = it.toIntOrNull() ?: 1
                onChange(block.copy(rounds = n.coerceAtLeast(1)))
            },
            label = { Text(stringResource(R.string.rounds)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        block.steps.forEachIndexed { si, step ->
            StepEditor(
                step = step,
                onChange = { new ->
                    onChange(
                        block.copy(
                            steps = block.steps.mapIndexed { i, s -> if (i == si) new else s },
                        ),
                    )
                },
                onRemove = {
                    onChange(block.copy(steps = block.steps.filterIndexed { i, _ -> i != si }))
                },
            )
        }
        FilledTonalButton(
            onClick = { onChange(block.copy(steps = block.steps + ExerciseStep())) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.add_step))
        }
    }
}

@Composable
fun StepEditor(
    step: ExerciseStep,
    onChange: (ExerciseStep) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove))
            }
        }
        OutlinedTextField(
            value = step.name,
            onValueChange = { onChange(step.copy(name = it)) },
            label = { Text(stringResource(R.string.exercise_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = step.reps?.toString().orEmpty(),
                onValueChange = { onChange(step.copy(reps = it.toIntOrNull())) },
                label = { Text(stringResource(R.string.reps_optional)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = step.durationSeconds?.toString().orEmpty(),
                onValueChange = { onChange(step.copy(durationSeconds = it.toIntOrNull())) },
                label = { Text(stringResource(R.string.seconds_optional)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        OutlinedTextField(
            value = step.notes.orEmpty(),
            onValueChange = { onChange(step.copy(notes = it.ifBlank { null })) },
            label = { Text(stringResource(R.string.notes_optional)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun StrengthLineEditor(
    line: StrengthLine,
    onChange: (StrengthLine) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove))
            }
        }
        OutlinedTextField(
            value = line.exerciseName,
            onValueChange = { onChange(line.copy(exerciseName = it)) },
            label = { Text(stringResource(R.string.exercise_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = line.sets.toString(),
                onValueChange = { onChange(line.copy(sets = it.toIntOrNull() ?: line.sets)) },
                label = { Text(stringResource(R.string.sets)) },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = line.reps.toString(),
                onValueChange = { onChange(line.copy(reps = it.toIntOrNull() ?: line.reps)) },
                label = { Text(stringResource(R.string.reps)) },
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = line.percentOfPb?.toString().orEmpty(),
            onValueChange = { onChange(line.copy(percentOfPb = it.toIntOrNull())) },
            label = { Text(stringResource(R.string.percent_pb_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = line.notes.orEmpty(),
            onValueChange = { onChange(line.copy(notes = it.ifBlank { null })) },
            label = { Text(stringResource(R.string.notes_optional)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
