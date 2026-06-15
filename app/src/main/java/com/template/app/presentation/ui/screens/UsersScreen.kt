package com.template.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.User
import com.template.app.presentation.viewmodel.UsersEvent
import com.template.app.presentation.viewmodel.UsersUiState
import com.template.app.presentation.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UsersEvent.NavigateToDetail -> onNavigateToDetail(event.userId)
                is UsersEvent.ShowSnackbar     -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showAddDialog) {
        UserDialog(
            title = "Add User",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email ->
                viewModel.addUser(name, email)
                showAddDialog = false
            }
        )
    }

    userToEdit?.let { user ->
        UserDialog(
            title = "Edit User",
            initialName = user.displayName,
            initialEmail = user.email,
            onDismiss = { userToEdit = null },
            onConfirm = { name, email ->
                viewModel.editUser(user.id, name, email)
                userToEdit = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Users") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshUsers() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UsersUiState.Loading -> {
                    if (!isRefreshing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UsersUiState.Success -> UserList(
                    users = state.users,
                    onUserClick = { viewModel.onUserClicked(it.id) },
                    onEditClick = { userToEdit = it },
                    onDeleteClick = { viewModel.deleteUser(it.id) }
                )

                is UsersUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadUsers() }
                )

                is UsersUiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<User>, 
    onUserClick: (User) -> Unit,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users, key = { it.id }) { user ->
            ListItem(
                headlineContent = { Text(user.displayName) },
                supportingContent = { Text(user.email) },
                modifier = Modifier.clickable { onUserClick(user) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEditClick(user) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDeleteClick(user) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun UserDialog(
    title: String,
    initialName: String = "",
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email) },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(userId: String, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("User Detail") },
            navigationIcon = { IconButton(onBack) { Text("←") } }
        )
    }) { padding ->
        Box(Modifier.padding(padding)) { Text("Detail for user: $userId") }
    }
}
