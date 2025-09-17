@file:OptIn(KoinExperimentalAPI::class)

package com.todays.learning.ui.screens.me

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todays.learning.ui.components.appbars.AppBar
import com.kmpalette.loader.rememberNetworkLoader
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import todayslearning.shared.generated.resources.Res
import todayslearning.shared.generated.resources.avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    mainPaddingValues: PaddingValues,
    viewModel: MeViewModel = koinViewModel<MeViewModel>(),
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.getSubjectDetails(subject = "")
    }

    val networkLoader = rememberNetworkLoader(httpClient = koinInject())
    val subjectDetailsState by viewModel.subjectDetailsState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainPaddingValues),
        topBar = { AppBar("My Profile") }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (subjectDetailsState.isLoading && subjectDetailsState.subjectDetails == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!subjectDetailsState.error.isNullOrEmpty()) {
                /* Text(
                     modifier = Modifier.align(Alignment.Center).padding(20.dp),
                     text = "Error:\n${subjectDetailsState.error}",
                     textAlign = TextAlign.Center
                 )*/
                showUserInfo()
            } else {
                showUserInfo()
            }
        }
    }
}

@Composable
private fun showUserInfo() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(30.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Image(
            painter = painterResource(Res.drawable.avatar),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,            // crop the image if it's not a square
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)                       // clip to the circle shape
                .border(2.dp, Color.Gray, CircleShape)   // add a border (optional)
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            text = "Vedan Sandeep Kasar",
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Normal,
            fontSize = 20.sp,
            color = DarkGray
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            text = "Std: III",
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Normal,
            fontSize = 20.sp,
            color = DarkGray
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            text = "Division: A",
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Normal,
            fontSize = 20.sp,
            color = DarkGray
        )
    }
}


