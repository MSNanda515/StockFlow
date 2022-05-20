package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.math.ceil

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

    var pallets: MutableList<Pallet> = mutableListOf(),
    val shipments: MutableList<Shipment> = mutableListOf(),
) {
    /**
     * Gets the number of pallets required to store the item
     */
    fun palletsRequired(units: Int): Int =
        ceil(1.0 * units / department.palleteCap).toInt()

    fun getDisplayStr(): String {
        return "$itemNo (Name: $name, Department: ${department.disp}, Pallet Cap: ${department.palleteCap})"
    }
    companion object {
        /**
         * Create an item object from the item view model
         */
        fun createItem(itemVm: ItemVM): Item {
            var item = Item(
                itemVm.itemNo, itemVm.name, itemVm.description, itemVm.department,
                ItemStatus.ACTIVE
            )
            return item
        }
    }
}

class ItemVM(
    @field:Min(1)
    var itemNo: Long,
    @field:NotBlank
    var name: String,
    @field:NotBlank
    var description: String,
    @field:NotNull
    var department: Department,
    var wareNo: Long = 1,
    @field:Min(0)
    var units: Int = 0,
    var pallets: MutableList<Pallet> = mutableListOf()
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

        /**
         * Prepares the view model from the Item object
         */
        fun prepareVM(item: Item): ItemVM {
            var itemVm = ItemVM(
                itemNo = item.itemNo,
                name = item.name,
                description = item.description,
                department = item.department,
                pallets = item.pallets
            )
            itemVm.units = item.pallets.fold(0) {sum, p -> sum + p.units}
            return itemVm
        }
    }

    fun getDisplayStr(): String {
        return "Item No: $itemNo, Name: $name, Desc: $description, Dep: $department, Units: $units"
    }
}

/**
 * Used to create inventory of an item
 */
data class ItemInventoryVM(
    @field:Min(1)
    var itemNo: Long,
    @field:Min(1)
    var wareNo: Long,
    @field:Min(1)
    var units: Int,
)

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