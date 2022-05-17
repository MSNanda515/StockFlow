package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

class WarehouseVM(
    val wareNo: Long,
    var name: String,
    var location: String,
)
@Document
data class Warehouse(
    var wareNo: Long,
    var name: String,
    var location: String,
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
                location = wareVm.location
            )
        }
    }

}


