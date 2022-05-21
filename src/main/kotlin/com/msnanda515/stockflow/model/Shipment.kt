package com.msnanda515.stockflow.model

import org.bson.types.ObjectId

data class Shipment(
    val from: Long?,
    val to: Long?,
    val units: Int?,
    val id: ObjectId = ObjectId.get(),
)

data class ShipmentItemVM(
    var itemSelected: Boolean,
    var itemNo: Long,
    var units: Int,
) {
    companion object {
        /**
         * Prepares default shipment item view model
         */
        fun prepareShipmentVM(itemVm: ItemVM): ShipmentItemVM {
            return ShipmentItemVM(false, itemVm.itemNo, 0, )
        }
    }
}

data class ShipmentVM(
    var from: Long,
    var to: Long,
    var items: List<ShipmentItemVM>,
)