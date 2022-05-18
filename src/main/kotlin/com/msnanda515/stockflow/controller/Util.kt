package com.msnanda515.stockflow.controller

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
    }
}