package com.ggdover.wodapp.ui.moments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.model.ExerciseBlock
import com.ggdover.wodapp.data.model.LoggedResult
import com.ggdover.wodapp.data.model.StrengthLine
import com.ggdover.wodapp.data.repository.WodRepository
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.components.BlockEditor
import com.ggdover.wodapp.ui.components.StrengthLineEditor
import com.ggdover.wodapp.ui.util.formatInstant
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentDetailScreen(
    momentId: String,
    onBack: () -> Unit,
) {
    val repo = LocalWodRepository.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var performedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var workoutName by remember { mutableStateOf("") }
    var workoutId by remember { mutableStateOf<String?>(null) }
    var variantLabel by remember { mutableStateOf("") }
    var freeNotes by remember { mutableStateOf("") }

    val warmBlocks = remember { mutableStateListOf<ExerciseBlock>() }
    val wodBlocks = remember { mutableStateListOf<ExerciseBlock>() }
    val strengthLines = remember { mutableStateListOf<StrengthLine>() }

    LaunchedEffect(momentId) {
        val m = repo.getMoment(momentId) ?: return@LaunchedEffect
        performedAt = m.performedAt
        workoutId = m.workoutId
        val w = repo.getWorkout(m.workoutId)
        workoutName = w?.name ?: ""
        val r = WodRepository.parseLoggedResult(m.resultJson)
        variantLabel = r.variantLabel.orEmpty()
        freeNotes = r.freeFormNotes
        warmBlocks.clear()
        warmBlocks.addAll(r.warmupBlocks)
        wodBlocks.clear()
        wodBlocks.addAll(r.wodBlocks)
        strengthLines.clear()
        strengthLines.addAll(r.strengthLines)
    }

    fun save() {
        scope.launch {
            val wid = workoutId ?: return@launch
            val result = LoggedResult(
                variantLabel = variantLabel.ifBlank { null },
                warmupBlocks = warmBlocks.toList(),
                wodBlocks = wodBlocks.toList(),
                strengthLines = strengthLines.toList(),
                freeFormNotes = freeNotes,
            )
            repo.upsertMoment(
                id = momentId,
                workoutId = wid,
                performedAt = performedAt,
                result = result,
            )
            onBack()
        }
    }

    fun pickDateTime() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = performedAt
        DatePickerDialog(
            context,
            { _, y, mo, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, mo)
                cal.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(
                    context,
                    { _, h, mi ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, mi)
                        performedAt = cal.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true,
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    fun fillFromTemplate() {
        scope.launch {
            val wid = workoutId ?: return@launch
            val w = repo.getWorkout(wid) ?: return@launch
            val warm = WodRepository.parseWarmUp(w.warmUpJson)
            val wod = WodRepository.parseWod(w.wodJson)
            val str = WodRepository.parseStrength(w.strengthJson)
            warmBlocks.clear()
            warmBlocks.addAll(
                warm.blocks.map { b ->
                    b.copy(steps = b.steps.map { it.copy() })
                },
            )
            val label = variantLabel.ifBlank { wod.variants.firstOrNull()?.label.orEmpty() }
            val variant = wod.variants.find { it.label == label } ?: wod.variants.firstOrNull()
            wodBlocks.clear()
            wodBlocks.addAll(
                variant?.blocks.orEmpty().map { b ->
                    b.copy(steps = b.steps.map { it.copy() })
                },
            )
            strengthLines.clear()
            strengthLines.addAll(str.lines.map { it.copy() })
            if (variantLabel.isBlank() && variant != null) {
                variantLabel = variant.label
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.moment_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(workoutName, style = MaterialTheme.typography.headlineSmall)
            Text(formatInstant(performedAt), style = MaterialTheme.typography.bodyLarge)
            FilledTonalButton(onClick = { pickDateTime() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.change_date_time))
            }
            OutlinedTextField(
                value = variantLabel,
                onValueChange = { variantLabel = it },
                label = { Text(stringResource(R.string.variant_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = freeNotes,
                onValueChange = { freeNotes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            FilledTonalButton(onClick = { fillFromTemplate() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.fill_from_workout_template))
            }

            Text(stringResource(R.string.logged_warmup), style = MaterialTheme.typography.titleMedium)
            warmBlocks.forEachIndexed { i, block ->
                BlockEditor(
                    title = stringResource(R.string.block_title_fmt, i + 1),
                    block = block,
                    onChange = { warmBlocks[i] = it },
                    onRemove = { warmBlocks.removeAt(i) },
                )
            }
            FilledTonalButton(
                onClick = { warmBlocks.add(ExerciseBlock()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_exercise_block))
            }

            Text(stringResource(R.string.logged_wod), style = MaterialTheme.typography.titleMedium)
            wodBlocks.forEachIndexed { i, block ->
                BlockEditor(
                    title = stringResource(R.string.block_title_fmt, i + 1),
                    block = block,
                    onChange = { wodBlocks[i] = it },
                    onRemove = { wodBlocks.removeAt(i) },
                )
            }
            FilledTonalButton(
                onClick = { wodBlocks.add(ExerciseBlock()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_exercise_block))
            }

            Text(stringResource(R.string.logged_strength), style = MaterialTheme.typography.titleMedium)
            strengthLines.forEachIndexed { i, line ->
                StrengthLineEditor(
                    line = line,
                    onChange = { strengthLines[i] = it },
                    onRemove = { strengthLines.removeAt(i) },
                )
            }
            FilledTonalButton(
                onClick = { strengthLines.add(StrengthLine()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_strength_line))
            }
        }
    }
}
