package com.example.bingebox.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bingebox.core.content.ContentDetailsResponse
import com.example.bingebox.core.content.ContentSummaryResponse
import com.example.bingebox.core.content.StreamingSourceResponse
import com.example.bingebox.presentation.auth.AuthViewModel
import com.example.bingebox.presentation.navigation.BingeBoxRoute
import com.example.bingebox.presentation.viewmodel.ContentDetailViewModel
import com.example.bingebox.presentation.viewmodel.HomeViewModel
import com.example.bingebox.presentation.viewmodel.SearchViewModel
import com.example.bingebox.presentation.viewmodel.NavigationViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    navigationViewModel: NavigationViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject(),
    searchViewModel: SearchViewModel = koinInject(),
    detailViewModel: ContentDetailViewModel = koinInject(),
) {
    val navigationState = navigationViewModel.uiState
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            Modifier
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
    ) {
        ShellTopBar(
            canGoBack = navigationState.canGoBack,
            onBack = navigationViewModel::goBack,
            onHome = navigationViewModel::showHome,
            onSearch = navigationViewModel::showSearch,
            onSignOut = { scope.launch { authViewModel.logout() } },
        )
        Spacer(Modifier.height(16.dp))
        when (val route = navigationState.currentRoute) {
            BingeBoxRoute.Home ->
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenDetails = navigationViewModel::showDetails,
                )
            BingeBoxRoute.Search ->
                SearchScreen(
                    viewModel = searchViewModel,
                    onOpenDetails = navigationViewModel::showDetails,
                )
            is BingeBoxRoute.Detail ->
                ContentDetailScreen(
                    titleId = route.titleID,
                    viewModel = detailViewModel,
                )
            else -> PlaceholderScreen("This section is scheduled for a later phase.")
        }
    }
}

@Composable
private fun ShellTopBar(
    canGoBack: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onSignOut: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (canGoBack) {
            TextButton(onClick = onBack) { Text("Back") }
        }
        Button(onClick = onHome) { Text("Home") }
        Button(onClick = onSearch) { Text("Search") }
        TextButton(onClick = onSignOut) { Text("Sign out") }
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDetails: (Long) -> Unit,
) {
    val state = viewModel.uiState
    LaunchedEffect(Unit) { viewModel.loadHomeRows() }
    ScreenStateFrame(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        isEmpty = state.isEmpty,
        emptyMessage = "No content is available yet.",
        onRetry = { viewModel.loadHomeRows() },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(state.rows) { row ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp),
                    ) {
                        items(row.items) { item ->
                            ContentSummaryCard(item, onOpenDetails)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenDetails: (Long) -> Unit,
) {
    val state = viewModel.uiState
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search") },
            singleLine = true,
            keyboardActions = KeyboardActions(onSearch = { scope.launch { viewModel.search() } }),
        )
        Button(
            onClick = { scope.launch { viewModel.search() } },
            enabled = !state.isLoading,
        ) {
            Text("Search")
        }
        ScreenStateFrame(
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            isEmpty = state.isSearchResEmpty,
            emptyMessage = "No matches found.",
            onRetry = { viewModel.search() },
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.results) { item ->
                    WideContentSummaryCard(item, onOpenDetails)
                }
            }
        }
    }
}

@Composable
fun ContentDetailScreen(
    titleId: Long,
    viewModel: ContentDetailViewModel,
) {
    val state = viewModel.uiState
    val scope = rememberCoroutineScope()
    LaunchedEffect(titleId) { viewModel.openTitle(titleId) }
    ScreenStateFrame(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        isEmpty = state.details == null && !state.isLoading && state.errorMessage == null,
        emptyMessage = "Details are unavailable.",
        onRetry = { viewModel.openTitle(titleId) },
    ) {
        val details = state.details ?: return@ScreenStateFrame
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 720.dp
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    DetailBody(details, Modifier.weight(1f))
                    SourceList(state.sources, Modifier.weight(1f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { DetailBody(details, Modifier.fillMaxWidth()) }
                    item { SourceList(state.sources, Modifier.fillMaxWidth()) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { scope.launch { viewModel.startTrailer() } },
            enabled = state.hasTrailer && !state.isRecordingTrailer,
        ) {
            Text(if (state.hasTrailer) "Open trailer" else "Trailer unavailable")
        }
        state.trailerMessage?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
    }
}

@Composable
private fun DetailBody(
    details: ContentDetailsResponse,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(details.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(listOfNotNull(details.type.displayName(), details.year?.toString()).joinToString(" / "))
        details.overview?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }
        details.runtimeMinutes?.let { Text("$it minutes") }
    }
}

@Composable
private fun SourceList(
    sources: List<StreamingSourceResponse>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Where to watch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (sources.isEmpty()) {
            Text("No regional sources are available.")
        } else {
            sources.forEach { source ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(source.name, fontWeight = FontWeight.SemiBold)
                        Text(source.type)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentSummaryCard(
    item: ContentSummaryResponse,
    onOpenDetails: (Long) -> Unit,
) {
    Card(
        onClick = { onOpenDetails(item.id) },
        modifier = Modifier.widthIn(min = 180.dp, max = 220.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(item.type.displayName(), item.year?.toString()).joinToString(" / "))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WideContentSummaryCard(
    item: ContentSummaryResponse,
    onOpenDetails: (Long) -> Unit,
) {
    Card(
        onClick = { onOpenDetails(item.id) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(item.type.displayName(), item.year?.toString()).joinToString(" / "))
        }
    }
}

@Composable
private fun ScreenStateFrame(
    isLoading: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    emptyMessage: String,
    onRetry: suspend () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    when {
        isLoading -> LoadingScreen()
        errorMessage != null ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Button(onClick = { scope.launch { onRetry() } }) { Text("Retry") }
            }
        isEmpty -> Text(emptyMessage)
        else -> content()
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text("Loading")
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    Text(message)
}

private fun String.displayName(): String =
    when (this) {
        "tv_series" -> "Series"
        "movie" -> "Movie"
        else -> replaceFirstChar { it.uppercase() }
    }