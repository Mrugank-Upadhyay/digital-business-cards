package cs446.dbc.views

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel

@Composable
fun UserCardsScreen(appViewModel: AppViewModel, cardViewModel: BusinessCardViewModel, origCardList: List<BusinessCardModel>) {
    appViewModel.updateScreenTitle("My Cards")
    val cards by cardViewModel.businessCards.collectAsStateWithLifecycle()
    // TODO: Remove after, we're just temporarily add cards to mock them for the demo
    /* TODO: This may work for saved preferences, but it'll be more complicated since we can delete cards
       and do so while switching context to another screen (so we can't just check if the
       businessCards list is empty)
     */
    LaunchedEffect(key1 = "load_examples") {
        if (cards.size < 1) {
            origCardList.forEach { card ->
                cardViewModel.performAction(
                    BusinessCardAction.PopulateCard(
                        front = card.front,
                        back = card.back,
                        favorite = card.favorite,
                        fields = card.fields
                    )
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(cards) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(card, cardViewModel::performAction)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun UserCardsScreenPreview() {
    val appViewModel: AppViewModel = viewModel()
    val cardList: List<BusinessCardModel> = listOf()
    val cardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle())
    }
    UserCardsScreen(appViewModel, cardViewModel, cardList)
}