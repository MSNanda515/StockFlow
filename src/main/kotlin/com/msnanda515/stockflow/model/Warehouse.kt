package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
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
}
@Document
data class Warehouse(
    var wareNo: Long,
    var name: String,
    var location: String,
    var pallets: MutableList<Pallet> = mutableListOf(),
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
            )
        }
    }

}


