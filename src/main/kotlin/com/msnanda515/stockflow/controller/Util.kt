package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.*
import org.springframework.ui.Model

class Util {
    companion object {
        /**
         * Adds the model attributes required for the navbar
         */
        fun addModelAttributesNavbar(model: Model, selectedWareName: String, wares: List<Warehouse>) {
            model.addAttribute("selectedWareName", selectedWareName)
            model.addAttribute("wares", wares)
        }

        /**
         * Adds the ui attributes required for dashboard
         */
        fun addModelAttributesDash(model: Model, itemVms: List<ItemVM>,
                                   wares: List<Warehouse>, isWareSelected: Boolean = false, selectedWare: WarehouseVM?,
                                   selectedWareName: String = "Warehouse",) {
            addModelAttributesNavbar(model, selectedWareName, wares)
            model.addAttribute("items", itemVms)
            model.addAttribute("isWareSelected", isWareSelected) // determine if edit button shown for ware
            if (isWareSelected && selectedWare != null) {
                model.addAttribute("selectedWare", selectedWare) // determine if edit button shown for ware
            }
        }

        /**
         * Adds the ui attributes required for edit item
         */
        fun addModelAttributesEditItem(model: Model, item: Item, wares: List<Warehouse>, wareNoRedirect: Long = 0) {
            val itemVm = ItemVM.prepareVM(item)
            itemVm.wareNo = wareNoRedirect
            val wareExist = wares.isNotEmpty()
            val wareSelected: Int = 0

            model.addAttribute("item", itemVm)
            model.addAttribute("isItemActive", item.status == ItemStatus.ACTIVE)
            Util.addModelAttributesNavbar(
                model, if (wareExist) wares[wareSelected].name else "Warehouse", wares
            )
        }

        /**
         * Add model attributes required for the ship items page
         */
        fun addModelAttributesShipItems(model: Model, selectedWareName: String, wares: List<Warehouse>,
                                        toWares: List<Warehouse>, shipment: ShipmentVM, items: List<ItemVM>,
                                        wareSelected: Boolean, itemsExist: Boolean,) {
            addModelAttributesNavbar(model, selectedWareName, wares)
            model.addAttribute("toWares", toWares)
            model.addAttribute("shipment", shipment)
            model.addAttribute("items", items)
            model.addAttribute("wareSelected", wareSelected)
            model.addAttribute("itemsExist", itemsExist)
        }
    }
}