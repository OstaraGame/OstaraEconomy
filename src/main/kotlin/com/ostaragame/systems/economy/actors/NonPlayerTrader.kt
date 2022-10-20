package com.ostaragame.systems.economy.actors

import com.ostaragame.systems.economy.engine.Location
import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.Connection
import com.ostaragame.systems.economy.engine.RouteLeg
import com.ostaragame.systems.economy.engine.TradeMission
import java.util.LinkedList

class NonPlayerTrader(val name: String, val traits: Traits) {

    var state:TraderState = TraderState.IDLE
    var tradeMission:TradeMission? = null

    var currentLocation: Location = WorldTradeMap.traderOffMapHome

    var route: LinkedList<RouteLeg> = LinkedList()
    var distanceRemainingToNextLocation: Float = 0.0F
    var currentLeg: RouteLeg? = null
    var destinationLocation: Location? = null


    fun seekingWork():Boolean {
        if (state == NonPlayerTrader.TraderState.IDLE || state == NonPlayerTrader.TraderState.COMPLETED_DELIVERY)  {
            state = NonPlayerTrader.TraderState.LOOKING_FOR_WORK
            return true
        }
        return false
    }

    fun goIdle() {
        state = TraderState.IDLE
        tradeMission = null
    }

    fun foundWork(newTradeMission: TradeMission, newRoute:Collection<RouteLeg>):Boolean {
        if (state == NonPlayerTrader.TraderState.LOOKING_FOR_WORK) {
            state = NonPlayerTrader.TraderState.TRAVELING_TO_SUPPLY
            tradeMission = newTradeMission
            //TODO do I need to clear the existing route first???
            route.plus(newRoute)
            return true
        }
        return false
    }

    enum class TraderState {
        IDLE,
        LOOKING_FOR_WORK,
        TRAVELING_TO_SUPPLY,
        ARRIVED_AT_SUPPLY_PICKUP,
        PICKUP_SUPPLIES,
        SUPPLIES_LOADED,
        TRAVELING_TO_DEMAND,
        ARRIVED_AT_DEMAND_LOCATION,
        DELIVER_GOODS,
        COMPLETED_DELIVERY

    }

    override fun toString(): String {
        return "$name-$state"
    }

    fun doWork() {
        when (state) {
            TraderState.TRAVELING_TO_SUPPLY -> travelToSupply()
            TraderState.ARRIVED_AT_SUPPLY_PICKUP -> arrivedAtSupplyPickup()
            TraderState.PICKUP_SUPPLIES -> pickupSupplies()
            TraderState.SUPPLIES_LOADED -> TODO()
            TraderState.TRAVELING_TO_DEMAND -> TODO()
            TraderState.ARRIVED_AT_DEMAND_LOCATION -> TODO()
            TraderState.DELIVER_GOODS -> TODO()
            TraderState.COMPLETED_DELIVERY -> TODO()
            TraderState.IDLE -> TODO()
            TraderState.LOOKING_FOR_WORK -> TODO()
        }
    }

    fun travelToSupply() {
        //Advance along the route and update position
        var arrived = false

        //do we have a current route leg?
        //if not, get the first route leg
        if (null == currentLeg) {
            //try {
                currentLeg = route.pop()
                /* } catch (NoSuchElementException e) {
                     error("Trader was traveling to supply and had no route")
                     state = TraderState.IDLE
                 }*/
            distanceRemainingToNextLocation = currentLeg!!.connection.distance

        }

        //Traverse distance on leg at traders travel rate
        //Check to see if we have arrived at end of leg, if so get next leg and reset travel distance
        if (distanceRemainingToNextLocation.minus(traits.travelSpeed) <= 0) {
            if (route.isNotEmpty()) {
                currentLeg = route.pop()

                //This means that the trader loses potential progress every time they hit a location. To allow the trader to make
                //more progress, then add the prior distance remaining (now a negative number) to the next leg's distance remaining
                //And then this route popping would need to be in a loop until the trader stopped blasting past locations
                distanceRemainingToNextLocation = currentLeg!!.connection.distance
            } else {
                currentLeg = null
                distanceRemainingToNextLocation = 0.0F
                arrived = true
            }
        }
        //else if no more legs, then we have arrived at final destination
        if (arrived) {
            state = TraderState.ARRIVED_AT_SUPPLY_PICKUP
        }
    }


    fun arrivedAtSupplyPickup() {
        println("Trader $name has arrived at Supply Pickup")
        state = TraderState.PICKUP_SUPPLIES
    }

    fun pickupSupplies() {
        println("Trader $name is picking up supplies")
        //TODO Continue the state transitions
        state = TraderState.IDLE
    }
}