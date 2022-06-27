package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.ItemVM
import com.msnanda515.stockflow.model.Warehouse
import com.msnanda515.stockflow.model.WarehouseVM
import com.msnanda515.stockflow.service.ItemService
import com.msnanda515.stockflow.service.WarehouseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/warehouses")
class WarehouseController(
    val warehouseService: WarehouseService,
    val itemService: ItemService,
) {

    /**
     * Gets all the warehouses
     */
    @GetMapping()
    fun getAllWarehouses(): ResponseEntity<List<Warehouse>> {
        return ResponseEntity.ok(warehouseService.getAllWarehouses())
    }

    /**
     * Create a warehouse
     */
    @PostMapping(
        consumes = ["application/json"],
        path = ["/create"]
    )
    fun createWarehouse(@RequestBody wareVm: WarehouseVM):
            ResponseEntity<WarehouseVM> {
        warehouseService.createWarehouse(wareVm)
        return ResponseEntity.ok(wareVm)
    }

    /**
     * Gets the CSV data file for the warehouse
     */
    @GetMapping("/data/csv/{wareNo}")
    fun getCsvDataWarehouse(@PathVariable wareNo: Long): ResponseEntity<Map<String, String>> {
        val csvData = itemService.getCsvForWarehouse(wareNo)
        val respData = mutableMapOf<String, String>()
        respData["data"] = csvData
        respData["filename"] = "stockflow_warehouse_${wareNo}_${System.currentTimeMillis()%100000}.csv"
        return ResponseEntity.ok(respData);
    }
}