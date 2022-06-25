package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ReceivingVM (
    val id: ObjectId,
    val from: Long,
    val to: Long,
    val items: MutableList<ItemVM> = mutableListOf(),
    val shippedOn: LocalDateTime = LocalDateTime.now()  // TODO: Add support for correct date time
) {
    /**
     * gets the string describing the receiving object for UI
     */
    fun getDisplayStr(): String {
        val sid = id.toString().takeLast(6)
        return "Shipment Id: $sid, From: $from, To: $to, Items: ${items.size}, Shipped On: " +
                shippedOn.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

data class ReceivingRequestVM (
    val shipmentIds: String,
    val at: Long,
)