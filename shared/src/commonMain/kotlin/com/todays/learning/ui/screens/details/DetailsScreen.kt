@file:OptIn(KoinExperimentalAPI::class)

package com.todays.learning.ui.screens.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.todays.learning.domain.models.Subject
import com.todays.learning.domain.utils.Constants.GEMINI_API_KEY
import com.todays.learning.domain.utils.Constants.OPENAI_API_KEY
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navigator: NavHostController,
    viewModel: DetailsViewModel = koinViewModel(),
    subject: String
) {
    LaunchedEffect(Unit) {
        viewModel.getSubjectDetails(subject)
    }

    val subjectDetailsState by viewModel.subjectDetailsState.collectAsState()
    val selectedTabState by viewModel.selectedTabState.collectAsState()
    val gptSummaryState by viewModel.gptSummaryState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subject,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (subjectDetailsState.isLoading && subjectDetailsState.subjectDetails == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            if (subjectDetailsState.error != null) {
                Text(
                    text = "Error: ${subjectDetailsState.error}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                return@Box
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                SubjectTabs(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabState) {
                    2 -> { // "In short"
                        when {
                            gptSummaryState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }

                            gptSummaryState.error != null -> {
                                Text(
                                    text = "Error: ${gptSummaryState.error}",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            else -> {
                                Text(
                                    text = gptSummaryState.summary ?: "No summary available",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    textAlign = TextAlign.Center,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = setLayoutData(selectedTabState, subjectDetailsState.subjectDetails),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectTabs(viewModel: DetailsViewModel) {
    val tabs = listOf("Learning", "Homework", "In short Gemini", "In short Gpt")
    val selectedTab by viewModel.selectedTabState.collectAsState()
    val subjectDetails by viewModel.subjectDetailsState.collectAsState()

    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(tabs) { index, title ->
            SubjectCard(
                subjectTab = title,
                setSelected = index == selectedTab
            ) {
                viewModel.updateClick(index)

                if (index == 2 && viewModel.gptSummaryState.value.summary == null) {
                    viewModel.summarizeLearningUsingGemini(
                        subjectDetails.subjectDetails?.learning.orEmpty(),
                        apiKey = GEMINI_API_KEY
                    )
                }

                if (index == 3 && viewModel.gptSummaryState.value.summary == null) {
                    viewModel.summarizeLearningUsingGpt(
                        subjectDetails.subjectDetails?.learning.orEmpty(),
                        apiKey = OPENAI_API_KEY
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subjectTab: String,
    setSelected: Boolean,
    isSelected: (String?) -> Unit
) {
    val backgroundColor = if (setSelected) MaterialTheme.colorScheme.secondaryContainer else Color.White
    val textColor = if (setSelected) Color.Black else Color.Gray

    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, backgroundColor),
        onClick = { isSelected(subjectTab) }
    ) {
        Text(
            text = subjectTab,
            modifier = Modifier.padding(12.dp),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun setLayoutData(
    selectedTab: Int,
    subject: Subject?
): String {
    return when (selectedTab) {
        0 -> subject?.learning ?: ""
        1 -> subject?.homework ?: ""
        2 -> subject?.keyPoints ?: ""
        else -> subject?.learning ?: ""
    }
}


