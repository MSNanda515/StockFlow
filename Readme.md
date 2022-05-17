# Stock Flow
Track the flow of your goods using this Inventory tracking web application.

## Dev Notes
- Model: 
  - Item: name, itemNo, department, status,
  - Pallet: palletId, warehouseId, aisle, section, level, 
     content: Pair(item, units)
  - Warehouse: pallets
  - Pallet Type: capacity

- Item can either be active or inactive
- Items organized as pallets with units, each palette has a max cap for each department
- Units shipped from warehouse to warehouse as pallets
- Use Cases: 
  - Find all palettes for item in warehouse
  - Find all 

TODO:
Find what happens when duplicate fields for unique warehouse