package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

class WarehouseVM(
    @field:Min(value = 1)
    var wareNo: Long,
    @field:NotBlank
    var name: String,
    @field:NotBlank
    var location: String,
    @field:Min(100)
    var aisle: Int = 200,
    @field:Min(5)
    var section: Int = 10,
    @field:Min(3)
    var level: Int = 4,
) {
    companion object {
        /**
         * Creates an empty View Model
         */
        fun createVM(): WarehouseVM {
            return WarehouseVM(
                wareNo = 1,
                name = "Name",
                location = "Location"
            )
        }
    }

    fun setDefaultValues(wareNo: Long) {
        this.wareNo = wareNo
        this.name = "Ware $wareNo"
        this.location = "Loc $wareNo"
    }
}
@Document
data class Warehouse(
    var wareNo: Long,
    var name: String,
    var location: String,
    var pallets: MutableList<Pallet> = mutableListOf(),
    var capacity: WarehouseCapacity = WarehouseCapacity(),
    @Id
    val id: ObjectId = ObjectId.get(),
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Create a warehouse object from its view model
         */
        fun createWarehouse(wareVm: WarehouseVM): Warehouse {
            return Warehouse(
                wareNo = wareVm.wareNo,
                name = wareVm.name,
                location = wareVm.location,
                capacity = WarehouseCapacity(wareVm.aisle, wareVm.section, wareVm.level)
            )
        }
    }

    override fun toString(): String {
        return "$wareNo ($name)"
    }
}

/**
 * Defines the max capacity of a warehouse
 */
data class WarehouseCapacity(
    var aisle: Int = 300,
    var section: Int = 10,
    var level: Int = 3,
) {
    /**
     * Gets the pallets allowed in the warehouse
     */
    fun getCapacity(): Long = 1L * aisle * section * level
}


