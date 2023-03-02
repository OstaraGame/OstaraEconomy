package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeGoodDemand (val tradeGood: TradeGood, val location: Location, val demandRate: Int, var inventory: Int, val storageClass: Int, var ceilingPrice: Int) {

    public fun unitsDemanded(): Int {
        return storageClass * EconomyEngine.STORAGE_SIZE - inventory
    }
}