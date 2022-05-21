package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import javax.validation.constraints.Min

data class Shipment(
    val from: Long?,
    val to: Long?,
    val units: Int?,
    val id: ObjectId = ObjectId.get(),
)


data class ShipmentVM(
    @field:Min(1)
    var from: Long,
    @field:Min(1)
    var to: Long,
    var itemNos: String = "",
    var itemUnits: String = "",
)