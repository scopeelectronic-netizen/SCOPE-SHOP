package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val type: String, // "Shop" or "Rental"
    val subtitle: String,
    val quantity: Int = 1
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val day: String, // e.g. "14" or "22"
    val month: String, // e.g. "OCT"
    val time: String, // e.g. "09:30 AM"
    val location: String // e.g. "Residential Site"
)

@Entity(tableName = "service_requests")
data class ServiceRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val eventType: String,
    val location: String,
    val guestCount: Int,
    val requirements: String, // Comma separated, e.g. "Stage Lighting, Live Recording"
    val details: String,
    val status: String = "Pending"
)

// --- DAOS ---

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: Int)

    @Delete
    suspend fun deleteCartItem(item: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY id ASC")
    fun getAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY id DESC")
    fun getServiceRequests(): Flow<List<ServiceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequest)
}

// --- DATABASE ---

@Database(
    entities = [CartItem::class, Appointment::class, ServiceRequest::class],
    version = 1,
    exportSchema = false
)
abstract class ScopeDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun serviceRequestDao(): ServiceRequestDao
}
