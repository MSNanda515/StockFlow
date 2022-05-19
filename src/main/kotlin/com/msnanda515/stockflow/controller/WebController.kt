package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.exception.DoesNotExistsException
import com.msnanda515.stockflow.exception.OutOfCapacityException
import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemInventoryVM
import com.msnanda515.stockflow.model.ItemVM
import com.msnanda515.stockflow.model.WarehouseVM
import com.msnanda515.stockflow.service.ItemService
import com.msnanda515.stockflow.service.WarehouseService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.Valid

/**
 * Controller for the web application
 */
@Controller
class WebController(
    private val itemService: ItemService,
    private val warehouseService: WarehouseService,
) {

    /**
     * Home page
     */
    @GetMapping(value = ["/", ""])
    fun index(model: Model): String {
        // get objects required for ui
        val wares = warehouseService.getAllWarehouses()
        val selectedWareName = "All Warehouses"
        val items = itemService.getAllActiveItems()
        val itemVms = items.map { ItemVM.prepareVM(it) }

        // pass required objects to model
        model.addAttribute("wares", wares)
        model.addAttribute("selectedWareName", selectedWareName)
        model.addAttribute("items", itemVms)
        return "index"
    }

    /**
     * Create a new warehouse page
     */
    @GetMapping("/warehouse/create")
    fun getCreateWarehouse(model: Model): String {
        var wareVm = WarehouseVM.createVM()
        val wares = warehouseService.getAllWarehouses()
        wareVm.setDefaultValues(warehouseService.getNextWarehouseNo())
        model.addAttribute("ware", wareVm)
        Util.addModelAttributesNavbar(
            model, "Warehouse", wares
        )
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
                "redirect:/"
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
        item.itemNo = itemService.getNextItemNo()
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
    fun postCreateItem(@Valid @ModelAttribute("item") itemVM: ItemVM, bindingResult: BindingResult,
                       model: Model): String {

        fun setCustFailModel() {
            var wares = warehouseService.getAllWarehouses()
            val wareExist = wares.isNotEmpty()
            model.addAttribute("item", itemVM)
            model.addAttribute("wareExist", wareExist)
            Util.addModelAttributesNavbar(
                model, "Warehouse", wares
            )
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "createItem"
        }
        try {
            itemService.createItem(itemVM)
            return "redirect:/"
        } catch (exp: OutOfCapacityException) {
            bindingResult.addError(
                FieldError("item", "units",
                    exp.message ?: "Not enough Capacity in warehouse")
            )
            setCustFailModel()
            return "createItem"
        } catch (exp: AlreadyExistsException) {
            bindingResult.addError(FieldError("item", "itemNo",
                exp.message ?: "Item No should be unique")
            )
            setCustFailModel()
            return "createItem"
        }
    }

    @GetMapping("/items/inventory/create")
    fun getCreateInventory(model: Model): String {
        var items = itemService.getAllActiveItems()
        var wares = warehouseService.getAllWarehouses()

        val selectedItemNo = if (items.isNotEmpty()) items[0].itemNo else 0
        val selectedWare = 0
        // Create default inventoryVM
        var inventory = ItemInventoryVM(selectedItemNo, if (wares.isEmpty()) 0 else wares[selectedWare].wareNo,1)

        model.addAttribute("itemsExist", items.isNotEmpty())
        model.addAttribute("waresExist", wares.isNotEmpty())
        model.addAttribute("items", items)
        model.addAttribute("inventory", inventory)
        Util.addModelAttributesNavbar(
            model, if (wares.isNotEmpty()) wares[selectedWare].name else "Warehouse", wares
        )
        return "createInventory"
    }

    @PostMapping("/items/inventory/create")
    fun postCreateInventory(@Valid @ModelAttribute("inventory") inventoryVM: ItemInventoryVM,
                            bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            var wares = warehouseService.getAllWarehouses()
            var items = itemService.getAllActiveItems()
            val waresExist = wares.isNotEmpty()
            val itemsExist = items.isNotEmpty()
            model.addAttribute("inventory", inventoryVM)
            model.addAttribute("waresExist", waresExist)
            model.addAttribute("itemsExist", itemsExist)
            model.addAttribute("items", items)
            Util.addModelAttributesNavbar(
                model, "Warehouse", wares
            )
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "createInventory"
        }
        try {
            itemService.createInventory(inventoryVM)
            return "redirect:/"
        } catch (exp: OutOfCapacityException) {
            bindingResult.addError(
                FieldError("inventory", "units",
                    exp.message ?: "Not enough Capacity in warehouse")
            )
            setCustFailModel()
            return "createInventory"
        } catch (exp: DoesNotExistsException) {
            bindingResult.addError(FieldError("inventory", "itemNo",
                exp.message ?: "Item No/ Warehouse No does not exist")
            )
            setCustFailModel()
            return "createInventory"
        }
    }
}