package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeMission(val tradeGood:TradeGood, val supply: TradeGoodSupply, val demand: TradeGoodDemand) {
    override fun toString(): String {
        return "(g=${tradeGood.name}, s=${supply.location}, d=${demand.location})"
    }
}

