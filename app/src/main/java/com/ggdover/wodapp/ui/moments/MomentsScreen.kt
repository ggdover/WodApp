package com.ggdover.wodapp.ui.moments

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.model.defaultLoggedResult
import com.ggdover.wodapp.data.repository.WodRepository
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.util.formatInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class MomentsViewMode { List, Calendar }

private val monthTitleFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentsScreen(
    onOpenMoment: (String) -> Unit,
) {
    val repo = LocalWodRepository.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var viewMode by remember { mutableStateOf(MomentsViewMode.List) }
    var menuOpen by remember { mutableStateOf(false) }
    var pickWorkoutOpen by remember { mutableStateOf(false) }
    var importConfirmJson by remember { mutableStateOf<String?>(null) }

    val moments by repo.observeMomentsWithWorkoutNames().collectAsStateWithLifecycle(initialValue = emptyList())
    val workouts by repo.observeWorkouts().collectAsStateWithLifecycle(initialValue = emptyList())

    val workoutDays = remember(moments) {
        val z = ZoneId.systemDefault()
        moments.map { Instant.ofEpochMilli(it.moment.performedAt).atZone(z).toLocalDate() }.toSet()
    }

    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    ins.bufferedReader().readText()
                }
            }
            if (text.isNullOrBlank()) {
                Toast.makeText(context, R.string.import_failed, Toast.LENGTH_SHORT).show()
            } else {
                importConfirmJson = text
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = withContext(Dispatchers.IO) { repo.exportJsonString() }
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }
            Toast.makeText(context, R.string.export_done, Toast.LENGTH_SHORT).show()
        }
    }

    importConfirmJson?.let { json ->
        AlertDialog(
            onDismissRequest = { importConfirmJson = null },
            title = { Text(stringResource(R.string.import_confirm_title)) },
            text = { Text(stringResource(R.string.import_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                repo.importJsonString(json)
                                Toast.makeText(context, R.string.import_done, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, R.string.import_failed, Toast.LENGTH_SHORT).show()
                            }
                            importConfirmJson = null
                        }
                    },
                ) { Text(stringResource(R.string.replace_all)) }
            },
            dismissButton = {
                TextButton(onClick = { importConfirmJson = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (pickWorkoutOpen) {
        AlertDialog(
            onDismissRequest = { pickWorkoutOpen = false },
            title = { Text(stringResource(R.string.pick_workout_title)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(workouts, key = { it.id }) { w ->
                        ListItem(
                            headlineContent = { Text(w.name.ifBlank { stringResource(R.string.untitled_workout) }) },
                            modifier = Modifier.clickable {
                                pickWorkoutOpen = false
                                scope.launch {
                                    val id = WodRepository.newId()
                                    repo.upsertMoment(
                                        id = id,
                                        workoutId = w.id,
                                        performedAt = System.currentTimeMillis(),
                                        result = defaultLoggedResult(),
                                    )
                                    onOpenMoment(id)
                                }
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickWorkoutOpen = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.moments_title)) },
                actions = {
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == MomentsViewMode.List) {
                                MomentsViewMode.Calendar
                            } else {
                                MomentsViewMode.List
                            }
                        },
                    ) {
                        Icon(
                            if (viewMode == MomentsViewMode.List) {
                                Icons.Filled.CalendarMonth
                            } else {
                                Icons.AutoMirrored.Filled.ViewList
                            },
                            contentDescription = stringResource(R.string.toggle_view),
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_json)) },
                                onClick = {
                                    menuOpen = false
                                    exportLauncher.launch("wodapp-backup.json")
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_json)) },
                                onClick = {
                                    menuOpen = false
                                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (workouts.isEmpty()) {
                        Toast.makeText(context, R.string.no_workouts_create_first, Toast.LENGTH_SHORT).show()
                    } else {
                        pickWorkoutOpen = true
                    }
                },
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.log_workout))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (viewMode) {
                MomentsViewMode.List -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        items(moments, key = { it.moment.id }) { row ->
                            ListItem(
                                headlineContent = { Text(row.workoutName) },
                                supportingContent = { Text(formatInstant(row.moment.performedAt)) },
                                modifier = Modifier.clickable { onOpenMoment(row.moment.id) },
                            )
                        }
                    }
                }
                MomentsViewMode.Calendar -> {
                    MonthHeader(
                        yearMonth = yearMonth,
                        onPrev = { yearMonth = yearMonth.minusMonths(1) },
                        onNext = { yearMonth = yearMonth.plusMonths(1) },
                    )
                    MonthGrid(
                        yearMonth = yearMonth,
                        workoutDays = workoutDays,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.calendar_legend),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPrev) { Text("◀") }
        Text(
            yearMonth.format(monthTitleFormatter),
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onNext) { Text("▶") }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    workoutDays: Set<LocalDate>,
) {
    val first = yearMonth.atDay(1)
    val monthLen = yearMonth.lengthOfMonth()
    val offset = (first.dayOfWeek.value + 6) % 7
    val cells = buildList<LocalDate?> {
        repeat(offset) { add(null) }
        for (d in 1..monthLen) {
            add(yearMonth.atDay(d))
        }
        while (size % 7 != 0) {
            add(null)
        }
    }
    val rows = cells.size / 7
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(d, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
        }
        for (r in 0 until rows) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
            ) {
                for (c in 0 until 7) {
                    val date = cells[r * 7 + c]
                    val marked = date != null && workoutDays.contains(date)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                if (marked) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                },
                                shape = MaterialTheme.shapes.small,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            date?.dayOfMonth?.toString().orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
