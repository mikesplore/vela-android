package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.AlertDelivery
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.AlertsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.history == null -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error ?: "Failed to load alerts", color = colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            IconButton(onClick = viewModel::refresh) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    SectionHeader(title = "Alert history")
                                    val summary = state.history
                                    Text(
                                        text = buildString {
                                            append("${summary?.todayCount ?: 0} today")
                                            summary?.totalStored?.let { append(" · $it stored") }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        val alerts = state.history?.alerts.orEmpty()
                        if (alerts.isEmpty()) {
                            item {
                                Text(
                                    "No alerts in the last 7 days",
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 24.dp)
                                )
                            }
                        } else {
                            items(alerts, key = { it.id }) { alert ->
                                AlertHistoryRow(alert)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryRow(alert: AlertDelivery) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = alert.title.ifBlank { alert.alertKind },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = alert.status == "sent",
                onClick = {},
                enabled = false,
                label = { Text(alert.status.ifBlank { "unknown" }, fontSize = 10.sp) }
            )
        }

        if (alert.body.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = alert.body,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = false,
                onClick = {},
                enabled = false,
                label = { Text(alert.channel.ifBlank { "—" }, fontSize = 10.sp) }
            )
            FilterChip(
                selected = false,
                onClick = {},
                enabled = false,
                label = { Text(alert.alertKind.ifBlank { "—" }, fontSize = 10.sp) }
            )
        }

        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = alert.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (alert.pushDelivered != null && alert.pushDelivered > 0) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(
                    " ${alert.pushDelivered}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            if (!alert.emailTo.isNullOrBlank()) {
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }

        alert.resource?.let { resource ->
            alert.value?.let { value ->
                Text(
                    "$resource ${String.format("%.1f", value)}% (threshold ${alert.threshold?.let { String.format("%.1f", it) } ?: "—"}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        alert.emailProviderId?.let { id ->
            Text(
                "Resend: $id",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
