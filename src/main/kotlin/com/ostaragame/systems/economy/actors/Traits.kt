package com.ostaragame.systems.economy.actors

import kotlinx.serialization.Serializable

@Serializable
class Traits (){
    constructor(visibleInWorld: Boolean) : this() {
        this.visibleInWorld = visibleInWorld
    }

    var travelSpeed:Float = 10.0F
    val maxCargoUnits = 100.0F
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

}