package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader

data class Location(val name: String, val id: Int, val supply: MutableCollection<TradeGoodSupply>, val demand: MutableCollection<TradeGoodDemand>,
                    val connections: MutableCollection<Connection>,
                    val travelers: MutableCollection<NonPlayerTrader>) {
    override fun toString(): String {
        return "($name)"
    }
}

