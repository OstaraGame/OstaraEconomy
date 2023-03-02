package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeMission(val tradeGood:TradeGood, val supply: TradeGoodSupply, val demand: TradeGoodDemand, val unitsDemanded: Int) {
    override fun toString(): String {
        return "(g=${tradeGood.name}, s=${supply.location}, d=${demand.location})"
    }

    fun negotiate(): Int {
        if (supply.currentPrice > tradeGood.cost) {
            return maxOf(supply.currentPrice * 90 / 100, tradeGood.cost)
        }
        return supply.currentPrice
    }
}

