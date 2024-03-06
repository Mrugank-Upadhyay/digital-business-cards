package cs446.dbc

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView.SavedState
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.views.SharedCardsScreen
import cs446.dbc.views.UserCardsScreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appActivity = this)
        }
    }


    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//    @OptIn(ExperimentalLifeCycleComposeApi::class)
    @Composable
    private fun App(appViewModel: AppViewModel = viewModel(), appActivity: AppCompatActivity) {
        val navController = rememberNavController()
        val homeUiState by appViewModel.uiState.collectAsStateWithLifecycle()
        val cardSavedStateHandle: SavedStateHandle = SavedStateHandle()
        val sharedCardSavedStateHandle: SavedStateHandle = SavedStateHandle()
        // Card View Model for user's personal cards
        val cardViewModel: BusinessCardViewModel = viewModel() {
            BusinessCardViewModel(savedStateHandle = cardSavedStateHandle)
        }
        // Card View Model for shared cards
        // TODO: There's an issue, since we're making 2 view models, they are being shared together
        //  because they have the same owner. This needs an immediate fix!!!!
        val sharedCardViewModel: BusinessCardViewModel = viewModel() {
            BusinessCardViewModel(savedStateHandle = sharedCardSavedStateHandle)
        }

        // TODO: remove after demo, we'll use this to start in the SharedCards Screen
        val sharedCardsList = listOf(
            BusinessCardModel(
                id = UUID.randomUUID(),
                front = "A",
                back = "B",
                favorite = false,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID(),
                front = "C",
                back = "D",
                favorite = true,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID(),
                front = "E",
                back = "F",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "Hanz Zimmer",
                        FieldType.TEXT
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID(),
                front = "G",
                back = "H",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Phone Number",
                        "416-111-2222",
                        FieldType.PHONE_NUMBER
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID(),
                front = "G",
                back = "H",
                favorite = false,
                template=TemplateType.TEMPLATE_1,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "John Doe",
                        FieldType.TEXT,
                    )
                ),
                cardType = CardType.SHARED
            ),
        )

        sharedCardsList.forEach { card ->
            sharedCardViewModel.performAction(
                BusinessCardAction.PopulateCard(
                    front = card.front,
                    back = card.back,
                    favorite = card.favorite,
                    fields = card.fields,
                    cardType = card.cardType
                )
            )
        }

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                AnimatedContent(
                                    targetState = homeUiState.screenTitle,
                                    label = "TopBarTitle",
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                ) {
                                    Text(it)
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Settings.route) },
                                ) {
                                    Icon(Icons.Outlined.Settings, "Settings")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(navController)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        appViewModel.updateScreenTitle("Saved Cards")
                        SharedCardsScreen(appViewModel, sharedCardViewModel, sharedCardsList)
                        NavHost(
                            navController,
                            startDestination = Screen.Home.route,
                        ) {
                            composable(Screen.Home.route) {
                                appViewModel.updateScreenTitle("Saved Cards") // TODO: Replace with SavedCardsScreen
                                SharedCardsScreen(appViewModel, sharedCardViewModel, sharedCardsList)
                            }
                            composable(Screen.UserCards.route) {
                                // TODO: Remove the example list after
                                UserCardsScreen(appViewModel, cardViewModel, listOf(
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "A",
                                        back = "B",
                                        favorite = false,
                                        fields = mutableListOf(),
                                        cardType = CardType.SHARED
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "C",
                                        back = "D",
                                        favorite = true,
                                        fields = mutableListOf(),
                                        cardType = CardType.SHARED
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "E",
                                        back = "F",
                                        favorite = false,
                                        fields = mutableListOf(
                                            Field(
                                                "Full Name",
                                                "Hanz Zimmer",
                                                FieldType.TEXT
                                            )
                                        ),
                                        cardType = CardType.SHARED
                                    ),
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "G",
                                        back = "H",
                                        favorite = false,
                                        fields = mutableListOf(
                                            Field(
                                                "Phone Number",
                                                "416-111-2222",
                                                FieldType.PHONE_NUMBER
                                            )
                                        ),
                                        cardType = CardType.SHARED
                                    ),
<<<<<<< Updated upstream
                                    BusinessCardModel(
                                        id = UUID.randomUUID(),
                                        front = "G",
                                        back = "H",
                                        favorite = false,
                                        template=TemplateType.TEMPLATE_1,
                                        fields = mutableListOf(
                                            Field(
                                                "Full Name",
                                                "John Doe",
                                                FieldType.TEXT,
                                            )
                                        ),
                                        cardType = CardType.SHARED
                                    ),
=======
//                                    BusinessCardModel(
//                                        id = UUID.randomUUID(),
//                                        front = "G",
//                                        back = "H",
//                                        favorite = false,
//                                        template=TemplateType.TEMPLATE_1,
//                                        fields = mutableListOf(
//                                            Field(
//                                                "Full Name",
//                                                "John Doe",
//                                                FieldType.TEXT,
//                                            )
//                                        )
//                                    ),
>>>>>>> Stashed changes
                                ))
                            }
                            composable(Screen.Settings.route) {
                                appViewModel.updateScreenTitle("Settings") // TODO: Replace with SettingsScreen
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        @Composable
        fun NavButton(screen: Screen, icon: ImageVector, description: String) {
            val routeSelected = navBackStackEntry?.destination?.route == screen.route
//            val colors = if (navBackStackEntry?.destination?.route == screen.route)
//                IconButtonDefaults.i
//                else
            IconToggleButton(
                checked = routeSelected,
                onCheckedChange = { navController.navigate(screen.route) },
            ) {
                Icon(icon, description)
            }
        }
        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
            actions = {
                NavButton(Screen.Home, Icons.Outlined.People,  "Saved Cards")
                NavButton(Screen.UserCards, Icons.Outlined.Person, "My Cards")
            },
            // TODO: Make sure to add parameters that we don't directly make (e.g. CardType)
            floatingActionButton = {
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.UserCards.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = { /*TODO: Go to business card creation screen*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Cards"
                        )
                    }
                }
            }
        )
    }

    sealed class Screen(val route: String) {
        object UserCards : Screen("my-cards")
        object Home : Screen("saved-cards")
        object Settings : Screen("settings")
    }
}