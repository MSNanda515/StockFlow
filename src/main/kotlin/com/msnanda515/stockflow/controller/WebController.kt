package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.model.ItemVM
import com.msnanda515.stockflow.model.WarehouseVM
import com.msnanda515.stockflow.repository.ItemRepository
import com.msnanda515.stockflow.service.ItemService
import com.msnanda515.stockflow.service.WarehouseService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.Valid


@Controller
class WebController(
    private val itemService: ItemService,
    private val warehouseService: WarehouseService,
) {

    /**
     * Home page
     */
    @GetMapping("/")
    fun index(model: Model): String {
        // get objects required for ui
        val wares = warehouseService.getAllWarehouses()
        val selectedWareName = "All Warehouses"
        val items = itemService.getAllItems()

        // pass required objects to model
        model.addAttribute("wares", wares)
        model.addAttribute("selectedWareName", selectedWareName)
        model.addAttribute("items", items)
        return "index"
    }

    /**
     * Create a new warehouse page
     */
    @GetMapping("/warehouse/create")
    fun getCreateWarehouse(model: Model): String {
        var wareVm = WarehouseVM.createVM()
        wareVm.wareNo = warehouseService.getNextWarehouseNo()
        model.addAttribute("ware", wareVm)
        return "createWarehouse"
    }

    /**
     * Creates a warehouse if all info valid, redirects to home page if successful
     */
    @PostMapping("/warehouse/create")
    fun postCreateWarehouse(@Valid @ModelAttribute("ware") wareVm: WarehouseVM,
        bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "createWarehouse"
        } else {
            return try {
                warehouseService.createWarehouse(wareVm)
                "index"
            } catch (exp: AlreadyExistsException) {
                bindingResult.addError(
                    FieldError("ware", "wareNo",
                        exp.message ?: "Warehouse Id should be unique")
                )
                "createWarehouse"
            }
        }
    }

//    @GetMapping("/items/{wareNo}")
//    fun getItemsWarehouse(@PathVariable wareNo: Long, model: Model): String {
//
//    }

    @GetMapping("/items/create")
    fun getCreateItem(model: Model): String {
        var item = ItemVM.createItem()
        var wares = warehouseService.getAllWarehouses()
        val wareExist = wares.isNotEmpty()
        var wareSelected: Int = 0
        model.addAttribute("item", item)
        model.addAttribute("wareExist", wareExist)
        Util.addModelAttributesNavbar(
            model, if (wareExist) wares[wareSelected].name else "Warehouse", wares
        )

        return "createItem"
    }

    @PostMapping("/items/create")
    fun postCreateItem(
        @Valid @ModelAttribute("item") itemVM: ItemVM,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            var wares = warehouseService.getAllWarehouses()
            val wareExist = wares.isNotEmpty()
            model.addAttribute("item", itemVM)
            model.addAttribute("wareExist", wareExist)
            Util.addModelAttributesNavbar(
                model, "ware", wares
            )
            return "createItem"
        }
//        else {
//            return try {
//                warehouseService.createWarehouse(wareVm)
//                "index"
//            } catch (exp: AlreadyExistsException) {
//                bindingResult.addError(
//                    FieldError("ware", "wareNo",
//                        exp.message ?: "Warehouse Id should be unique")
//                )
//                "createWarehouse"
//            }
//        }
        return "index"
    }
}