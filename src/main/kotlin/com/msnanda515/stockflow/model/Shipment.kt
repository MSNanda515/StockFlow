package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import javax.validation.constraints.Min

/**
 * Track shipments for items
 */
data class Shipment(
    val from: Long,
    val to: Long,
    val units: Int = 0,
    val id: ObjectId = ObjectId.get(),
)

/**
 * View model for Shipment
 */
data class ShipmentVM(
    @field:Min(1)
    var from: Long,
    @field:Min(1)
    var to: Long,
    var itemNos: String = "",
    var itemUnits: String = "",
)