package com.ggdover.wodapp.ui.personalbests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.local.entity.PersonalBestEntity
import com.ggdover.wodapp.data.repository.WodRepository
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.util.formatDateOnly
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalBestsScreen() {
    val repo = LocalWodRepository.current
    val scope = rememberCoroutineScope()
    val pbs by repo.observePersonalBests().collectAsStateWithLifecycle(initialValue = emptyList())

    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        AddPersonalBestDialog(
            onDismiss = { showAdd = false },
            onSave = { entity ->
                scope.launch {
                    repo.upsertPersonalBest(entity)
                    showAdd = false
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.pbs_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_pb))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(pbs, key = { it.id }) { pb ->
                ListItem(
                    headlineContent = { Text(pb.exerciseName.ifBlank { "—" }) },
                    supportingContent = {
                        Text(pbSummary(pb), style = MaterialTheme.typography.bodyMedium)
                    },
                )
            }
        }
    }
}

@Composable
private fun pbSummary(pb: PersonalBestEntity): String {
    val repsStr = pb.reps?.let { stringResource(R.string.pb_reps_fmt, it) }
    val weightStr = pb.weightKg?.let { stringResource(R.string.pb_weight_fmt, it) }
    val bwLabel = if (pb.isBodyweight) {
        stringResource(R.string.bodyweight_yes)
    } else {
        stringResource(R.string.bodyweight_no)
    }
    val parts = buildList {
        repsStr?.let { add(it) }
        weightStr?.let { add(it) }
        add(bwLabel)
        pb.extraNotes?.takeIf { it.isNotBlank() }?.let { add(it) }
        add(formatDateOnly(pb.achievedAt))
    }
    return parts.joinToString(" · ")
}

@Composable
private fun AddPersonalBestDialog(
    onDismiss: () -> Unit,
    onSave: (PersonalBestEntity) -> Unit,
) {
    var exercise by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bodyweight by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_pb)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = exercise,
                    onValueChange = { exercise = it },
                    label = { Text(stringResource(R.string.exercise_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.reps_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight_kg_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.bodyweight)) },
                    leadingContent = {
                        Checkbox(
                            checked = bodyweight,
                            onCheckedChange = { bodyweight = it },
                        )
                    },
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        PersonalBestEntity(
                            id = WodRepository.newId(),
                            exerciseName = exercise.trim(),
                            reps = reps.toIntOrNull(),
                            weightKg = weight.toDoubleOrNull(),
                            isBodyweight = bodyweight,
                            extraNotes = notes.ifBlank { null },
                            achievedAt = System.currentTimeMillis(),
                        ),
                    )
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
