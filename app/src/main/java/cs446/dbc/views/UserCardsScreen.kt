package cs446.dbc.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cs446.dbc.components.BusinessCard
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.MainActivity
import cs446.dbc.viewmodels.AppViewModel

@Composable
fun UserCardsScreen(appViewModel: AppViewModel) {
    appViewModel.updateScreenTitle("My Cards")
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        LazyColumn(
//        ) {
//            items(4) {
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    BusinessCardContainer()
//                }
//            }
//        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /*TODO: Go to business card creation screen*/ }
        ) {
            Icon(
                imageVector = Icons.Outlined.AddCircleOutline,
                contentDescription = "Add Cards"
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun UserCardsScreenPreview() {
    val appViewModel: AppViewModel = viewModel()
    UserCardsScreen(appViewModel)
}