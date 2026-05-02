package com.ggdover.wodapp.ui.workouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ggdover.wodapp.R
import com.ggdover.wodapp.data.repository.WodRepository
import com.ggdover.wodapp.data.model.defaultLoggedResult
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.util.formatInstant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    onLogResult: (String) -> Unit,
    onOpenMoment: (String) -> Unit,
) {
    val repo = LocalWodRepository.current
    val workout by repo.observeWorkout(workoutId).collectAsStateWithLifecycle(initialValue = null)
    val moments by repo.observeMomentsForWorkout(workoutId).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    val warm = workout?.let { WodRepository.parseWarmUp(it.warmUpJson) }
    val wod = workout?.let { WodRepository.parseWod(it.wodJson) }
    val strength = workout?.let { WodRepository.parseStrength(it.strengthJson) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.name ?: stringResource(R.string.loading)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEdit,
                        enabled = workout != null,
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                    }
                },
            )
        },
    ) { padding ->
        if (workout == null || warm == null || wod == null || strength == null) {
            Text(stringResource(R.string.loading), modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(
                    onClick = {
                        scope.launch {
                            val id = WodRepository.newId()
                            repo.upsertMoment(
                                id = id,
                                workoutId = workoutId,
                                performedAt = System.currentTimeMillis(),
                                result = defaultLoggedResult(),
                            )
                            onLogResult(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.log_this_workout))
                }
            }
            item { WarmUpSectionCard(warm) }
            item { WodSectionCard(wod) }
            item { StrengthSectionCard(strength) }
            item {
                Text(stringResource(R.string.past_results), style = MaterialTheme.typography.titleLarge)
            }
            if (moments.isEmpty()) {
                item {
                    Text(stringResource(R.string.no_results_yet), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(moments, key = { it.id }) { m ->
                    val result = WodRepository.parseLoggedResult(m.resultJson)
                    ListItem(
                        headlineContent = { Text(formatInstant(m.performedAt)) },
                        supportingContent = {
                            Text(
                                result.variantLabel ?: stringResource(R.string.no_variant_selected),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { onOpenMoment(m.id) },
                    )
                }
            }
        }
    }
}
