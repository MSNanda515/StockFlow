# Stock Flow
Track the flow of your goods using this Inventory tracking web application.

## Dev Notes
- Model: 
  - Item: name, itemNo, department, status,
  - Pallet: palletId, aisle, section, level, 
     content: Pair(item, units)
  - Warehouse: Items, pallets
  - Pallet Type: capacity