package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.domain.PricingEngine
import com.example.data.local.SeedCatalogData
import com.example.data.model.CartItemDetail
import com.example.data.model.CatalogFilterState
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.DeliverySpeedFilter
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.EligiblePinCodeEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OfferEvaluation
import com.example.data.model.PriceBracket
import com.example.data.model.PriceBreakdown
import com.example.data.model.Product
import com.example.data.model.ReviewEntity
import com.example.data.model.SortOption
import com.example.data.repository.ShasvatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val activeItems: List<CartItemDetail> = emptyList(),
    val savedForLaterItems: List<CartItemDetail> = emptyList(),
    val totalActiveCount: Int = 0,
    val offerEvaluations: List<OfferEvaluation> = emptyList(),
    val eligibleFreeGifts: List<Product> = emptyList(),
    val selectedFreeGift: Product? = null,
    val selectedDeliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD,
    val checkedPinCode: String = "",
    val isPinRushEligible: Boolean = false,
    val priceBreakdown: PriceBreakdown = PriceBreakdown(
        productSubtotal = 0,
        offerDiscount = 0,
        standardDeliveryCharge = 0,
        rushDeliveryCharge = 0,
        totalDeliveryCharge = 0,
        shoppingBagCharge = 0,
        finalPayableAmount = 0,
        appliedOffer = null,
        isFreeStandardDeliveryApplied = false,
        isFreeGiftEligible = false,
        freeGiftMaxPrice = null,
        selectedFreeGift = null,
        isRushAvailableForPin = false,
        estimatedDeliveryText = SeedCatalogData.initialDeliverySettings.standardEstimateText
    ),
    val deliverySettings: DeliverySettingsEntity = SeedCatalogData.initialDeliverySettings
)

class StorefrontViewModel(
    private val repository: ShasvatRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(CatalogFilterState())
    val filterState: StateFlow<CatalogFilterState> = _filterState.asStateFlow()

    private val _selectedDeliveryMethod = MutableStateFlow(DeliveryMethod.STANDARD)
    private val _selectedFreeGiftId = MutableStateFlow<String?>(null)

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessages = _uiMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val categories: StateFlow<List<CategoryEntity>> = repository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviewsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistProducts: StateFlow<List<Product>> = repository.wishlistProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val offers: StateFlow<List<OfferEntity>> = repository.offersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eligiblePinCodes: StateFlow<List<EligiblePinCodeEntity>> = repository.eligiblePinCodesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliverySettings: StateFlow<DeliverySettingsEntity> = repository.deliverySettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedCatalogData.initialDeliverySettings)

    val availableBrands: StateFlow<List<String>> = allProducts.combine(MutableStateFlow(Unit)) { list, _ ->
        list.map { it.brand }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Shasvat Textiles"))

    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.productsFlow,
        _filterState
    ) { products, filter ->
        applyFiltersAndSort(products, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val cartSelectionFlow = combine(
        repository.selectedPinCodeFlow,
        repository.selectedOfferIdFlow,
        _selectedDeliveryMethod,
        _selectedFreeGiftId,
        eligiblePinCodes
    ) { pin, offerId, deliveryMethod, freeGiftId, pinList ->
        CartSelectionArgs(pin, offerId, deliveryMethod, freeGiftId, pinList)
    }

    val cartUiState: StateFlow<CartUiState> = combine(
        repository.cartDetailsFlow,
        repository.productsFlow,
        repository.offersFlow,
        repository.deliverySettingsFlow,
        cartSelectionFlow
    ) { cartDetails, products, offersList, settings, selection ->
        val active = cartDetails.filter { !it.cartItem.isSavedForLater }
        val saved = cartDetails.filter { it.cartItem.isSavedForLater }
        val activeSubtotal = active.sumOf { it.lineTotal }
        val evals = PricingEngine.evaluateOffers(activeSubtotal, offersList, settings)

        val cleanedPin = selection.pin.trim()
        val isPinRushEligible = cleanedPin.length == 6 &&
            selection.pinList.any { it.pinCode == cleanedPin && it.isRushAvailable }

        val effectiveDelivery = if (selection.deliveryMethod == DeliveryMethod.RUSH && isPinRushEligible) {
            DeliveryMethod.RUSH
        } else {
            DeliveryMethod.STANDARD
        }

        val freeGifts = products.filter { it.isInStock && it.price < 800 }.sortedByDescending { it.price }
        val chosenGift = selection.freeGiftId?.let { gid -> freeGifts.firstOrNull { it.id == gid } }
            ?: freeGifts.firstOrNull()

        val breakdown = PricingEngine.calculatePriceBreakdown(
            cartItems = active,
            offers = offersList,
            deliverySettings = settings,
            preferredOfferId = selection.offerId,
            deliveryMethod = effectiveDelivery,
            isPinRushEligible = isPinRushEligible,
            selectedFreeGift = chosenGift
        )

        CartUiState(
            activeItems = active,
            savedForLaterItems = saved,
            totalActiveCount = active.sumOf { it.cartItem.quantity },
            offerEvaluations = evals,
            eligibleFreeGifts = freeGifts,
            selectedFreeGift = breakdown.selectedFreeGift,
            selectedDeliveryMethod = effectiveDelivery,
            checkedPinCode = cleanedPin,
            isPinRushEligible = isPinRushEligible,
            priceBreakdown = breakdown,
            deliverySettings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    fun observeProduct(productId: String): Flow<Product?> = repository.observeProduct(productId)

    fun observeProductReviews(productId: String): Flow<List<ReviewEntity>> =
        repository.observeReviewsForProduct(productId)

    // Filter & Search actions
    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun selectSingleCategory(categoryName: String?) {
        _filterState.update {
            it.copy(selectedCategories = if (categoryName.isNullOrBlank()) emptySet() else setOf(categoryName))
        }
    }

    fun toggleCategoryFilter(categoryName: String) {
        _filterState.update { state ->
            val next = if (categoryName in state.selectedCategories) {
                state.selectedCategories - categoryName
            } else {
                state.selectedCategories + categoryName
            }
            state.copy(selectedCategories = next)
        }
    }

    fun togglePriceBracket(bracket: PriceBracket) {
        _filterState.update { state ->
            val next = if (bracket in state.selectedPriceBrackets) {
                state.selectedPriceBrackets - bracket
            } else {
                state.selectedPriceBrackets + bracket
            }
            state.copy(selectedPriceBrackets = next)
        }
    }

    fun setCustomPriceRange(minPrice: Int?, maxPrice: Int?) {
        _filterState.update { it.copy(customMinPrice = minPrice, customMaxPrice = maxPrice) }
    }

    fun toggleBrandFilter(brand: String) {
        _filterState.update { state ->
            val next = if (brand in state.selectedBrands) {
                state.selectedBrands - brand
            } else {
                state.selectedBrands + brand
            }
            state.copy(selectedBrands = next)
        }
    }

    fun setMinRatingFilter(rating: Int?) {
        _filterState.update { it.copy(minRating = rating) }
    }

    fun setDeliverySpeedFilter(speed: DeliverySpeedFilter?) {
        _filterState.update { it.copy(deliverySpeed = speed) }
    }

    fun setSortOption(option: SortOption) {
        _filterState.update { it.copy(sortOption = option) }
    }

    fun clearAllFilters() {
        _filterState.value = CatalogFilterState()
    }

    // Cart, Wishlist, Offer, PIN actions
    fun addToCart(
        productId: String,
        quantity: Int = 1,
        selectedColor: String = "",
        selectedSize: String = "",
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val res = repository.addToCart(productId, quantity, selectedColor, selectedSize)
            res.onSuccess {
                _uiMessages.tryEmit("Added to shopping cart")
                onSuccess?.invoke()
            }.onFailure { err ->
                _uiMessages.tryEmit(err.message ?: "Unable to add to cart")
            }
        }
    }

    fun updateCartQuantity(cartItemId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, newQty).onFailure {
                _uiMessages.tryEmit(it.message ?: "Unable to update quantity")
            }
        }
    }

    fun saveForLater(cartItemId: String) {
        viewModelScope.launch {
            repository.toggleSaveForLater(cartItemId, true)
            _uiMessages.tryEmit("Saved for later")
        }
    }

    fun moveToCart(cartItemId: String) {
        viewModelScope.launch {
            repository.toggleSaveForLater(cartItemId, false)
            _uiMessages.tryEmit("Moved back to cart")
        }
    }

    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
            _uiMessages.tryEmit("Removed from cart")
        }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            val added = repository.toggleWishlist(productId)
            _uiMessages.tryEmit(if (added) "Added to wishlist" else "Removed from wishlist")
        }
    }

    fun moveWishlistItemToCart(productId: String) {
        viewModelScope.launch {
            repository.addToCart(productId, 1).onSuccess {
                repository.toggleWishlist(productId)
                _uiMessages.tryEmit("Moved from wishlist to cart")
            }.onFailure {
                _uiMessages.tryEmit(it.message ?: "Unable to move to cart")
            }
        }
    }

    fun checkDeliveryPinCode(pinCode: String) {
        viewModelScope.launch {
            val clean = pinCode.trim()
            if (clean.length != 6 || clean.any { !it.isDigit() }) {
                _uiMessages.tryEmit("Please enter a valid 6-digit PIN code")
                return@launch
            }
            repository.setSelectedPinCode(clean)
            val eligible = repository.isPinRushEligible(clean)
            if (!eligible && _selectedDeliveryMethod.value == DeliveryMethod.RUSH) {
                _selectedDeliveryMethod.value = DeliveryMethod.STANDARD
            }
            _uiMessages.tryEmit(
                if (eligible) "Rush Delivery (Within 2 Hours) is available for PIN $clean!"
                else "Standard Delivery available for PIN $clean. Rush Delivery is not available in this area."
            )
        }
    }

    fun selectDeliveryMethod(method: DeliveryMethod) {
        _selectedDeliveryMethod.value = method
    }

    fun selectOffer(offerId: String?) {
        viewModelScope.launch {
            repository.setSelectedOfferId(offerId)
        }
    }

    fun selectFreeGift(productId: String) {
        _selectedFreeGiftId.value = productId
    }

    fun submitReview(
        productId: String,
        rating: Int,
        title: String,
        comment: String,
        onDone: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.submitReview(productId, rating, title, comment)
            res.onSuccess {
                _uiMessages.tryEmit("Review published! Thank you for your feedback.")
                onDone(true, "Review published")
            }.onFailure {
                val msg = it.message ?: "Unable to submit review"
                _uiMessages.tryEmit(msg)
                onDone(false, msg)
            }
        }
    }

    fun subscribeNewsletter(email: String) {
        viewModelScope.launch {
            repository.subscribeNewsletter(email).onSuccess {
                _uiMessages.tryEmit("Subscribed to Shasvat Textiles updates!")
            }.onFailure {
                _uiMessages.tryEmit(it.message ?: "Please enter a valid email")
            }
        }
    }

    private fun applyFiltersAndSort(products: List<Product>, filter: CatalogFilterState): List<Product> {
        val q = filter.query.trim().lowercase()
        val filtered = products.filter { p ->
            val matchesQuery = if (q.isBlank()) true else {
                p.name.lowercase().contains(q) ||
                    p.categoryName.lowercase().contains(q) ||
                    p.brand.lowercase().contains(q) ||
                    p.description.lowercase().contains(q) ||
                    p.fabricDetails.lowercase().contains(q) ||
                    p.specifications.lowercase().contains(q)
            }

            val matchesCategory = if (filter.selectedCategories.isEmpty()) true else {
                filter.selectedCategories.any { it.equals(p.categoryName, ignoreCase = true) }
            }

            val matchesBracket = if (filter.selectedPriceBrackets.isEmpty()) true else {
                filter.selectedPriceBrackets.any { bracket ->
                    val minOk = bracket.min == null || p.price >= bracket.min
                    val maxOk = bracket.max == null || p.price <= bracket.max
                    minOk && maxOk
                }
            }

            val matchesCustomRange =
                (filter.customMinPrice == null || p.price >= filter.customMinPrice) &&
                    (filter.customMaxPrice == null || p.price <= filter.customMaxPrice)

            val matchesBrand = if (filter.selectedBrands.isEmpty()) true else {
                filter.selectedBrands.any { it.equals(p.brand, ignoreCase = true) }
            }

            val matchesRating = if (filter.minRating == null) true else {
                p.averageRating >= filter.minRating.toDouble()
            }

            val matchesDelivery = when (filter.deliverySpeed) {
                null -> true
                DeliverySpeedFilter.WITHIN_2_HOURS -> p.isRushEligible
                DeliverySpeedFilter.WITHIN_24_HOURS -> p.deliveryEstimate.contains("24", ignoreCase = true) || p.isRushEligible
                DeliverySpeedFilter.WITHIN_2_DAYS -> true
            }

            matchesQuery && matchesCategory && matchesBracket && matchesCustomRange &&
                matchesBrand && matchesRating && matchesDelivery
        }

        return when (filter.sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
            SortOption.CUSTOMER_RATING -> filtered.sortedWith(
                compareByDescending<Product> { it.averageRating }.thenByDescending { it.reviewCount }
            )
            SortOption.NEW_ARRIVALS -> filtered.sortedWith(
                compareByDescending<Product> { it.isNewArrival }.thenByDescending { it.createdAt }
            )
            SortOption.POPULARITY -> filtered.sortedWith(
                compareByDescending<Product> { it.isBestSeller }
                    .thenByDescending { it.isTrending }
                    .thenBy { it.createdAt }
            )
        }
    }

    private data class CartSelectionArgs(
        val pin: String,
        val offerId: String?,
        val deliveryMethod: DeliveryMethod,
        val freeGiftId: String?,
        val pinList: List<EligiblePinCodeEntity>
    )

    companion object {
        fun factory(repository: ShasvatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StorefrontViewModel(repository) as T
                }
            }
    }
}
