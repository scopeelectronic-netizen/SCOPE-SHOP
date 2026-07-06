package com.example.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppTab {
    DASHBOARD,
    SHOP,
    RENTALS,
    SERVICES,
    CART
}

// --- STATIC ITEM DATA MODELS ---

data class StaticProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val originalPrice: Int? = null,
    val imageUrl: String,
    val badge: String? = null,
    val isPopular: Boolean = false,
    val category: String = "General"
)

data class StaticRentalPackage(
    val id: String,
    val name: String,
    val description: String,
    val pricePerDay: Int,
    val pricePerWeek: Int,
    val imageUrl: String,
    val badge: String? = null,
    val features: List<String>
)

data class StaticService(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val iconName: String
)

data class StaticAmcPlan(
    val id: String,
    val name: String,
    val description: String,
    val pricePerYear: Int,
    val isPopular: Boolean = false,
    val features: List<String>
)

class ScopeViewModel(private val repository: ScopeRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    // --- Tab Navigation State ---
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Flows from Database ---
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.appointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceRequests: StateFlow<List<ServiceRequest>> = repository.serviceRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Rentals Tab Dates ---
    private val _rentalsStartDate = MutableStateFlow("2024-05-15")
    val rentalsStartDate: StateFlow<String> = _rentalsStartDate.asStateFlow()

    private val _rentalsEndDate = MutableStateFlow("2024-05-18")
    val rentalsEndDate: StateFlow<String> = _rentalsEndDate.asStateFlow()

    fun updateRentalsDates(start: String, end: String) {
        _rentalsStartDate.value = start
        _rentalsEndDate.value = end
    }

    // Calculated days period
    val rentalsPeriodDays: Int
        get() {
            return try {
                val start = LocalDate.parse(_rentalsStartDate.value)
                val end = LocalDate.parse(_rentalsEndDate.value)
                val days = java.time.temporal.ChronoUnit.DAYS.between(start, end)
                if (days > 0) days.toInt() else 1
            } catch (e: Exception) {
                3
            }
        }

    // --- Services Selection ---
    private val _selectedServiceType = MutableStateFlow("AC Deep Cleaning")
    val selectedServiceType: StateFlow<String> = _selectedServiceType.asStateFlow()

    private val _selectedServicePrice = MutableStateFlow(85)
    val selectedServicePrice: StateFlow<Int> = _selectedServicePrice.asStateFlow()

    fun selectService(name: String, price: Int) {
        _selectedServiceType.value = name
        _selectedServicePrice.value = price
    }

    // Services Tab Calendar selection
    private val _selectedCalendarDay = MutableStateFlow(10) // default 10 Oct
    val selectedCalendarDay: StateFlow<Int> = _selectedCalendarDay.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow("11:30 AM")
    val selectedTimeSlot: StateFlow<String> = _selectedTimeSlot.asStateFlow()

    fun selectCalendarDay(day: Int) {
        _selectedCalendarDay.value = day
    }

    fun selectTimeSlot(slot: String) {
        _selectedTimeSlot.value = slot
    }

    // --- Modal Success States ---
    private val _showCustomRequestModal = MutableStateFlow(false)
    val showCustomRequestModal: StateFlow<Boolean> = _showCustomRequestModal.asStateFlow()

    private val _showBookingSuccessModal = MutableStateFlow(false)
    val showBookingSuccessModal: StateFlow<Boolean> = _showBookingSuccessModal.asStateFlow()

    fun setCustomRequestModalVisible(visible: Boolean) {
        _showCustomRequestModal.value = visible
    }

    fun setBookingSuccessModalVisible(visible: Boolean) {
        _showBookingSuccessModal.value = visible
    }

    // --- Cart Actions ---
    fun addToCart(name: String, price: Int, imageUrl: String, type: String, subtitle: String) {
        viewModelScope.launch {
            repository.addCartItem(name, price, imageUrl, type, subtitle)
        }
    }

    fun updateCartQuantity(id: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(id, quantity)
        }
    }

    fun deleteCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.removeCartItem(item)
        }
    }

    fun checkoutCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // --- Appointment Actions ---
    fun scheduleAppointment(title: String, day: String, month: String, time: String, location: String) {
        viewModelScope.launch {
            repository.addAppointment(title, day, month, time, location)
            setBookingSuccessModalVisible(true)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // --- Custom Setup Request Form Action ---
    fun submitCustomSetup(
        fullName: String,
        eventType: String,
        location: String,
        guestCountStr: String,
        requirements: List<String>,
        details: String
    ) {
        viewModelScope.launch {
            val guestCount = guestCountStr.toIntOrNull() ?: 150
            val requirementsStr = requirements.joinToString(", ")
            repository.addServiceRequest(
                fullName = if (fullName.isNotBlank()) fullName else "John Doe",
                eventType = eventType,
                location = if (location.isNotBlank()) location else "City Hall, Downtown District",
                guestCount = guestCount,
                requirements = requirementsStr,
                details = details
            )
            setCustomRequestModalVisible(true)
        }
    }

    // --- STATIC CONTENT DATA ---

    val shopProducts = listOf(
        StaticProduct(
            id = "prod_ac",
            name = "Scope V-Series AC",
            description = "Variable Frequency Inverter • 2.0 Ton",
            price = 2850,
            originalPrice = 3499,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDHvLl42k4rkaS-J94msMPOF46oVAZbA1OSvVaSp8-RoqAD2Ow9INh3ZaE_qCL__LWtQzV9smjZhh7jUTNg0KW3v2sut74GhKSqMtNjTWEXTfe4HbgFru2u8eiAoyAm3yFmD2bVv5X8S4gipRbj4p1NcF_fRrswoezAbORRmx888zvLTLSpkyO_lLxUOuGFQ_BWjCCnplm9FJOn9TOyqvl3J4pP828kG3C8Xm7BrIGy5-GCMpTk1IvygtCAod8p8tTofO_pBFgtoLyB",
            badge = "In Stock",
            category = "Cooling"
        ),
        StaticProduct(
            id = "prod_thermostat",
            name = "Scope Eco-Thermostat",
            description = "AI Smart Temperature Controller",
            price = 420,
            originalPrice = 499,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCIKBOb2Tc79KHDEHTnLWGQtMnRN-1Bk4TR_BTN2iThm-v_5gUHuP1JJh2y3d-z5ro9hnjeZVNYKGxfGQj75Q7kHW-z1vIk8i9VFvNY4o7oiMBhipBWmBFWcHBebSzAXzP_ilFmS2fzglAWuLLkVlTAOlMQUYBeq3jiFaH0l4rToD-x9h-zNtxTz_ZHgSBbfEvUv9ZqDX4SJZzC21yvSvwdaLRVrnxUnqEAk9Fqut-NN6TXKwD3ynHIU5FL8h0m_gJ3mlWt0MJ_5BoD",
            badge = "Eco-Friendly",
            category = "Cooling"
        ),
        StaticProduct(
            id = "prod_camera",
            name = "Optic X-900 Pro",
            description = "4K Monitoring • Wide-Angle Field",
            price = 1200,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCl-mSoQdtPI7QCRyUUd8pCjk4UVvbVu5t50me_ajyrDdcDgzCKny3WHZfFwBLaL1C3nPLkm29_7Au35tulRCQYf46BbIL39QSruomdnVU4TI3dIKpV3CjEbPgyF0qlMv8bxjrVcVqUVykdYda48o5k4bdORYojMSHq_19W_D_kR0ZHhOD3-726_m5-uXBIQRBUC6RjVxQxarJsDpfim6ByYJf6nYNKTwpNCFluJeDSAqY-ptr5CQF9WlJL6vffWSYW0kKAk8cIF5b3",
            category = "Security"
        ),
        StaticProduct(
            id = "prod_smartlock",
            name = "SecureGuard Smart Lock",
            description = "Biometric Keyless Entry • Zinc Alloy",
            price = 750,
            originalPrice = 899,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBFk22RuP719ww9O9qwulMqd8kgJnVjPskCzNUJIUIaM6-PDkBoOhTvfF6_3G4-mkOcJzTou5i3RSvElkeGJcq6uJ3D6Eyzm1wiZV40vh8jhA__j-Jr-Go2I7GE9QmYcBrD9t-gudX7ywBSHcPtgDsoarICtbf1WsFwxRGwGG94f_gkY1_Wow1mJVcxsa4zIGNG9y_UDeOz3Cy3nByntUiPs0oy9bcQs40s8vYL6VvQRQsMMkss3agtyoDGRQ4Y8bDXuEXKzAlTIyDt",
            badge = "New",
            category = "Security"
        ),
        StaticProduct(
            id = "prod_headphones",
            name = "Sonic Boom 360",
            description = "Active Noise Cancellation • 40h Battery",
            price = 899,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBznBSHbH2FqWF2QRdhzD8fFuVJW4gjETR48orV_38eqzDgxeb_YmGqkKSzJOL1xzJxfWJwIkoJY0bk4N26iNO-3Y48fv2UGrmm00r5TMwdDBVZPeJXkLDtfVJGA6WouCWTJTc_01MkS-zxJpo2hBL2YMqR2guCgIuvNgK08P310o0OESTKnXWTzWY9mdvAJrfbWVX5xGZLhkABpo8iy_fyXAUK-eKJA2ONMi0431gYcEuaYAg_jQLjv2TK_wI3ofxtf3TN24H4z5D9",
            badge = "Popular",
            isPopular = true,
            category = "Audio"
        ),
        StaticProduct(
            id = "prod_monitor",
            name = "Scope Studio Monitor S5",
            description = "Professional High-Fidelity Speakers",
            price = 1499,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuArUrBpDru79iWn-e_0L2pGASeGZjihFS8eEGKtEHNb_NwLkH5F3JjlnpT6FtxVSL92jRLapbdxsTn2wnk-hVCXcWBpsMYZaM8cKoh1EN3oE0SEqsF8x9kcku1oH62c7HZRqKlccyZEGqtirQ9wzAkGO-8QTdt3CGfKadSt4duBVkDLTXyB-7Mpkk2_tjiIQNIAbTgdr929MQ4lOCwy3MUXCFGsopW-Jf5oTv6Bzn5EMelF1GZ4xm-NEcHyu-jaKu18U-mLL8WcjNop",
            category = "Audio"
        ),
        StaticProduct(
            id = "prod_microwave",
            name = "SmartHeat Pro 30L",
            description = "Sensor Cook • Convection System",
            price = 1150,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDOL0q6tJEqa83RFcCQve3LcqcDaRSxogbQiClFd056EphApQmz7xuHtEhzZbhDiiibfWQjuksdG8esrFmfAzOKhtzHNX3nQE1-yu3YPkzvqjlNYtVt15BU1hOY8IsVVNo_98tnhFD3I2vZV0qSVJ2PoZesNA656bbbdYDguWsO4gPbMPAaIENO25tKB0RItiEv7V7dFfeIW56FSoFJS1R941yXLNEsBzJ35VTlJ0FpCO8GJY5zQX144PxNo2LcCXYy391spYRqW4He",
            category = "Kitchen"
        ),
        StaticProduct(
            id = "prod_brewmaster",
            name = "Scope BrewMaster Espresso",
            description = "19 Bar Pump • ThermoBlock Fast Heating",
            price = 1890,
            originalPrice = 2199,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDsMIqdaFYfUq6O3SZCGJBwab7xPraSuEIAh7oZrobYMiJIikUv1p7GNPY3FoIVrnm5b7T-qa0vVN3NcfPUJVFM9y4UY1_Du4ObS2uvjltL4DrtAcWBBtkNna8aZAj-__iqgN97LZN5BoU_XQ-Xx3YiCjpGJn9CO-GmXJoOBde9Lw-_8RPwiPgBKIbOpudt7Kaod3YdvpfeeLLSG83oevPqu03iFKXfc833zOEbii6wz-McSLb1l455RKSvFTyQNDo2s3foGCLSkuy2",
            badge = "Premium",
            category = "Kitchen"
        )
    )

    val latestDeals = listOf(
        StaticProduct(
            id = "deal_display",
            name = "Pro Workstation Display",
            description = "High refresh rate enterprise monitor",
            price = 1450,
            badge = "-15% OFF",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCIKBOb2Tc79KHDEHTnLWGQtMnRN-1Bk4TR_BTN2iThm-v_5gUHuP1JJh2y3d-z5ro9hnjeZVNYKGxfGQj75Q7kHW-z1vIk8i9VFvNY4o7oiMBhipBWmBFWcHBebSzAXzP_ilFmS2fzglAWuLLkVlTAOlMQUYBeq3jiFaH0l4rToD-x9h-zNtxTz_ZHgSBbfEvUv9ZqDX4SJZzC21yvSvwdaLRVrnxUnqEAk9Fqut-NN6TXKwD3ynHIU5FL8h0m_gJ3mlWt0MJ_5BoD"
        ),
        StaticProduct(
            id = "deal_headphones",
            name = "Audio Precision X1",
            description = "Studio grade noise-canceling",
            price = 899,
            badge = "LIMITED",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6sr4zXilQDTQF5d48j4aqGwVdd1kE-eCeblYPXcc-V-KBzNVFbhK50rmdZMdrQWwGIDur8mHHRHUL2RVccFbUYm4Ik1w2J50wPktS7Kad6eMn0Rvcx8dmmQy1owjDnn6VFlbX-5q7vrw5znaeWEAX9xvWieNAMa5U8SgXtbkpJSpr1duH89fGVOr--i9DR4mm_aCKk5Rs43cF2C-gTmw8WuN_d6YGYByNiHUZxTfQr9OO3gYCz-AG37Cq3JfxRGWKLZNVtluzkLCW"
        ),
        StaticProduct(
            id = "deal_grid",
            name = "Smart Power Grid Hub",
            description = "Enterprise energy management",
            price = 3200,
            badge = "NEW",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDsMIqdaFYfUq6O3SZCGJBwab7xPraSuEIAh7oZrobYMiJIikUv1p7GNPY3FoIVrnm5b7T-qa0vVN3NcfPUJVFM9y4UY1_Du4ObS2uvjltL4DrtAcWBBtkNna8aZAj-__iqgN97LZN5BoU_XQ-Xx3YiCjpGJn9CO-GmXJoOBde9Lw-_8RPwiPgBKIbOpudt7Kaod3YdvpfeeLLSG83oevPqu03iFKXfc833zOEbii6wz-McSLb1l455RKSvFTyQNDo2s3foGCLSkuy2"
        )
    )

    val rentalPackages = listOf(
        StaticRentalPackage(
            id = "rent_compact",
            name = "Compact Duo",
            description = "Perfect for seminars, presentations, and cocktail events up to 50 guests. Plug-and-play simplicity.",
            pricePerDay = 145,
            pricePerWeek = 750,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDqIP__Du0LlYJ6lCMIcwDwKGJIfpmFiJ0dgE-w_F5q4iDpWchpBMqm1yu_QERWQWG0erjhj6wwjWwkEMMDsJOowAJ_ofzyWUIoOc5WAEq0A0fao93VJsDegA1u439x9BYOCVleWaUhNRWUD1hSNkL_DJA6YI8TKf5RImh35kPWGfylW_jp1S7YgIe2lHv_i5eOAH3gVbMaSnW2GdGoCNciTbCzQ6JLnIA6O610ukMiQvo3eDQkFpjaxI-7HK0METstHBe7CPsina5-",
            badge = "Most Popular",
            features = listOf("2x 10\" Powered Speakers", "Wireless Microphone System", "Bluetooth Connectivity")
        ),
        StaticRentalPackage(
            id = "rent_venue",
            name = "Venue Premier",
            description = "High-fidelity sound for weddings, parties, and gala dinners up to 200 guests. Deep bass and clear highs.",
            pricePerDay = 295,
            pricePerWeek = 1450,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA0idZ1ZzgNWT5n8VZAGV_rljdsfIaQ9cInz1C1CY6y1FI1mZmPskwBalspUn9-6Keujh_jBeW6tjiE7wLBQQFh_93o29Y847CeRrPQEPzZnbedfj2MXVciorckRXJJ5Ab54uvmmkDyMc96bYKsEnrNHK1A_muUIo7Dzj5c1kMnttKTLNxmjkL4g18EXUSSwmo-RY2p4EU2vci53KvynHTEgRkdYPF98fsMqb4ObK4eBGhF8WwotWdm3AVNLHHo5a7g1VhYy3d8Pd0V",
            features = listOf("2x 12\" Tops + 15\" Subwoofer", "8-Channel Digital Mixer", "Full Cabling & Setup Kit")
        ),
        StaticRentalPackage(
            id = "rent_concert",
            name = "Concert Master",
            description = "Full-scale production for outdoor events, festivals, or live band performances. Touring-grade hardware.",
            pricePerDay = 540,
            pricePerWeek = 2800,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDEefNHoiCeECuz89mHqhGtH4CWPrlf63TPvbvs4RKyqDnWXi7awYnHrWnJOQHvP7GQo1MSEYIzn_tTeg2ipidxJ0Kfj7zBp7PejM468uqvwAky_KIPYzJbwTs-52-eRCutiH0IcniIt2vUcZblfI0jdYQgG2kMpWItSCCfa0sdwkItPjSeInSrVw2YXe81DFtS-HBf01Plf-dQS9LWcbgy0_3omclfyViwNrkf7GH-gOyHid9vPn4KuRbww7AlD3SlRyZXcirpa4Go",
            badge = "Expert Choice",
            features = listOf("4x Line-Array Elements + 2x Subs", "32-Channel Console", "Dedicated Sound Engineer Option")
        )
    )

    val staticServices = listOf(
        StaticService(
            id = "serv_ac",
            name = "AC Cleaning",
            description = "Deep sanitization & efficiency check.",
            price = 85,
            iconName = "ac_unit"
        ),
        StaticService(
            id = "serv_cctv",
            name = "CCTV Checkup",
            description = "Security audit & hardware testing.",
            price = 120,
            iconName = "videocam"
        )
    )

    val amcPlans = listOf(
        StaticAmcPlan(
            id = "amc_basic",
            name = "Basic",
            description = "Essential protection for your electronics.",
            pricePerYear = 299,
            features = listOf("2 Scheduled Visits", "24h Response Time", "Standard Spare Parts")
        ),
        StaticAmcPlan(
            id = "amc_silver",
            name = "Silver",
            description = "Comprehensive care for home owners.",
            pricePerYear = 499,
            isPopular = true,
            features = listOf("4 Scheduled Visits", "12h Response Time", "Labor Costs Included", "Free Hardware Cleaning")
        ),
        StaticAmcPlan(
            id = "amc_gold",
            name = "Gold",
            description = "Commercial grade reliability & priority.",
            pricePerYear = 899,
            features = listOf("Unlimited Emergency Calls", "4h Priority Response", "All Parts & Labor Included", "Dedicated Account Manager")
        )
    )
}

// --- VIEWMODEL FACTORY ---

class ScopeViewModelFactory(private val repository: ScopeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScopeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScopeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
