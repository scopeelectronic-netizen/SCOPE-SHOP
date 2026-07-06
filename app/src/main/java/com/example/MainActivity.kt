package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: ScopeDatabase
    private lateinit var repository: ScopeRepository
    private lateinit var viewModel: ScopeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            ScopeDatabase::class.java,
            "scope_database"
        ).fallbackToDestructiveMigration().build()

        repository = ScopeRepository(database)

        // Initialize ViewModel
        val factory = ScopeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ScopeViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainContainer(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(viewModel: ScopeViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    // Count total quantities in cart
    val cartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_company_logo),
                            contentDescription = "Scope Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Text(
                            text = "SCOPE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = "ELECTRONICS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Menu Action */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Options Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Shopping cart icon with dynamic badge
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickableSingle { viewModel.selectTab(AppTab.CART) }
                            .testTag("top_bar_cart_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { viewModel.selectTab(AppTab.CART) }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Shopping Basket",
                                tint = if (currentTab == AppTab.CART) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd)
                                    .testTag("cart_badge"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_dashboard")
                )

                // Shop Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SHOP,
                    onClick = { viewModel.selectTab(AppTab.SHOP) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Shop"
                        )
                    },
                    label = { Text("Shop", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_shop")
                )

                // Rentals Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.RENTALS,
                    onClick = { viewModel.selectTab(AppTab.RENTALS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Rentals"
                        )
                    },
                    label = { Text("Rentals", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_rentals")
                )

                // Services Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SERVICES,
                    onClick = { viewModel.selectTab(AppTab.SERVICES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = "Services"
                        )
                    },
                    label = { Text("Services", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_services")
                )

                // Cart / Basket Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CART,
                    onClick = { viewModel.selectTab(AppTab.CART) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Basket"
                        )
                    },
                    label = { Text("Basket", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_basket")
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            label = "tab_crossfade",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.selectTab(it) }
                )
                AppTab.SHOP -> ShopScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.selectTab(it) }
                )
                AppTab.RENTALS -> RentalsScreen(
                    viewModel = viewModel
                )
                AppTab.SERVICES -> ServicesScreen(
                    viewModel = viewModel
                )
                AppTab.CART -> CartScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

// Helper to handle simple click modifiers on non-button items safely
fun Modifier.clickableSingle(onClick: () -> Unit): Modifier = this.clickable { onClick() }
