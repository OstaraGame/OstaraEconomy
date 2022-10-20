package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.actors.NonPlayerTrader

object EconomyEngine : ServerTick {
    val idleTraders:MutableList<NonPlayerTrader> = mutableListOf()
    val tradersLookingForJobs:MutableList<NonPlayerTrader> = mutableListOf()
    val workingTraders:MutableList<NonPlayerTrader> = mutableListOf()

    fun registerTrader(trader: NonPlayerTrader) {
        //TODO Make sure a trader is only added once
        idleTraders.add(trader)
    }


    fun prepareIdleWorkers() {
        for (trader in idleTraders) {
            traderLookingForWork(trader)
        }
    }

    private fun traderLookingForWork(trader: NonPlayerTrader) {
        if (trader.seekingWork()) {
            tradersLookingForJobs.add(trader)
        }
    }

    fun assignTradeJobs() {

        while (tradersLookingForJobs.isNotEmpty()) {
            val trader = tradersLookingForJobs.removeFirst()
            val currentLocation = trader.currentLocation

            //TODO: Use traits, list of demands, value, and distances
            val tradeGood = TradeLibrary.tradeGoods["Parts"]
            val result = searchForSupplyAndDemand(currentLocation,tradeGood!!)
            if (result.first == "Success") {
                println("Found work for ${trader.name}, ${result.second}")
                trader.foundWork(result.second!!, mutableListOf())
                //Here is your route TODO
                //Maybe Cache Routes Here!
                workingTraders.add(trader)
            } else {
                println("No work found for ${trader.name}")
                trader.goIdle()
                idleTraders.add(trader)
            }
        }

    }

    fun doWork() {
        for (trader in workingTraders) {
            trader.doWork()
        }
    }


    override fun doTick() {
        prepareIdleWorkers()
        assignTradeJobs()
        doWork()
    }



    fun searchForSupplyAndDemand(startingFrom:Location, tradeGood: TradeGood): Pair<String, TradeMission?> {
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
                if (demand.tradeGood == tradeGood) {
                    tradeGoodDemand = demand
                    println(tradeGoodDemand)
                }
            }
            //Do we have supply
            for (supply in location.supply) {
                if (supply.tradeGood == tradeGood) {
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