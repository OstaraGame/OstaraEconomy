package com.ostaragame.systems.economy

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import com.ostaragame.systems.economy.actors.Traits
import com.ostaragame.systems.economy.engine.*
import java.util.PriorityQueue
import java.util.Stack
import kotlin.math.abs

/* All the locations and their connections and demands */
object WorldTradeMap {
    var locations:MutableMap<String,Location> = mutableMapOf()
    var connections:MutableMap<String,Connection> = mutableMapOf()

    val traderOffMapHome: Location = Location("Trader Off Map Starting Location", -1,
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), 0.0,0.0
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
        findRouteAStar(startingLocation,
            tradeMission.supply.location,
            traits,
            NonPlayerTrader.TraderActivity.PICKUP,
            newRoute)

        findRouteAStar(
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


    private fun findRouteAStar(
        start: Location,
        goal: Location,
        traits: Traits,
        traderActivity: NonPlayerTrader.TraderActivity,
        route: ArrayDeque<RouteLeg>
    ) : ArrayDeque<RouteLeg> {

        //A*
        val cost_so_far:  MutableMap<Location,Double> = mutableMapOf()
        val came_from:  MutableMap<Location,Location> = mutableMapOf()

        //TODO: This comparator can use the traits or speed calculation of the traveller to adjust the weight
        val compareByDistance: Comparator<LocationPriorityWrapper> = compareBy { it.priority }

        val frontier: PriorityQueue<LocationPriorityWrapper> = PriorityQueue(locations.size, compareByDistance)

        came_from[start] = start
        cost_so_far[start] = 0.0

        frontier.add(LocationPriorityWrapper(start,0.0))


        while (frontier.isNotEmpty() && frontier.peek()!!.location != goal) {
            val current: LocationPriorityWrapper = frontier.remove()

            //If we came from the location, then we can skip it in finding the distance in the route
            for ( nextNeighbor in current.location.neighbors().filterNot { came_from[current.location]!!.name == it }) {
                locations[nextNeighbor]?.let {
                    next ->
                    val new_cost: Double = cost_so_far[current.location]!! + current.location.connectionDistance(next)
                    if ( ! cost_so_far.containsKey(next) || new_cost < cost_so_far[next]!!) {
                        cost_so_far[next] = new_cost
                        val priority: Double = new_cost + heuristic(goal, next)
                        frontier.add(LocationPriorityWrapper(next, priority))
                        came_from[next] = current.location
                    }
                }

            }
        }

        var currentPointInRoute = goal
        var foundStart = false
        if (currentPointInRoute == start)  {
            route.add(RouteLeg(SelfConnection, currentPointInRoute, traderActivity))
        } else {
            val routestack:Stack<RouteLeg> = Stack()
            var infinateLoopProtection: Int = 0
            while (!foundStart && infinateLoopProtection++ <= came_from.size) {
                var previousInRoute = came_from[currentPointInRoute]
                previousInRoute?.let {
                    foundStart = previousInRoute == start
                    val activityAtStop = if (currentPointInRoute == goal) traderActivity else NonPlayerTrader.TraderActivity.NONE
                    val connection: Connection? = previousInRoute.connectionFor(currentPointInRoute)
                    connection?.let { routestack.push(RouteLeg(it, currentPointInRoute, activityAtStop)) }
                    currentPointInRoute = previousInRoute
                }
            }
            while (routestack.isNotEmpty()) {
                route.add(routestack.pop())
            }
        }
        return route
    }

    private fun heuristic(goal: Location, next: Location): Double {
        //TODO Use real XY coords and get rid of the inflation for the UI
        return (abs(goal.x - next.x) + abs(goal.y - next.y)) / 5
    }

    private class LocationPriorityWrapper(val location: Location, var priority:Double) {
        override fun hashCode(): Int {
            return location.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return location == other
        }

        override fun toString(): String {
            return "[${location.name}, $priority]"
        }
    }
}