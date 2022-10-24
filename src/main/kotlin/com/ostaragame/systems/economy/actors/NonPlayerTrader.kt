package com.ostaragame.systems.economy.actors

import com.ostaragame.systems.economy.engine.Location
import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.RouteLeg
import com.ostaragame.systems.economy.engine.TradeGood
import com.ostaragame.systems.economy.engine.TradeMission
import java.util.LinkedList

class NonPlayerTrader(val name: String, val traits: Traits) {

    private var state:TraderState = TraderState.IDLE
    var currentLocation: Location = WorldTradeMap.traderOffMapHome
    var cargoStored:MutableMap<TradeGood, Cargo> = mutableMapOf()
    var cargoUnitsFilled = 0.0F
    var money = 0.0F

    var tradeMission:TradeMission? = null
    var route: ArrayDeque<RouteLeg> = ArrayDeque()
    var distanceRemainingToNextLocation: Float = 0.0F
    var currentLeg: RouteLeg? = null
    var destinationLocation: Location = currentLocation


    fun seekingWork():Boolean {
        if (state == TraderState.LOOKING_FOR_WORK)  {
            return true
        }
        return false
    }

    fun readyForWork() {
        state = TraderState.LOOKING_FOR_WORK
    }

    fun goIdle() {
        state = TraderState.IDLE
        tradeMission = null
    }

    fun foundWork(newTradeMission: TradeMission, newRoute:ArrayDeque<RouteLeg>):Boolean {
        if (state == TraderState.LOOKING_FOR_WORK) {
            state = TraderState.TRAVELING_TO_SUPPLY
            tradeMission = newTradeMission
            route = newRoute
            println("Trader $name is starting at $currentLocation")
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

    enum class TraderActivity {
        NONE,
        PICKUP,
        DROP_OFF,
        PICKUP_DROP_OFF
    }

    override fun toString(): String {
        return "$name-$state-$currentLocation"
    }

    fun doWork() {
        when (state) {
            TraderState.TRAVELING_TO_SUPPLY -> travelToSupply()
            TraderState.ARRIVED_AT_SUPPLY_PICKUP -> arrivedAtSupplyPickup()
            TraderState.PICKUP_SUPPLIES -> pickupSupplies()
            TraderState.SUPPLIES_LOADED -> suppliesLoaded()
            TraderState.TRAVELING_TO_DEMAND -> travelingToDemand()
            TraderState.ARRIVED_AT_DEMAND_LOCATION -> arrivedAtDemandLocation()
            TraderState.DELIVER_GOODS -> deliverGoods()
            TraderState.COMPLETED_DELIVERY -> completedDelivery()

            //TraderState.IDLE -> TODO()
            //TraderState.LOOKING_FOR_WORK -> TODO()
            else -> {}
        }
    }

    private fun travelToSupply() {
        println("Trader $name is traveling towards Supply Pickup")
        //Advance along the route and update position
        val arrived = doTravel()

        //else if no more legs, then we have arrived at final destination
        if (arrived) {
            state = TraderState.ARRIVED_AT_SUPPLY_PICKUP
        }
    }


    private fun arrivedAtSupplyPickup() {
        println("Trader $name has arrived at Supply Pickup")
        state = TraderState.PICKUP_SUPPLIES
    }

    private fun pickupSupplies() {
        println("Trader $name is picking up supplies")
        //TODO The loading of cargo needs to be managed by something else as traders could pull cargo that doesn't exist, or have multithreading issues
        tradeMission?.let {
            if (it.supply.inventoryCurrent > 0 && availableCargoSpace() > 0) {
                val amountToLoad = minOf(it.supply.inventoryCurrent, availableCargoSpace())
                //TODO Use the price of the supply, not the cost
                money = money.minus(amountToLoad*it.supply.tradeGood.cost)
                it.supply.inventoryCurrent = it.supply.inventoryCurrent.minus(amountToLoad)
                cargoUnitsFilled = cargoUnitsFilled.plus(amountToLoad)
                val cargo = cargoStored[it.tradeGood] ?: Cargo(it.tradeGood, 0.0F)
                cargo.units = cargo.units.plus(amountToLoad)
                cargoStored[cargo.tradeGood] = cargo
                state = TraderState.SUPPLIES_LOADED
            } else {
                //TODO Handle no available cargo space, and how long does a trader wait if there is no stock? Or how much will the trader accept as a minimum stock
                goIdle()
                println("Trader $name has no supplies to pickup")
            }
        }
        //TODO A trader could be stuck here if the trade mission is null?
        if (tradeMission == null) {
            goIdle()
            println("TRADERSTUCKNOMISSION: Trader $name has no supplies to pickup because the mission disappeared")
        }
    }

    private fun suppliesLoaded() {
        //TODO Find route to destination
        println("Trader $name is finished loading and now planning the route to the demand")
        state = TraderState.TRAVELING_TO_DEMAND
    }

    private fun travelingToDemand() {
        //Advance along the route and update position
        val arrived = doTravel()

        //else if no more legs, then we have arrived at final destination
        if (arrived) {
            state = TraderState.ARRIVED_AT_DEMAND_LOCATION
        }
    }

    private fun arrivedAtDemandLocation() {
        println("Trader $name has arrived at demand location")
        state = TraderState.DELIVER_GOODS
    }

    private fun deliverGoods() {
        println("Trader $name is delivering goods")
        //TODO The loading of cargo needs to be managed by something else as traders could pull cargo that doesn't exist, or have multithreading issues
        tradeMission?.let {
            if (it.demand.unitsDemanded > 0) {
                val cargo = cargoStored[it.tradeGood]?: Cargo(it.tradeGood, 0.0F)
                val amountToOffload = minOf(it.demand.unitsDemanded, cargo.units)
                cargoUnitsFilled = cargoUnitsFilled.minus(amountToOffload)
                cargo.units = cargo.units.minus(amountToOffload)
                it.demand.unitsDemanded = it.demand.unitsDemanded.minus(amountToOffload)
                //TODO Use the price of the demand, not the ceilingPrice
                money = money.plus(amountToOffload*it.demand.ceilingPrice)
                if (cargo.units <= 0) {
                    cargoStored.remove(it.tradeGood)
                }

                state = TraderState.COMPLETED_DELIVERY
            } else {
                //TODO Handle no available demand, and how long does a trader wait to sell
                goIdle()
                println("Trader $name has no supplies to deliver")
            }
        }
        //TODO A trader could be stuck here if the trade mission is null?
    }

    fun completedDelivery() {
        println("Trader $name now has $$money")
        state = TraderState.LOOKING_FOR_WORK
        //If the trader goes Idle, then they are not looking for work for a while...
    }
    private fun doTravel(): Boolean {
        //TODO This will be rewritten tu support multiple activities at the same location. Probably this needs to not return a boolean, but the activity to do?
        var arrived = false
        //do we have a current route leg?
        //if not, get the first route leg
        //TODO Rewrite this again until its the Kotlin way
        if (null != tradeMission) {
            if (currentLeg == null && route.isNotEmpty()) {
                destinationLocation = route.last().nextStop
                currentLeg = route.removeFirst()
                if (currentLocation != destinationLocation && currentLeg?.traderActivity == TraderActivity.NONE) { //Did we start where we already need to be? Meaning the currentLocation is the nextStop of the first RouteLeg
                    currentLeg?.let { distanceRemainingToNextLocation = it.connection.distance }
                    if (distanceRemainingToNextLocation <= 0) {
                        goIdle()
                        error("Trader moved to first leg and it has no distance or connection")
                    }
                }
            }
        } else {
            goIdle()
            error("Trader was traveling to supply and had no TradeMission?")
        }

        //Traverse distance on leg at traders travel rate
        //Check to see if we have arrived at end of leg, if so get next leg and reset travel distance
        distanceRemainingToNextLocation = distanceRemainingToNextLocation.minus(traits.travelSpeed)
        if (distanceRemainingToNextLocation <= 0) {
            currentLeg?.let { currentLocation = it.nextStop }
            println("Trader $name is at $currentLocation, continuing to travel")
            if (currentLeg?.traderActivity == TraderActivity.NONE && route.isNotEmpty()) { //Then keep going....
                currentLeg = route.removeFirst()

                //This means that the trader loses potential progress every time they hit a location. To allow the trader to make
                //more progress, then add the prior distance remaining (now a negative number) to the next leg's distance remaining
                //And then this route popping would need to be in a loop until the trader stopped blasting past locations
                currentLeg?.let{ distanceRemainingToNextLocation = it.connection.distance }
                if (distanceRemainingToNextLocation <= 0) {
                    goIdle()
                    error("Trader tried to move to a route leg and it has no distance or connection")
                }
            } else {
                currentLeg = null
                distanceRemainingToNextLocation = 0.0F
                arrived = true
            }
        } else {
            println("Trader $name is traveling towards ${currentLeg?.nextStop}, dist remaining: $distanceRemainingToNextLocation ")
        }
        return arrived
    }
    fun availableCargoSpace():Float {
        return traits.maxCargoUnits - cargoUnitsFilled
    }
}