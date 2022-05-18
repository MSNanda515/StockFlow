package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

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

class ItemVM(
    @field:Min(1)
    val itemNo: Long,
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val description: String,
    @field:NotNull
    val department: Department,

) {
    companion object {
        fun createItem(): ItemVM {
            return ItemVM(
                itemNo = 1,
                name = "Name",
                description = "Desc",
                department = Department.MISC,
            )
        }
    }
}

/**
 * Represents the status code for item
 */
enum class ItemStatus(disp: String) {
    @JsonProperty("active")  ACTIVE("Active"),
    @JsonProperty("inactive") INACTIVE("Inactive"),
}

/**
 * Represents the departments to group
 * items
 */
enum class Department(val disp: String, val palleteCap: Int) {
    @JsonProperty("grocery") GRCY("Grocery", 50), // Grocery
    @JsonProperty("electric") ELEC("Electric", 10), // Electric
    @JsonProperty("household") HSLD("Household", 1), // Household
    @JsonProperty("stationary") STRY("Stationary", 50), // Stationary
    @JsonProperty("automobile") ATMB("Automobile", 1), // Automobile
    @JsonProperty("misc") MISC("Miscellaneous", 10), // Miscellaneous
}