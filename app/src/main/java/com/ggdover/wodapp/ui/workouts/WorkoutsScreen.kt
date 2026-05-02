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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ggdover.wodapp.R
import com.ggdover.wodapp.ui.LocalWodRepository
import com.ggdover.wodapp.ui.util.formatDateOnly

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onOpenWorkout: (String) -> Unit,
    onCreateWorkout: () -> Unit,
) {
    val repo = LocalWodRepository.current
    var query by remember { mutableStateOf("") }
    val workouts by repo.workoutsMatchingQuery(query).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.workouts_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateWorkout) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_workout))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_workouts_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(workouts, key = { it.id }) { w ->
                    ListItem(
                        headlineContent = { Text(w.name.ifBlank { stringResource(R.string.untitled_workout) }) },
                        supportingContent = {
                            Text(
                                stringResource(R.string.updated_label, formatDateOnly(w.updatedAt)),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        modifier = Modifier.clickable { onOpenWorkout(w.id) },
                    )
                }
            }
        }
    }
}
