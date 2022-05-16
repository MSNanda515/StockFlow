package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Represents the model for an item in the warehouse
 */
@Document
data class Item(
    var name: String,
    var description: String,
    var department: String,
    var status: ItemStatus,
    @Id
    val id: ObjectId = ObjectId.get(),
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now()
)

class ItemRequestVM(
    val name: String,
    val description: String,
    val department: String,
    val status: ItemStatus,
)

/**
 * Represents the status code for item
 */
enum class ItemStatus {
    @JsonProperty("active")  ACTIVE,
    @JsonProperty("inactive") INACTIVE
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