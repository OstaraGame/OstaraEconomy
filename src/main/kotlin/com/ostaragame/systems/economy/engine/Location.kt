package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader

data class Location(val name: String, val id: Int, val supply: MutableCollection<TradeGoodSupply>, val demand: MutableCollection<TradeGoodDemand>,
                    val connections: MutableCollection<Connection>,
                    val travelers: MutableCollection<NonPlayerTrader>) {
    override fun toString(): String {
        return "($name)"
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
    fun neighbors() : List<Location> {
        val neighborList = mutableListOf<Location>()
        for ( connection in connections ) {
            if (connection.location1 != this ) {
                neighborList.add(connection.location1)
            } else {
                neighborList.add(connection.location2)
            }
        }
        return neighborList
    }

    fun connectionDistance(connectedLocation: Location) : Float {
        var distance = Float.MAX_VALUE
        //connectionFor(connectedLocation)?.let( distance = connection.distance)
        for ( connection in connections ) {
            if (connection.location1 == connectedLocation || connection.location2 == connectedLocation ) {
                distance = connection.distance
            }
        }
        return distance
    }

    fun connectionFor(connectedLocation: Location) : Connection? {
        for ( connection in connections ) {
            if (connection.location1 == connectedLocation || connection.location2 == connectedLocation ) {
                return connection
            }
        }
        return null
    }
}

