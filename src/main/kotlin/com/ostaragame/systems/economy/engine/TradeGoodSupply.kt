package com.ostaragame.systems.economy.engine

data class TradeGoodSupply(val tradeGood: TradeGood, val location: Location, var inventoryCurrent: Float, var inventoryMax: Float,
                           val restockRateBase: Float, var restockRate: Float)
