package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.actors.NonPlayerTrader

object EconomyEngine : ServerTick {
    private val idleTraders:MutableList<NonPlayerTrader> = mutableListOf()
    private val tradersLookingForJobs:MutableList<NonPlayerTrader> = mutableListOf()
    private val workingTraders:MutableList<NonPlayerTrader> = mutableListOf()

    fun registerTrader(trader: NonPlayerTrader) {
        //TODO Make sure a trader is only added once
        idleTraders.add(trader)
    }


    fun prepareIdleWorkers() {
        while (idleTraders.isNotEmpty()) {
            val trader = idleTraders.removeFirst()
            trader.readyForWork()
            traderLookingForWork(trader)
        }
    }

    private fun traderLookingForWork(trader: NonPlayerTrader) {
        if (trader.seekingWork()) {
            tradersLookingForJobs.add(trader)
        }
    }

    //TODO To round-robin distribute trade goods, probably not the way
    var pointer = 0
    private fun assignTradeJobs() {

        val tradeGoodsInWorld = TradeLibrary.tradeGoods.values

        while (tradersLookingForJobs.isNotEmpty()) {
            val trader = tradersLookingForJobs.removeFirst()
            val currentLocation = trader.currentLocation

            //TODO: Use traits, list of demands, value, and distances
            val tradeGood = tradeGoodsInWorld.elementAt(pointer)
            pointer++
            if (pointer >= tradeGoodsInWorld.size) {
                pointer = 0
            }
            val result = searchForSupplyAndDemand(currentLocation,tradeGood!!)
            if (result.first == "Success") {
                println("Found work for ${trader.name}, ${result.second}")
                //Here is your route
                val tradeMission = result.second!!
                trader.foundWork(tradeMission, WorldTradeMap.findRouteForTradeMission(currentLocation, tradeMission, trader.traits))

                //Maybe Cache Routes Here?
                workingTraders.add(trader)
            } else {
                println("No work found for ${trader.name}")
                //TODO does the trader go idle if no work is found, or do they keep looking for work, and for how long? Trait controlled?
                //trader.goIdle()
                //idleTraders.add(trader)
            }
        }

    }

    private fun doWork() {
        for (trader in workingTraders) {
            if (trader.seekingWork())
                traderLookingForWork(trader)
            else
                trader.doWork()
        }
    }

    private fun doRestock() {
        //TODO handle demand increasing unchecked, or overflowing past Float.MAX_VALUE
        for (location in WorldTradeMap.locations.values) {
            for (supply in location.supply) {
                supply.inventoryCurrent = maxOf( (supply.inventoryCurrent + supply.restockRate), supply.inventoryMax )
            }
            for (demand in location.demand) {
                demand.unitsDemanded = demand.unitsDemanded + demand.demandRate
            }
        }
    }


    override fun doTick() {
        //prepareIdleWorkers()
        doRestock()
        assignTradeJobs()
        doWork()
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
                if (demand.tradeGood == tradeGood && demand.unitsDemanded > 0) {
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
                if (!visitedMap.containsKey(connection.location1.name))
                    queue.add(connection.location1)
                if (!visitedMap.containsKey(connection.location2.name))
                    queue.add(connection.location2)
            }
            stillLooking = tradeGoodSupply == null || tradeGoodDemand == null

        }
        var result = "Not Found"
        var tradeMission:TradeMission? = null
        if (!stillLooking) {
            result = "Success"
            tradeMission = TradeMission(tradeGood, tradeGoodSupply!!, tradeGoodDemand!!)
        }
        return Pair(result, tradeMission)
    }

}