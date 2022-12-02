import com.ostaragame.systems.economy.SceneTree
import com.ostaragame.systems.economy.ui.TradeViewer
import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.actors.NonPlayerTrader
import com.ostaragame.systems.economy.actors.Traits
import com.ostaragame.systems.economy.engine.EconomyEngine
import com.ostaragame.systems.economy.engine.SelfConnection
import com.ostaragame.systems.economy.engine.TradeLibrary
import com.ostaragame.systems.economy.engine.WeatherEngine
import com.ostaragame.systems.economy.ui.NPTGraphSprite
import com.ostaragame.systems.economy.utility.LocationDataLoader
import org.graphstream.ui.spriteManager.Sprite
import org.graphstream.ui.view.Viewer
import java.lang.Thread.sleep


fun main(args: Array<String>) {
    println("Ostara Economy!")

    val dataLoader = LocationDataLoader()
    @Suppress("UNUSED_VARIABLE") val sceneTree = SceneTree
    SceneTree.worldTradeMap = WorldTradeMap
//    dataLoader.loadLocationAndConnectionsIntoGraph(TradeViewer.graph, SceneTree.worldTradeMap)
    dataLoader.loadDGSLocationAndConnectionsIntoGraph(TradeViewer.graph, SceneTree.worldTradeMap)
    /*
     Uncomment below to write out a JSON file connections.json. Then move to the resources directory to update the
        starting connection list
     */
//    dataLoader.writeOutConnections()

    /* <TRADERS> */

    val aTrader = NonPlayerTrader("Yukon Gold!", Traits(visibleInWorld = true))
//    aTrader.currentLocation = SceneTree.worldTradeMap.locations["Albuquerque"]!!
    aTrader.currentLocation = SceneTree.worldTradeMap.locations["Lucky Bend"]!!
    SceneTree.worldTradeMap.locations["Lucky Bend"]!!.travelers.add(aTrader)
    EconomyEngine.registerTrader(aTrader)

    val bTrader = NonPlayerTrader("Derr_Mann", Traits(visibleInWorld = true))
    bTrader.currentLocation = SceneTree.worldTradeMap.locations["Bluewater"]!!
    SceneTree.worldTradeMap.locations["Bluewater"]!!.travelers.add(bTrader)
    EconomyEngine.registerTrader(bTrader)

    val cTrader = NonPlayerTrader("Anon1", Traits(visibleInWorld = true))
    cTrader.currentLocation = SceneTree.worldTradeMap.locations["Scrapyard"]!!
    SceneTree.worldTradeMap.locations["Scrapyard"]!!.travelers.add(cTrader)
    EconomyEngine.registerTrader(cTrader)

    /* </TRADERS> */


    SceneTree.tradeLibrary = TradeLibrary
    dataLoader.loadTradeGoods()

//    println("We can has water? " + SceneTree.tradeLibrary.tradeGoods["Water"])
//    println("Does Old Town Exist? " + (SceneTree.worldTradeMap.locations["Old Town"]?.name ?: "No"))
//    println("Does South Albuquerque Farms Exist? " + (SceneTree.worldTradeMap.locations["South Albuquerque Farms"]?.name
//        ?: "No"))

    dataLoader.loadTradeGoodDemands(SceneTree.worldTradeMap)
   // println(SceneTree.worldTradeMap.locations["Old Town"]?.demand?.first())

    dataLoader.loadTradeGoodSupply(SceneTree.worldTradeMap)
   // println(SceneTree.worldTradeMap.locations["Albuquerque"]?.supply?.first())

    TradeViewer.updateSupplyAndDemandOnGraph()
    val viewer:Viewer = TradeViewer.graph.display(false)

//    viewer.defaultView.enableMouseOptions()

    //viewer.getDefaultView().setMouseManager(MouseOverMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
//    val fromViewer = viewer.newViewerPipe()
//    fromViewer.addViewerListener(GraphMouseEventManager())
//    fromViewer.addSink(TradeViewer.graph)


    /*
        Sprite Manager code here
     */
    val sprites:MutableMap<String,NPTGraphSprite> = mutableMapOf()
    var visibleTraderList = EconomyEngine.visibleTraders()
    for (trader in visibleTraderList) {
        val traderSprite:Sprite = TradeViewer.spriteManager.addSprite(trader.name)
        traderSprite.attachToNode(trader.currentLocation.name )

        if (trader.name == "Derr_Mann") {
            traderSprite.setAttribute("ui.class", "traderderrmann")
        } else if (trader.name == "Anon1") {
            traderSprite.setAttribute("ui.class", "traderanon")
        }else {
            traderSprite.setAttribute("ui.class", "trader")
        }
        sprites[trader.name] = NPTGraphSprite(traderSprite,"",0.0)
    }
    val BadlandsAlbuquerqueWeather:Sprite = TradeViewer.spriteManager.addSprite("Badlands-Albuquerque")
    BadlandsAlbuquerqueWeather.attachToEdge("Badlands-Albuquerque")
    BadlandsAlbuquerqueWeather.setPosition(.5)

//    val raiderSprite:Sprite = TradeViewer.spriteManager.addSprite("Raider1")
//    raiderSprite.attachToNode("Badlands")

    EconomyEngine.prepareIdleWorkers()
    WeatherEngine.updateWeather()
    var frames = 60.0

    val traderOnEdge:MutableMap<NonPlayerTrader,String?> = mutableMapOf()
    val framerate = 60
    while (true) {
        sleep(16)
        if (frames >=  framerate) {
            EconomyEngine.doTick()
            //MissionEngine.doTick()
            WeatherEngine.doTick()
            visibleTraderList = EconomyEngine.visibleTraders()
            frames = 0.0
        }
        frames++

        for (trader in visibleTraderList) {
            trader.currentLeg?.let {currentLeg ->
                currentLeg.connection.let { connection ->
                    val direction = connection.travelDirection(trader.currentLocation.name)
                    val nptGraphSprite:NPTGraphSprite = sprites[trader.name]!!
                    val priorEdgeName:String? = traderOnEdge[trader]
                    var priorEdge = ""
                    if (priorEdgeName != null) {
                        priorEdge = priorEdgeName
                    }
                    if (priorEdge != connection.name()) {
                        if (SelfConnection == connection) {
                            nptGraphSprite.sprite.attachToNode(trader.currentLocation.name)
                            traderOnEdge[trader] = trader.currentLocation.name
                        } else {
                            nptGraphSprite.sprite.attachToEdge(connection.name())
                            traderOnEdge[trader] = connection.name()
                            nptGraphSprite.travelPercentage = 0.0
                        }
                    }
                    if (connection.distance > 0) {
                        val travelPct =
                            ((connection.distance - trader.distanceRemainingToNextLocation) / connection.distance.toDouble())
                        val speedPct = trader.effectiveSpeed() / connection.distance.toDouble()
                        if (nptGraphSprite.travelPercentage <= travelPct + speedPct) {
                            val delta = (speedPct) / framerate
                            nptGraphSprite.travelPercentage += delta
                        } else {
                            nptGraphSprite.travelPercentage = travelPct
                        }

                        val vector = if (direction == 0) {
                            nptGraphSprite.travelPercentage
                        } else {
                            direction - nptGraphSprite.travelPercentage
                        }

                        nptGraphSprite.sprite.setPosition(vector)
                    }
                    //println(vector)
                }


            }

        }
        //            fromViewer.pump()
//            raiderPos += 1.5
//            traderPos += 1
//            if (traderPos >= 100.0) {
//                traderPos = 0.0
//                yukonSprite.setPosition(0.0)
//                yukonSprite.attachToEdge("Lucky Bend-Bluewater")
//            }
//            if (raiderPos >= 100.0) {
//                raiderPos = 0.0
//                raiderSprite.setPosition(0.0)
//                raiderSprite.attachToEdge("Lucky Bend-Badlands")
//            }
//            yukonSprite.setPosition(traderPos/100.0)
//            raiderSprite.setPosition(raiderPos/100.0)
    }
//
/*
3 types of goods - shapes
Supply on right side, demand on left side
Demand colors red - high, yellow - mid, green - low
trader sprite with a cargo sprite indicating what good carried
 */


}