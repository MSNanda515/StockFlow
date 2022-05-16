package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Represents the model for an item in the warehouse
 */
@Document
data class Item(
    @Id
    val id: ObjectId = ObjectId.get(),
    var name: String,
    var description: String,
    var department: ItemStatus,
    var status: Department,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now()
)

/**
 * Represents the status code for item
 */
enum class ItemStatus {
    ACTIVE, INACTIVE
}

/**
 * Represents the departments to group
 * items
 */
enum class Department {
    GRCY, // Grocery
    ELEC, // Electric
    HSLD, // Household
    STRY, // Stationary
    ATMB, // Automobile
    MISC, // Miscellaneous
}