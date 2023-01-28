package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import kotlinx.serialization.Serializable

@Serializable
data class Location(val name: String, val id: Int, val supply: MutableList<TradeGoodSupply>, val demand: MutableList<TradeGoodDemand>,
                    val connections: MutableList<Connection>,
                    val travelers: MutableList<NonPlayerTrader>,
                    val x: Double, val y: Double) {
    override fun toString(): String {
        return "($name)"
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    fun neighbors() : List<String> {
        val neighborList = mutableListOf<String>()
        for ( connection in connections ) {
            if (connection.location1 != this.name ) {
                neighborList.add(connection.location1)
            } else {
                neighborList.add(connection.location2)
            }
        }
        return neighborList
    }

    fun connectionDistance(connectedLocation: Location) : Double {
        var distance = Double.MAX_VALUE
        //connectionFor(connectedLocation)?.let( distance = connection.distance)
        for ( connection in connections ) {
            if (connection.location1 == connectedLocation.name || connection.location2 == connectedLocation.name) {
                distance = connection.distance
            }
        }
        return distance
    }

    fun connectionFor(connectedLocation: Location) : Connection? {
        for ( connection in connections ) {
            if (connection.location1 == connectedLocation.name || connection.location2 == connectedLocation.name) {
                return connection
            }
        }
        return null
    }
}

