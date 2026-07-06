package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ScopeRepository(private val database: ScopeDatabase) {

    val cartItems: Flow<List<CartItem>> = database.cartDao().getCartItems()
    val appointments: Flow<List<Appointment>> = database.appointmentDao().getAppointments()
    val serviceRequests: Flow<List<ServiceRequest>> = database.serviceRequestDao().getServiceRequests()

    suspend fun addCartItem(name: String, price: Int, imageUrl: String, type: String, subtitle: String) {
        // Check if item already exists in cart, if so increase quantity
        val currentItems = database.cartDao().getCartItems().first()
        val existingItem = currentItems.find { it.name == name && it.type == type }
        if (existingItem != null) {
            database.cartDao().updateQuantity(existingItem.id, existingItem.quantity + 1)
        } else {
            database.cartDao().insertCartItem(
                CartItem(
                    name = name,
                    price = price,
                    imageUrl = imageUrl,
                    type = type,
                    subtitle = subtitle,
                    quantity = 1
                )
            )
        }
    }

    suspend fun updateCartItemQuantity(id: Int, quantity: Int) {
        if (quantity <= 0) {
            // Remove the item instead of setting quantity to 0
            val currentItems = database.cartDao().getCartItems().first()
            val item = currentItems.find { it.id == id }
            if (item != null) {
                database.cartDao().deleteCartItem(item)
            }
        } else {
            database.cartDao().updateQuantity(id, quantity)
        }
    }

    suspend fun removeCartItem(item: CartItem) {
        database.cartDao().deleteCartItem(item)
    }

    suspend fun clearCart() {
        database.cartDao().clearCart()
    }

    suspend fun addAppointment(title: String, day: String, month: String, time: String, location: String) {
        database.appointmentDao().insertAppointment(
            Appointment(
                title = title,
                day = day,
                month = month,
                time = time,
                location = location
            )
        )
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        database.appointmentDao().deleteAppointment(appointment)
    }

    suspend fun addServiceRequest(
        fullName: String,
        eventType: String,
        location: String,
        guestCount: Int,
        requirements: String,
        details: String
    ) {
        database.serviceRequestDao().insertRequest(
            ServiceRequest(
                fullName = fullName,
                eventType = eventType,
                location = location,
                guestCount = guestCount,
                requirements = requirements,
                details = details
            )
        )
    }

    // Prepopulate the database with the user's initial upcoming appointments
    suspend fun prepopulateDatabaseIfEmpty() {
        val currentAppts = database.appointmentDao().getAppointments().first()
        if (currentAppts.isEmpty()) {
            database.appointmentDao().insertAppointment(
                Appointment(
                    title = "Home Sound System Install",
                    day = "14",
                    month = "OCT",
                    time = "09:30 AM",
                    location = "Residential Site"
                )
            )
            database.appointmentDao().insertAppointment(
                Appointment(
                    title = "Annual Maintenance Check",
                    day = "22",
                    month = "OCT",
                    time = "11:00 AM",
                    location = "Dubai HQ"
                )
            )
        }
    }
}
