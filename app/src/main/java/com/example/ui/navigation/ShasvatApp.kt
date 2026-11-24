package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.UserRole
import com.example.data.repository.ShasvatRepository
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.GstInvoiceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrdersListScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.WholesaleScreen
import com.example.ui.screens.WishlistScreen
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.CheckoutAndAccountViewModel
import com.example.viewmodel.StorefrontViewModel
import kotlinx.coroutines.flow.merge

object Routes {
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val PRODUCTS = "products"
    const val WISHLIST = "wishlist"
    const val CART = "cart"
    const val ACCOUNT = "account"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order/{orderId}"
    const val GST_INVOICE = "invoice/{orderId}"
    const val WHOLESALE = "wholesale"
    const val NOTIFICATIONS = "notifications"
    const val ADMIN = "admin"

    fun productDetail(productId: String) = "product/$productId"
    fun orderDetail(orderId: String) = "order/$orderId"
    fun gstInvoice(orderId: String) = "invoice/$orderId"
}

private data class TopNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val topNavTabs = listOf(
    TopNavTab(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TopNavTab(Routes.CATEGORIES, "Categories", Icons.Filled.Category, Icons.Outlined.Category),
    TopNavTab(Routes.PRODUCTS, "Products", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    TopNavTab(Routes.WISHLIST, "Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TopNavTab(Routes.CART, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    TopNavTab(Routes.ACCOUNT, "Account", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun ShasvatApp() {
    val context = LocalContext.current
    val repository = remember { ShasvatRepository.getInstance(context) }

    val storefrontVm: StorefrontViewModel = viewModel(factory = StorefrontViewModel.factory(repository))
    val accountVm: CheckoutAndAccountViewModel = viewModel(factory = CheckoutAndAccountViewModel.factory(repository))
    val adminVm: AdminViewModel = viewModel(factory = AdminViewModel.factory(repository))

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        merge(storefrontVm.uiMessages, accountVm.messages, adminVm.messages).collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val categories by storefrontVm.categories.collectAsStateWithLifecycle()
    val allProducts by storefrontVm.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by storefrontVm.filteredProducts.collectAsStateWithLifecycle()
    val filterState by storefrontVm.filterState.collectAsStateWithLifecycle()
    val availableBrands by storefrontVm.availableBrands.collectAsStateWithLifecycle()
    val allReviews by storefrontVm.allReviews.collectAsStateWithLifecycle()
    val offers by storefrontVm.offers.collectAsStateWithLifecycle()
    val wishlistProducts by storefrontVm.wishlistProducts.collectAsStateWithLifecycle()
    val wishlistIds by storefrontVm.wishlistIds.collectAsStateWithLifecycle()
    val cartState by storefrontVm.cartUiState.collectAsStateWithLifecycle()

    val activeUser by accountVm.activeUser.collectAsStateWithLifecycle()
    val adminCount by accountVm.adminCount.collectAsStateWithLifecycle()
    val authError by accountVm.authError.collectAsStateWithLifecycle()
    val savedAddresses by accountVm.savedAddresses.collectAsStateWithLifecycle()
    val userOrders by accountVm.userOrders.collectAsStateWithLifecycle()
    val notifications by accountVm.notifications.collectAsStateWithLifecycle()
    val wholesaleEnquiries by accountVm.wholesaleEnquiries.collectAsStateWithLifecycle()
    val businessSettings by accountVm.businessSettings.collectAsStateWithLifecycle()
    val checkoutState by accountVm.checkoutState.collectAsStateWithLifecycle()

    val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }
    val isAdmin = activeUser?.role == UserRole.ADMIN.key

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showPrimaryNav = currentRoute in topNavTabs.map { it.route }

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showPrimaryNav && !useRail) {
                    NavigationBar {
                        topNavTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                modifier = Modifier.testTag("nav_${tab.route}"),
                                icon = {
                                    val count = when (tab.route) {
                                        Routes.CART -> cartState.totalActiveCount
                                        Routes.WISHLIST -> wishlistIds.size
                                        else -> 0
                                    }
                                    if (count > 0) {
                                        BadgedBox(badge = { Badge { Text("$count") } }) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = { Text(tab.label, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showPrimaryNav && useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        topNavTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    val count = when (tab.route) {
                                        Routes.CART -> cartState.totalActiveCount
                                        Routes.WISHLIST -> wishlistIds.size
                                        else -> 0
                                    }
                                    if (count > 0) {
                                        BadgedBox(badge = { Badge { Text("$count") } }) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            categories = categories,
                            products = allProducts,
                            offers = offers,
                            reviews = allReviews,
                            wishlistIds = wishlistIds,
                            cartCount = cartState.totalActiveCount,
                            unreadNotificationCount = unreadNotifications,
                            isAdmin = isAdmin,
                            onSearchSubmit = { q ->
                                storefrontVm.setSearchQuery(q)
                                navigateTopTab(Routes.PRODUCTS)
                            },
                            onShopNow = {
                                storefrontVm.clearAllFilters()
                                navigateTopTab(Routes.PRODUCTS)
                            },
                            onCategoryClick = { catName ->
                                storefrontVm.selectSingleCategory(catName)
                                navigateTopTab(Routes.PRODUCTS)
                            },
                            onProductClick = { pid -> navController.navigate(Routes.productDetail(pid)) },
                            onAddToCart = { pid -> storefrontVm.addToCart(pid) },
                            onToggleWishlist = { pid -> storefrontVm.toggleWishlist(pid) },
                            onOpenWishlist = { navigateTopTab(Routes.WISHLIST) },
                            onOpenCart = { navigateTopTab(Routes.CART) },
                            onOpenWholesale = { navController.navigate(Routes.WHOLESALE) },
                            onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onOpenAdmin = { navController.navigate(Routes.ADMIN) },
                            onSubscribeNewsletter = { email -> storefrontVm.subscribeNewsletter(email) },
                            onOpenAccount = { navigateTopTab(Routes.ACCOUNT) },
                            onOpenOrders = { navController.navigate(Routes.ORDERS) }
                        )
                    }

                    composable(Routes.CATEGORIES) {
                        CategoriesScreen(
                            categories = categories,
                            products = allProducts,
                            onCategorySelect = { catName ->
                                storefrontVm.selectSingleCategory(catName)
                                navigateTopTab(Routes.PRODUCTS)
                            }
                        )
                    }

                    composable(Routes.PRODUCTS) {
                        CatalogScreen(
                            products = filteredProducts,
                            categories = categories,
                            availableBrands = availableBrands,
                            filterState = filterState,
                            wishlistIds = wishlistIds,
                            onQueryChange = storefrontVm::setSearchQuery,
                            onToggleCategory = storefrontVm::toggleCategoryFilter,
                            onTogglePriceBracket = storefrontVm::togglePriceBracket,
                            onCustomPriceRange = storefrontVm::setCustomPriceRange,
                            onToggleBrand = storefrontVm::toggleBrandFilter,
                            onMinRating = storefrontVm::setMinRatingFilter,
                            onDeliverySpeed = storefrontVm::setDeliverySpeedFilter,
                            onSortOption = storefrontVm::setSortOption,
                            onClearAllFilters = storefrontVm::clearAllFilters,
                            onProductClick = { pid -> navController.navigate(Routes.productDetail(pid)) },
                            onAddToCart = { pid -> storefrontVm.addToCart(pid) },
                            onToggleWishlist = { pid -> storefrontVm.toggleWishlist(pid) }
                        )
                    }

                    composable(Routes.WISHLIST) {
                        WishlistScreen(
                            wishlistProducts = wishlistProducts,
                            onProductClick = { pid -> navController.navigate(Routes.productDetail(pid)) },
                            onMoveToCart = storefrontVm::moveWishlistItemToCart,
                            onRemoveFromWishlist = storefrontVm::toggleWishlist,
                            onBrowseProducts = { navigateTopTab(Routes.PRODUCTS) }
                        )
                    }

                    composable(Routes.CART) {
                        CartScreen(
                            cartState = cartState,
                            onUpdateQuantity = storefrontVm::updateCartQuantity,
                            onRemoveItem = storefrontVm::removeFromCart,
                            onSaveForLater = storefrontVm::saveForLater,
                            onMoveToCart = storefrontVm::moveToCart,
                            onSelectOffer = storefrontVm::selectOffer,
                            onSelectFreeGift = storefrontVm::selectFreeGift,
                            onCheckPinCode = storefrontVm::checkDeliveryPinCode,
                            onSelectDeliveryMethod = storefrontVm::selectDeliveryMethod,
                            onProductClick = { pid -> navController.navigate(Routes.productDetail(pid)) },
                            onBrowseProducts = { navigateTopTab(Routes.PRODUCTS) },
                            onProceedToCheckout = {
                                accountVm.prepareCheckout(
                                    prefillPinCode = cartState.checkedPinCode,
                                    prefillDeliveryMethod = cartState.selectedDeliveryMethod
                                )
                                navController.navigate(Routes.CHECKOUT)
                            }
                        )
                    }

                    composable(Routes.ACCOUNT) {
                        AccountScreen(
                            activeUser = activeUser,
                            adminCount = adminCount,
                            savedAddresses = savedAddresses,
                            orders = userOrders,
                            authError = authError,
                            onClearError = accountVm::clearAuthError,
                            onLogin = { id, pw -> accountVm.login(id, pw) },
                            onSignUp = { name, email, mobile, pw, admin ->
                                accountVm.signUp(name, email, mobile, pw, admin)
                            },
                            onResetPassword = accountVm::resetPassword,
                            onLogout = accountVm::logout,
                            onUpdateProfile = accountVm::updateProfile,
                            onSaveAddress = accountVm::saveAddress,
                            onDeleteAddress = accountVm::deleteAddress,
                            onOpenOrders = { navController.navigate(Routes.ORDERS) },
                            onOpenWholesale = { navController.navigate(Routes.WHOLESALE) },
                            onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onOpenAdminDashboard = { navController.navigate(Routes.ADMIN) }
                        )
                    }

                    composable(
                        route = Routes.PRODUCT_DETAIL,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { entry ->
                        val pid = entry.arguments?.getString("productId") ?: ""
                        val product by storefrontVm.observeProduct(pid).collectAsStateWithLifecycle(initialValue = null)
                        val reviews by storefrontVm.observeProductReviews(pid).collectAsStateWithLifecycle(initialValue = emptyList())
                        val related = remember(allProducts, pid) {
                            allProducts.filter { it.id != pid }.take(4)
                        }

                        ProductDetailScreen(
                            product = product,
                            relatedProducts = related,
                            reviews = reviews,
                            isWishlisted = pid in wishlistIds,
                            wishlistIds = wishlistIds,
                            checkedPinCode = cartState.checkedPinCode,
                            isPinRushEligible = cartState.isPinRushEligible,
                            isSignedIn = activeUser != null,
                            onBack = { navController.popBackStack() },
                            onAddToCart = { qty, color, size ->
                                storefrontVm.addToCart(pid, qty, color, size)
                            },
                            onBuyNow = { qty, color, size ->
                                storefrontVm.addToCart(pid, qty, color, size) {
                                    accountVm.prepareCheckout(
                                        prefillPinCode = cartState.checkedPinCode,
                                        prefillDeliveryMethod = cartState.selectedDeliveryMethod
                                    )
                                    navController.navigate(Routes.CHECKOUT)
                                }
                            },
                            onToggleWishlist = storefrontVm::toggleWishlist,
                            onCheckPinCode = storefrontVm::checkDeliveryPinCode,
                            onSubmitReview = { rating, title, comment, onDone ->
                                storefrontVm.submitReview(pid, rating, title, comment, onDone)
                            },
                            onRelatedProductClick = { relId ->
                                navController.navigate(Routes.productDetail(relId))
                            },
                            onOpenAccount = { navigateTopTab(Routes.ACCOUNT) }
                        )
                    }

                    composable(Routes.CHECKOUT) {
                        CheckoutScreen(
                            checkoutState = checkoutState,
                            cartState = cartState,
                            savedAddresses = savedAddresses,
                            gatewayConfig = accountVm.getGatewayConfigStatus(),
                            isPinRushEligible = cartState.isPinRushEligible,
                            onUpdateState = accountVm::updateCheckoutFields,
                            onApplySavedAddress = accountVm::applySavedAddressToCheckout,
                            onCheckPinCode = storefrontVm::checkDeliveryPinCode,
                            onNextStep = {
                                accountVm.validateAndAdvanceStep(cartState.isPinRushEligible)
                            },
                            onPrevStep = {
                                accountVm.goToCheckoutStep(checkoutState.currentStep - 1)
                            },
                            onSelectDeliveryMethod = storefrontVm::selectDeliveryMethod,
                            onPlaceOrder = {
                                accountVm.placeOrder(
                                    preferredOfferId = cartState.priceBreakdown.appliedOffer?.id,
                                    selectedFreeGiftId = cartState.selectedFreeGift?.id,
                                    onOrderPlaced = {}
                                )
                            },
                            onTrackOrder = { orderId ->
                                navController.navigate(Routes.orderDetail(orderId)) {
                                    popUpTo(Routes.HOME)
                                }
                            },
                            onViewInvoice = { orderId ->
                                navController.navigate(Routes.gstInvoice(orderId)) {
                                    popUpTo(Routes.HOME)
                                }
                            },
                            onContinueShopping = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.ORDERS) {
                        OrdersListScreen(
                            orders = userOrders,
                            onBack = { navController.popBackStack() },
                            onOpenOrder = { oid -> navController.navigate(Routes.orderDetail(oid)) },
                            onBrowseProducts = { navigateTopTab(Routes.PRODUCTS) }
                        )
                    }

                    composable(
                        route = Routes.ORDER_DETAIL,
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { entry ->
                        val oid = entry.arguments?.getString("orderId") ?: ""
                        val order by accountVm.observeOrderById(oid).collectAsStateWithLifecycle(initialValue = null)
                        OrderDetailScreen(
                            order = order,
                            onBack = { navController.popBackStack() },
                            onCancelOrder = accountVm::cancelOrder,
                            onOpenInvoice = { id -> navController.navigate(Routes.gstInvoice(id)) }
                        )
                    }

                    composable(
                        route = Routes.GST_INVOICE,
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { entry ->
                        val oid = entry.arguments?.getString("orderId") ?: ""
                        val order by accountVm.observeOrderById(oid).collectAsStateWithLifecycle(initialValue = null)
                        GstInvoiceScreen(
                            order = order,
                            businessSettings = businessSettings,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.WHOLESALE) {
                        WholesaleScreen(
                            products = allProducts,
                            enquiries = wholesaleEnquiries,
                            activeUser = activeUser,
                            onBack = { navController.popBackStack() },
                            onSubmitEnquiry = accountVm::submitWholesaleEnquiry,
                            onRespondToQuote = accountVm::respondToWholesaleQuote
                        )
                    }

                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            notifications = notifications,
                            onMarkAllRead = accountVm::markNotificationsRead,
                            onOpenOrder = { oid -> navController.navigate(Routes.orderDetail(oid)) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.ADMIN) {
                        val reports by adminVm.reportsSummary.collectAsStateWithLifecycle()
                        val adminProducts by adminVm.products.collectAsStateWithLifecycle()
                        val adminCategories by adminVm.categories.collectAsStateWithLifecycle()
                        val adminOrders by adminVm.filteredOrders.collectAsStateWithLifecycle()
                        val adminOrderSearch by adminVm.orderSearchQuery.collectAsStateWithLifecycle()
                        val adminUsers by adminVm.allUsers.collectAsStateWithLifecycle()
                        val adminDelivery by adminVm.deliverySettings.collectAsStateWithLifecycle()
                        val adminPins by adminVm.eligiblePinCodes.collectAsStateWithLifecycle()
                        val adminOffers by adminVm.offers.collectAsStateWithLifecycle()
                        val adminWholesale by adminVm.wholesaleEnquiries.collectAsStateWithLifecycle()
                        val adminBusiness by adminVm.businessSettings.collectAsStateWithLifecycle()
                        val adminNewsletter by adminVm.newsletterSubscribers.collectAsStateWithLifecycle()

                        AdminDashboardScreen(
                            activeUser = activeUser,
                            reports = reports,
                            products = adminProducts,
                            categories = adminCategories,
                            orders = adminOrders,
                            orderSearchQuery = adminOrderSearch,
                            users = adminUsers,
                            deliverySettings = adminDelivery,
                            eligiblePinCodes = adminPins,
                            offers = adminOffers,
                            wholesaleEnquiries = adminWholesale,
                            businessSettings = adminBusiness,
                            newsletterSubscribers = adminNewsletter,
                            gatewayConfig = accountVm.getGatewayConfigStatus(),
                            onBack = { navController.popBackStack() },
                            onOrderSearchChange = adminVm::setOrderSearchQuery,
                            onImportImages = adminVm::importPickedImages,
                            onSaveProduct = { entity -> adminVm.saveProduct(entity) },
                            onDeleteProduct = adminVm::deleteProduct,
                            onSaveCategory = adminVm::saveCategory,
                            onUpdateOrderStatus = adminVm::updateOrderStatus,
                            onOpenOrderInvoice = { oid -> navController.navigate(Routes.gstInvoice(oid)) },
                            onToggleUserActive = adminVm::toggleUserActive,
                            onSaveDeliverySettings = adminVm::saveDeliverySettings,
                            onSavePinCode = adminVm::savePinCode,
                            onDeletePinCode = adminVm::deletePinCode,
                            onSaveOffer = adminVm::saveOffer,
                            onDeleteOffer = adminVm::deleteOffer,
                            onUpdateWholesale = adminVm::updateWholesaleEnquiry,
                            onSaveBusinessSettings = adminVm::saveBusinessSettings
                        )
                    }
                }
            }
        }
    }
}
