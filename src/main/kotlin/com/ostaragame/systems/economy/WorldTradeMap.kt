package com.ostaragame.systems.economy

import com.ostaragame.systems.economy.engine.Location

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

 }