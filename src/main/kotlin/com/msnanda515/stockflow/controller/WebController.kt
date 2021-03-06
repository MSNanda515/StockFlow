package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.exception.*
import com.msnanda515.stockflow.model.*
import com.msnanda515.stockflow.service.ItemService
import com.msnanda515.stockflow.service.WarehouseService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.validation.Valid

/**
 * Controller for the web application
 */
@Controller
class WebController(
    private val itemService: ItemService,
    private val warehouseService: WarehouseService,
    private val env: Environment,
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
        Util.addModelAttributesDash(model, itemVms, wares,  false, null, selectedWareName,)
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
        model.addAttribute("googleApiKey", env.getProperty("mnanda.google.api.key"))

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
        bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            val wares = warehouseService.getAllWarehouses()
            model.addAttribute("googleApiKey", env.getProperty("mnanda.google.api.key"))
            Util.addModelAttributesNavbar(
                model, "Warehouse", wares
            )
        }
        if (bindingResult.hasErrors()) {
            setCustFailModel()
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

    /**
     * Create an item
     */
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

    /**
     * Creates an item if all parameters valid
     */
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

    /**
     * Creates inventory for existing items
     */
    @GetMapping("/items/inventory/create")
    fun getCreateInventory(@RequestParam(required = false, name = "itemNo") itemNoReq: Long? = 0,
                           @RequestParam(required = false, name = "wareNo") wareNoReq: Long? = 0,
                           model: Model): String {
        var items = itemService.getAllActiveItems()
        var wares = warehouseService.getAllWarehouses()
        val itemNo = itemNoReq ?: if(items.isNotEmpty()) items[0].itemNo else 0
        val wareNo = wareNoReq ?: if(wares.isNotEmpty()) wares[0].wareNo else 0
        val selectedWare = wares.find { it.wareNo == wareNo }
        // Create default inventoryVM
        var inventory = ItemInventoryVM(itemNo, wareNo, 1)

        model.addAttribute("itemsExist", items.isNotEmpty())
        model.addAttribute("waresExist", wares.isNotEmpty())
        model.addAttribute("items", items)
        model.addAttribute("inventory", inventory)
        Util.addModelAttributesNavbar(
            model, selectedWare?.name ?: "All Warehouses", wares
        )
        return "createInventory"
    }

    /**
     * Adds inventory if all params valid
     */
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
            return "redirect:/warehouse/${inventoryVM.wareNo}"
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

    /**
     * Gets all items in the warehouse
     */
    @GetMapping("/warehouse/{wareNo}")
    fun getWarehouseItems(@PathVariable wareNo: Long,  model: Model): String {
        val items = itemService.getActiveItemsInWarehouse(wareNo)
        // get objects required for ui
        val wares = warehouseService.getAllWarehouses()
        val itemVms = items.map { ItemVM.prepareVM(it) }
        val selectedWare = wares.find { it.wareNo==wareNo }
        val weatherApiKey = env.getProperty("mnanda.weather.api.key")
        model.addAttribute("weatherApiKey", weatherApiKey)

        // pass required objects to model
        Util.addModelAttributesDash(
            model = model, itemVms = itemVms, wares = wares,
            selectedWareName = selectedWare?.name ?: "Warehouse" ,
            selectedWare = if (selectedWare != null) WarehouseVM.prepareVM(selectedWare) else null ,
            isWareSelected = true,
        )

        return "index"
    }

    /**
     * Edit an item
     */
    @GetMapping("/items/edit/{itemNo}")
    fun getEditItem(@PathVariable itemNo: Long, @RequestParam(required = false, name = "wareNo") wareNoReq: Long?,
                    model: Model): String {
        val item = itemService.getItem(itemNo)
        val wares = warehouseService.getAllWarehouses()
        Util.addModelAttributesEditItem(model, item, wares, wareNoReq ?: 0)
        return "editItem"
    }

    @PostMapping("/items/edit")
    fun postEditItem(@Valid @ModelAttribute("inventory") itemVM: ItemVM,
                          bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            val item = itemService.getItem(itemVM.itemNo)
            val wares = warehouseService.getAllWarehouses()
            Util.addModelAttributesEditItem(model, item, wares)
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "editItem"
        }

        try {
            itemService.editItem(itemVM)
        } catch (exp: DoesNotExistsException) {
            bindingResult.addError(FieldError("item", "itemNo",
                exp.message ?: "Item No does not exist")
            )
            setCustFailModel()
            return "editItem"
        } catch (exp: DoesNotMatchException) {
            bindingResult.addError(FieldError("item", "department",
                exp.message ?: "department does not exist")
            )
            setCustFailModel()
            return "editItem"
        }

        return if (itemVM.wareNo == 0L) "redirect:/" else "redirect:/warehouse/${itemVM.wareNo}"
    }

    /**
     * Delete an item
     */
    @GetMapping("/items/delete/{itemNo}")
    fun getDeleteItem(@PathVariable itemNo: Long, @RequestParam(required = false, name = "wareNo") wareNoReq: Long?,
                    model: Model): String {
        val items = itemService.getAllActiveItems()
        val item = items.find { it.itemNo == itemNo }
            ?: throw DoesNotExistsException("Item with $itemNo does not exist")
        // todo: verify flow
        val wares = warehouseService.getAllWarehouses()
        Util.addModelAttributesEditItem(model, item, wares, wareNoReq ?: 0)
        model.addAttribute("itemsExist", items.isNotEmpty())
        return "deleteItem"
    }

    @PostMapping("/items/delete")
    fun postDeleteItem(@Valid @ModelAttribute("inventory") itemVM: ItemVM,
                          bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            val item = Item.createItem(itemVM)
            val wares = warehouseService.getAllWarehouses()
            Util.addModelAttributesEditItem(model, item, wares, itemVM.wareNo)
            model.addAttribute("item", itemVM)
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "deleteItem"
        }
        try {
            itemService.deleteItem(itemVM)
        } catch(exp: DoesNotExistsException) {
            bindingResult.addError(FieldError("item", "itemNo",
                exp.message ?: "department does not exist")
            )
            setCustFailModel()
            return "deleteItem"
        }
        return if (itemVM.wareNo == 0L) "redirect:/" else "redirect:/warehouse/${itemVM.wareNo}"
    }

    /**
     * Edit a warehouse
     */
    @GetMapping("/warehouse/edit/{wareNo}")
    fun getEditWarehouse(@PathVariable wareNo: Long, model: Model): String {
        val wares = warehouseService.getAllWarehouses()
        val ware = wares.find { it.wareNo == wareNo }
            ?: return "redirect:/" // Todo: Toasts

        Util.addModelAttributesNavbar(model, ware.name, wares)
        model.addAttribute("ware", WarehouseVM.prepareVM(ware))
        model.addAttribute("minCap", ware.pallets.size)
        return "editWarehouse"
    }

    @PostMapping("/warehouse/edit")
    fun postEditWarehouse(@Valid @ModelAttribute("ware") wareVm: WarehouseVM,
                          bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {

        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "editWarehouse"
        }

        try {
            warehouseService.editWarehouse(wareVm)
            return "redirect:/warehouse/${wareVm.wareNo}"
        } catch (exp: WarehouseCapacityException) {
            bindingResult.addError(FieldError("ware", "aisle",
                exp.message ?: "Warehouse capacity exception")
            )
            setCustFailModel()
            return "editWarehouse"
        } catch (exp: DoesNotExistsException) {
            bindingResult.addError(FieldError("ware", "wareNo",
                exp.message ?: "ware no does not exist")
            )
            setCustFailModel()
            return "editWarehouse"
        }
    }

    /**
     * Send a shipment
     */
    @GetMapping("/warehouse/shipment/{wareNo}")
    fun getSendShipment(@PathVariable wareNo: Long, model: Model): String {
        val wares = warehouseService.getAllWarehouses()
        val ware = wares.find { it.wareNo == wareNo }
            ?: return "redirect:/" // Todo: Toasts
        val items = itemService.getActiveItemsInWarehouse(wareNo)
            .map { ItemVM.prepareVMForWarehouse(it, wareNo) }

        if (wares.size < 2) {
            // cannot ship an item to itself
            model.addAttribute("wareSelected", false)
            return "shipItem"
        }


        val otherWares = wares.filter { it.wareNo != wareNo }
        val shipment = ShipmentVM(wareNo, otherWares[0].wareNo)
        Util.addModelAttributesShipItems(model, ware.name, wares, otherWares, shipment, items, true,
            items.isNotEmpty())
        return "shipItem"
    }

    /**
     * Post mapping for send shipment endpoint
     */
    @PostMapping("/warehouse/shipment")
    fun postSendShipment(@Valid @ModelAttribute("shipment") shipmentVM: ShipmentVM,
                          bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            val wareNo = shipmentVM.from
            val wares = warehouseService.getAllWarehouses()
            val ware = wares.find {it.wareNo == wareNo} ?: throw InvalidRequestException("Warehouse does not exist")
            val items = itemService.getActiveItemsInWarehouse(wareNo)
                .map { ItemVM.prepareVMForWarehouse(it, wareNo) }
            // todo: set units according to previous value
            val otherWares = wares.filter { it.wareNo != wareNo }
            val shipment = ShipmentVM(wareNo, otherWares[0].wareNo)
            Util.addModelAttributesShipItems(model, ware.name, wares, otherWares, shipment, items, true,
                items.isNotEmpty())
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "shipItem"
        }

        try {
            if (shipmentVM.itemNos != "")
                itemService.shipItems(shipmentVM)
            return "redirect:/warehouse/${shipmentVM.from}"
        } catch(exp: Exception) {
            if (exp !is java.lang.NumberFormatException && exp !is InvalidRequestException)
                throw exp

            bindingResult.addError(FieldError("shipment", "itemNos",
                exp.message ?: "Something went wrong")
            )
            setCustFailModel()
            return "shipItem"
        }

    }

    /**
     * Send a shipment
     */
    @GetMapping("/warehouse/receive/{wareNo}")
    fun getReceiveShipment(@PathVariable wareNo: Long, model: Model): String {
        val wares = warehouseService.getAllWarehouses()
        val ware = wares.find { it.wareNo == wareNo }
            ?: return "redirect:/" // Todo: Toasts
        val receiving = itemService.getItemsInReceivingForWarehouse(wareNo)
        val receive = ReceivingRequestVM("", wareNo)

        Util.addModelAttributesReceiveItem(model, ware.name, wares, receive, receiving)
        return "receiveItem"
    }

    @PostMapping("/warehouse/receive")
    fun postReceiveShipment(@Valid @ModelAttribute("receive") receivingRequest: ReceivingRequestVM,
                         bindingResult: BindingResult, model: Model): String {
        fun setCustFailModel() {
            getReceiveShipment(receivingRequest.at, model)
        }

        if (bindingResult.hasErrors()) {
            // Prepare the context for model and show the errors UI
            setCustFailModel()
            return "receiveItem"
        }

        try {
            if (receivingRequest.shipmentIds != "")
                itemService.receiveShipment(receivingRequest.shipmentIds, receivingRequest.at)
            return "redirect:/warehouse/${receivingRequest.at}"
        } catch (exp: InvalidRequestException) {
            bindingResult.addError(FieldError("receive", "shipmentIds",
                exp.message ?: "Something went wrong")
            )
            setCustFailModel()
            return "receiveItem"
        }
    }
}