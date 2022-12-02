package com.ostaragame.systems.economy

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import com.ostaragame.systems.economy.actors.Traits
import com.ostaragame.systems.economy.engine.*
import java.util.PriorityQueue

/* All the locations and their connections and demands */
object WorldTradeMap {
    var locations:MutableMap<String,Location> = mutableMapOf()
    var connections:MutableMap<String,Connection> = mutableMapOf()

    val traderOffMapHome: Location = Location("Trader Off Map Starting Location", -1,
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
    )
    private var generatedlocationId:Int = 0
    //TODO singleton ID generation not working
    fun getNextLocationId():Int {
            synchronized(this) {
                generatedlocationId = generatedlocationId++
                return generatedlocationId
            }
        }

    fun findRouteForTradeMission(startingLocation: Location, tradeMission: TradeMission, traits: Traits) : ArrayDeque<RouteLeg> {

        val newRoute = ArrayDeque<RouteLeg>()
        findRoute(startingLocation,
            tradeMission.supply.location,
            traits,
            NonPlayerTrader.TraderActivity.PICKUP,
            newRoute)

        findRoute(
            tradeMission.supply.location,
            tradeMission.demand.location,
            traits,
            NonPlayerTrader.TraderActivity.DROP_OFF,
            newRoute
        )

        return newRoute
    }

    //TODO Possible further optimization by implementing a Priority Queue that can reorder nodes instead of needing to remove and re-add (research runtime improvements)
    // TODO A better way to assemble the final route?
    private fun findRoute(
        startingLocation: Location,
        destinationLocation: Location,
        traits: Traits,
        traderActivity: NonPlayerTrader.TraderActivity,
        route: ArrayDeque<RouteLeg>
    ) : ArrayDeque<RouteLeg> {

        //Dijkstra
        val distance:  MutableMap<Location,Double> = mutableMapOf()
        val prev:  MutableMap<Location,Location> = mutableMapOf()

        val compareByDistance: Comparator<Location> = compareBy { distance[it] }

        val queue: PriorityQueue<Location> = PriorityQueue(locations.size, compareByDistance)

        for( location in locations.values ) {
            distance[location] = Double.MAX_VALUE
        }

        distance[startingLocation] = 0.0
        queue.add(startingLocation)

        var nextNearestLocationU: Location
        while (queue.isNotEmpty()) {

            nextNearestLocationU = queue.remove()
            if ( nextNearestLocationU == destinationLocation ) {
                break
            }

            for ( neighborV in nextNearestLocationU.neighbors()) {
                if ( queue.contains(locations[neighborV]) || distance[locations[neighborV]] == Double.MAX_VALUE) {
                    val altDistance = distance[nextNearestLocationU]!! + nextNearestLocationU.connectionDistance(locations[neighborV]!!)
                    if (altDistance < distance[locations[neighborV]]!!) {
                        distance[locations[neighborV]!!] = altDistance
                        prev[locations[neighborV]!!] = nextNearestLocationU
                        queue.add(locations[neighborV])
                    }
                }
            }
        }

        val shortestPath:ArrayDeque<Location> = ArrayDeque()
        var routeStop: Location? = destinationLocation
        if ( prev[routeStop] != null || routeStop == startingLocation ) {
            while (routeStop != null) {
                shortestPath.addFirst(routeStop)
                routeStop = prev[routeStop]
            }
        }

        for ((index, location) in shortestPath.withIndex()) {
            if (shortestPath.size == 1) {
                route.add(RouteLeg(SelfConnection, location, traderActivity))
            } else if (index + 1 <= shortestPath.size - 1) {
                val nextLocation = shortestPath[index + 1]
                val activityAtStop =
                    if (nextLocation == destinationLocation) traderActivity else NonPlayerTrader.TraderActivity.NONE
                val connection: Connection? = location.connectionFor(nextLocation)

                connection?.let { route.add(RouteLeg(it, nextLocation, activityAtStop)) }

            } else {
                //the last item in the route was covered in the location.connectionFor(nextLocation) or by the initial IF. This should not happen...
                //println("The Last Item in the route: $location")
            }
        }

        return route
    }
 }