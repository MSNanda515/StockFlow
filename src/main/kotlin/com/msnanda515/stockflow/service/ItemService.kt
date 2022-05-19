package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemStatus
import com.msnanda515.stockflow.model.ItemVM
import com.msnanda515.stockflow.model.Pallet
import com.msnanda515.stockflow.repository.ItemRepository
import org.springframework.stereotype.Service

@Service
class ItemService(
    val itemRepository: ItemRepository,
    var warehouseService: WarehouseService,
) {
    /**
     * Gets all active items
     */
    fun getAllActiveItems(): List<Item> {
        return itemRepository.findAllByStatus(ItemStatus.ACTIVE)
    }

    fun createItem(itemVm: ItemVM) {
        var itemExists = itemRepository.findAllByItemNo(itemVm.itemNo).isNotEmpty()
        if (itemExists) {
            throw AlreadyExistsException("Item with ${itemVm.itemNo} already exists")
        }

        var item = Item.createItem(itemVm)
        // get the pallets required for the item
        var palletsRequired = item.palletsRequired(itemVm.units)
        val palletLocs = warehouseService.getAvailablePalletPos(palletsRequired, itemVm.wareNo)
        val pallets = Pallet.createPalletsForItem(item.itemNo, itemVm.units, palletLocs, palletsRequired,
            item.department.palleteCap)
        item.pallets.addAll(pallets)
        warehouseService.addPalletsToWarehouse(itemVm.wareNo, pallets)
        itemRepository.save(item)
    }

    /**
     * Returns an available item no to be configured as the default value for the form
     */
    fun getNextItemNo(): Long {
        return (itemRepository.findTopByOrderByItemNoDesc()?.itemNo ?: 0) + 1
    }

}