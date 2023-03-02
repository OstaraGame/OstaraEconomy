package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeGoodSupply(val tradeGood: TradeGood, val location: Location, var inventoryCurrent: Int, var inventoryMax: Int,
                           val restockRateBase: Int, var restockRate: Int, var currentPrice: Int)
