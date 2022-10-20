package com.ostaragame.systems.economy.ui

import org.graphstream.ui.view.ViewerListener

class GraphMouseEventManager : ViewerListener{
    override fun viewClosed(viewName: String?) {
        //TODO("Not yet implemented")
    }

    override fun buttonPushed(id: String?) {
        println("Old Town: Supply: Water, Demand: Metal")
    }

    override fun buttonReleased(id: String?) {
        //TODO("Not yet implemented")
    }

    override fun mouseOver(id: String?) {
        println(id)
    }

    override fun mouseLeft(id: String?) {
        //TODO("Not yet implemented")
    }
}