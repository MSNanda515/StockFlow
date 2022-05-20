package com.msnanda515.stockflow.model

import org.bson.types.ObjectId

data class Shipment(
    val from: Long?,
    val to: Long?,
    val units: Int?,
    val id: ObjectId = ObjectId.get(),
)

data class ShipmentItemVM(
    val itemSelected: Boolean,
    val itemNo: Long,
    val item: ItemVM?,
    val units: Int,
) {
    companion object {
        /**
         * Prepares default shipment item view model
         */
        fun prepareShipmentVM(itemVm: ItemVM): ShipmentItemVM {
            return ShipmentItemVM(false, itemVm.itemNo, itemVm, 0)
        }
    }
}

data class ShipmentVM(
    val from: Long,
    val to: Long,
    val items: List<ShipmentItemVM>,
)