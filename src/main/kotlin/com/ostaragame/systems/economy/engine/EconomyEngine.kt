package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.actors.NonPlayerTrader

object EconomyEngine : ServerTick {
    public const val STORAGE_SIZE: Int = 500
    private val nonPlayerTraders:MutableSet<NonPlayerTrader> = mutableSetOf()

    private val tradersLookingForJobs:MutableList<NonPlayerTrader> = mutableListOf()
    private val workingTraders:MutableList<NonPlayerTrader> = mutableListOf()

    /**
     * For Setup, new traders are added to the engine here. If called multiple times with the same trader,
     * the trader will only exist once (in a set). Traders added after the engine is running will begin working at the
     * next tick that calls prepareIdleWorkers()
     */
    fun registerTrader(trader: NonPlayerTrader) {
        nonPlayerTraders.add(trader)
    }


    fun prepareIdleWorkers() {
        for (trader in nonPlayerTraders) {
            if (trader.isIdle()) {
                trader.readyForWork()
                traderLookingForWork(trader)
            }
        }
    }

    fun visibleTraders():List<NonPlayerTrader> {
        return nonPlayerTraders.filter { it.traits.visibleInWorld }
    }

    private fun traderLookingForWork(trader: NonPlayerTrader) {
        if (trader.seekingWork() && ! tradersLookingForJobs.contains(trader)) {
            tradersLookingForJobs.add(trader)
        }
    }

    //TODO To round-robin distribute trade goods, probably not the way
    private var pointer = 0
    private fun assignTradeJobs() {

        val tradeGoodsInWorld = TradeLibrary.tradeGoods.values

        while (tradersLookingForJobs.isNotEmpty()) {
            val trader = tradersLookingForJobs.removeFirst()
            val currentLocation = trader.currentLocation

            var searchingForWork = true
            var giveUp = false
            val startingPointer = pointer
            while (searchingForWork && ! giveUp) {
                //TODO: Use traits, list of demands, value, and distances
                val tradeGood = tradeGoodsInWorld.elementAt(pointer)
                pointer++
                if (pointer >= tradeGoodsInWorld.size) {
                    pointer = 0
                }
                val result = searchForSupplyAndDemand(currentLocation,tradeGood)
                if (result.first == "Success") {
                    println("Found work for ${trader.name}, ${result.second}")
                    //Here is your route
                    val tradeMission = result.second!!
                    trader.foundWork(tradeMission, WorldTradeMap.findRouteForTradeMission(currentLocation, tradeMission, trader.traits))

                    //Maybe Cache Routes Here?
                    workingTraders.add(trader)
                    searchingForWork = false
                } else if (startingPointer == pointer){
                    giveUp = true
                } else {
                    println("No work found for ${trader.name} ${tradeGood.name}")
                }
            }
            if (giveUp) {
                println("No work found for ${trader.name} going Idle")
                //TODO does the trader go idle if no work is found, or do they keep looking for work, and for how long? Trait controlled?
                trader.goIdle()
                nonPlayerTraders.add(trader)
            }
        }

    }

    private fun doWork() {
        val immutableList = workingTraders.toTypedArray()
        for (trader in immutableList) {
            if (trader.seekingWork()) {
                workingTraders.remove(trader)
                traderLookingForWork(trader)
            } else if (trader.isIdle()) {
                workingTraders.remove(trader)
            } else {
                trader.doWork()
            }
        }
    }

    private fun doRestock() {
        for (location in WorldTradeMap.locations.values) {
            for (supply in location.supply) {
                supply.inventoryCurrent = minOf( (supply.inventoryCurrent + supply.restockRate), supply.inventoryMax )
            }
            for (demand in location.demand) {
                demand.inventory = maxOf(demand.inventory - demand.demandRate, 0)
            }
        }
    }


    /*
        TODO: I was thinking of a time class that handled some of the work of counting time,
         advancing mission ticks, economic ticks
         do work every tick
         10 ticks = 1 day
         prepare idle workers daily
         Assign trade jobs every 5 ticks (1 & 6)
         doRestock every 3 days
     */
    private var dayCount = 1
    private var tickCount = 0
    override fun doTick() {
        tickCount++
        if (tickCount % 10 == 0) {
            prepareIdleWorkers()
        }

        if (dayCount % 3 == 0) {
            doRestock()
        }

        if (tickCount == 1 || tickCount == 6) {
            assignTradeJobs()
        }

        doWork()

        if (tickCount % 10 == 0) {
            tickCount = 0
            dayCount++
            println("***** Day $dayCount *****")
        }
    }


    //TODO This in combination with findRoute is inefficient because we are searching for the supply location twice and should have the initial route from that
    private fun searchForSupplyAndDemand(startingFrom:Location, tradeGood: TradeGood): Pair<String, TradeMission?> {
        val queue:MutableList<Location> = mutableListOf()
        val visitedMap:MutableMap<String,Location> = mutableMapOf()
        queue.add(startingFrom)
        var stillLooking = true
        var tradeGoodSupply:TradeGoodSupply? = null
        var tradeGoodDemand:TradeGoodDemand? = null
        while (queue.isNotEmpty() && stillLooking) {
            val location = queue.removeFirst()
            //Do we have demand
            for (demand in location.demand) {
                if (demand.tradeGood == tradeGood && demand.unitsDemanded() > 0) {
                    tradeGoodDemand = demand
                    println(tradeGoodDemand)
                }
            }
            //Do we have supply
            for (supply in location.supply) {
                if (supply.tradeGood == tradeGood && supply.inventoryCurrent > 0) {
                    tradeGoodSupply = supply
                    println(tradeGoodSupply)
                }
            }
            visitedMap[location.name] = location
            for (connection in location.connections) {
                if (!visitedMap.containsKey(connection.location1))
                    queue.add(WorldTradeMap.locations[connection.location1]!!)
                if (!visitedMap.containsKey(connection.location2))
                    queue.add(WorldTradeMap.locations[connection.location2]!!)
            }
            stillLooking = tradeGoodSupply == null || tradeGoodDemand == null

        }
        var result = "Not Found"
        var tradeMission:TradeMission? = null
        if (!stillLooking) {
            result = "Success"
            tradeMission = TradeMission(tradeGood, tradeGoodSupply!!, tradeGoodDemand!!, tradeGoodDemand.unitsDemanded())
        }
        return Pair(result, tradeMission)
    }

}