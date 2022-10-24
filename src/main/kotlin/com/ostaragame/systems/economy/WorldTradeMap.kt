package com.ostaragame.systems.economy

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import com.ostaragame.systems.economy.actors.Traits
import com.ostaragame.systems.economy.engine.*

/* All the locations and their connections and demands */
object WorldTradeMap {
    var locations:MutableMap<String,Location> = mutableMapOf()


    val traderOffMapHome: Location = Location("Trader Off Map Starting Location", -1,
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
    )
    private var generatedlocationId:Int = 0
    //TODO singleton ID generation not working
    fun getNextLocationId():Int {
            synchronized(generatedlocationId) {
                generatedlocationId = generatedlocationId++
                return generatedlocationId
            }
        }

    fun findRouteForTradeMission(startingLocation: Location, tradeMission: TradeMission, traits: Traits) : ArrayDeque<RouteLeg> {

        val newRoute = ArrayDeque<RouteLeg>()
        val firstPartOfRoute = findRoute(startingLocation,
            tradeMission.supply.location,
            traits,
            NonPlayerTrader.TraderActivity.PICKUP,
            newRoute)

        val secondPartOfRoute = findRoute(
            tradeMission.supply.location,
            tradeMission.demand.location,
            traits,
            NonPlayerTrader.TraderActivity.DROP_OFF,
            newRoute
        )

        return newRoute
    }

    //TODO This needs to be rewritten, and actually use a min queue, and a better way to assemble the final route.
    fun findRoute(
        startingLocation: Location,
        destinationLocation: Location,
        traits: Traits,
        traderActivity: NonPlayerTrader.TraderActivity,
        route: ArrayDeque<RouteLeg>
    ) : ArrayDeque<RouteLeg> {

        //Dijkstra
        val dist:  MutableMap<Location,Float> = mutableMapOf()
        val prev:  MutableMap<Location,Location> = mutableMapOf()
        val queue: MutableList<Location> = mutableListOf()

        for( vertex in locations.values ) {
            dist[vertex] = Float.MAX_VALUE
            queue.add(vertex)
        }
        dist[startingLocation] = 0.0F

        var nextNearestLocationU = startingLocation
        while (queue.isNotEmpty()) {
            //TODO Replace queue with a min-priority queue sorted by dist to remove the following inefficient search
            var smallestValue = Int.MAX_VALUE
            for ( location in queue ) {
                val distanceValueForLocation = dist[location]!!
                if (distanceValueForLocation < smallestValue) {
                    nextNearestLocationU = location
                }
            }
            if ( nextNearestLocationU == destinationLocation ) {
                break
            }
            queue.remove(nextNearestLocationU)

            for ( neighborV in nextNearestLocationU.neighbors()) {
                if ( queue.contains(neighborV) ) {
                    val altDistance = dist[nextNearestLocationU]!! + nextNearestLocationU.connectionDistance(neighborV)
                    if (altDistance < dist[neighborV]!!) {
                        dist[neighborV] = altDistance
                        prev[neighborV] = nextNearestLocationU
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

//        if (location == destinationLocation) {
//            //TODO Some sort of self connection or other way of not needing a connection for a route that is to the same location?
//            route.add(
//                RouteLeg(
//                    Connection(
//                        location, location, 0.0F, Infrastructure.NONE, Terrain.PLAINS, Weather.CLEAR,
//                        mutableListOf()
//                    ), location, traderActivity
//                )
//            )
//        } else {
            for ((index, location) in shortestPath.withIndex()) {
                if (index + 1 <= shortestPath.size - 1) {
                    val nextLocation = shortestPath[index + 1]
                    val activityAtStop =
                        if (nextLocation == destinationLocation) traderActivity else NonPlayerTrader.TraderActivity.NONE
                    val connection: Connection? = location.connectionFor(nextLocation)

                    connection?.let { route.add(RouteLeg(it, nextLocation, activityAtStop)) }

                } else {
                    //the last item in the route was covered in the location.connectionFor(nextLocation) or by the initial IF. This should not happen...
                    println("The Last Item in the route: $location")
                }
            }
//        }

        return route
    }
 }