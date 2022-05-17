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
    var itemNo: Long,
    var name: String,
    var description: String,
    var department: Department,
    var status: ItemStatus,
    @Id
    val id: ObjectId = ObjectId.get(),
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now(),

    val pallets: MutableList<Pallet> = mutableListOf(),
    val shipments: MutableList<Shipment> = mutableListOf(),
)

class ItemRequestVM(
    val itemNo: Long,
    val name: String,
    val description: String,
    val department: Department,
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
    @JsonProperty("grocery") GRCY, // Grocery
    @JsonProperty("electric") ELEC, // Electric
    @JsonProperty("household") HSLD, // Household
    @JsonProperty("stationary") STRY, // Stationary
    @JsonProperty("automobile") ATMB, // Automobile
    @JsonProperty("misc") MISC, // Miscellaneous
}