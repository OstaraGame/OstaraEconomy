package com.ostaragame.systems.economy.actors

import com.ostaragame.systems.economy.engine.Weather
import kotlinx.serialization.Serializable

@Serializable
class Traits (){
    constructor(visibleInWorld: Boolean) : this() {
        this.visibleInWorld = visibleInWorld
    }

    var travelSpeed:Double = 10.0
    val maxCargoUnits = 100.0f
    var visibleInWorld = false

    //Overhauler: Picks up more supplies than the demand calls for
    //Patient: Waits if there is no supply/demand, versus impatient dropping job and searching again
    //SupplyFocus
    //DemandFocus
    //Greedy: Most Profit regardless of distance
    //Efficient: Balances Most Profit against travel time/distance
    //MasterEfficiency: Allows multiple stops to pickup and drop off different goods
    //Weather?
    //Infrastructure?
    //Risky - willing to off on roads without much info
    //High Standards/profit margin

    fun weatherTravelEffect(currentWeather: Weather) : Double {
        return when (currentWeather) {
            Weather.CLEAR -> 1.0
            Weather.CLOUDY -> .98
            Weather.FOG -> .5
            Weather.HOT -> .5
            Weather.RAIN -> .85
            Weather.SNOW -> .4
            Weather.COLD -> .75
            Weather.HURRICANE -> .01
            Weather.BLIZZARD -> .01
        }
    }
}