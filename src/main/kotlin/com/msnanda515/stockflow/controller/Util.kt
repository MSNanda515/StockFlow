package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemStatus
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
    }
}