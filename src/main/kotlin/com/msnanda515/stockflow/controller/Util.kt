package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.ItemVM
import com.msnanda515.stockflow.model.Warehouse
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
                                   wares: List<Warehouse>, selectedWareName: String = "Warehouse",
                                   selectedWareNo: Long = 0, isWareSelected: Boolean = false) {
            addModelAttributesNavbar(model, selectedWareName, wares)
            model.addAttribute("items", itemVms)
            model.addAttribute("isWareSelected", isWareSelected) // determine if edit button shown for ware
            model.addAttribute("selectedWareNo", selectedWareNo) // determine if edit button shown for ware
        }
    }
}