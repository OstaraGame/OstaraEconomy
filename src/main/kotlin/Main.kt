import com.ostaragame.systems.economy.SceneTree
import com.ostaragame.systems.economy.ui.TradeViewer
import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.actors.NonPlayerTrader
import com.ostaragame.systems.economy.actors.Traits
import com.ostaragame.systems.economy.engine.EconomyEngine
import com.ostaragame.systems.economy.engine.TradeLibrary
import com.ostaragame.systems.economy.ui.GraphMouseEventManager
import com.ostaragame.systems.economy.utility.LocationDataLoader
import org.graphstream.ui.spriteManager.Sprite
import org.graphstream.ui.view.Viewer
import java.lang.Thread.sleep


fun main(args: Array<String>) {
    println("Hello Ostara!")

    println("Program arguments: ${args.joinToString()}")
    System.setProperty("org.graphstream.ui", "swing")


    val dataLoader = LocationDataLoader()

    SceneTree.worldTradeMap = WorldTradeMap
    dataLoader.loadLocationListIntoGraph(TradeViewer.graph, SceneTree.worldTradeMap)

    /* <TRADERS> */
    val aTrader = NonPlayerTrader("Yukon Gold!", Traits())
    aTrader.currentLocation = SceneTree.worldTradeMap.locations["Lucky Bend"]!!
    SceneTree.worldTradeMap.locations["Lucky Bend"]!!.travelers.add(aTrader)
    EconomyEngine.registerTrader(aTrader)

    /* </TRADERS> */


    SceneTree.tradeLibrary = TradeLibrary
    dataLoader.loadTradeGoods(SceneTree.tradeLibrary)

    println("We can has water? " + SceneTree.tradeLibrary.tradeGoods["Water"])
    println("Does Old Town Exist? " + (SceneTree.worldTradeMap.locations["Old Town"]?.name ?: "No"))
    println("Does South Albuquerque Farms Exist? " + (SceneTree.worldTradeMap.locations["South Albuquerque Farms"]?.name
        ?: "No"))

    dataLoader.loadTradeGoodDemands(SceneTree.tradeLibrary, SceneTree.worldTradeMap)
   // println(SceneTree.worldTradeMap.locations["Old Town"]?.demand?.first())

    dataLoader.loadTradeGoodSupply(SceneTree.tradeLibrary, SceneTree.worldTradeMap)
   // println(SceneTree.worldTradeMap.locations["Albuquerque"]?.supply?.first())

    TradeViewer.updateSupplyAndDemandOnGraph()
//    val viewer:Viewer = TradeViewer.graph.display()



//    viewer.defaultView.enableMouseOptions()

    //viewer.getDefaultView().setMouseManager(MouseOverMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
//    val fromViewer = viewer.newViewerPipe()
//    fromViewer.addViewerListener(GraphMouseEventManager())
//    fromViewer.addSink(TradeViewer.graph)


    //TODO Pull the starting position from the travelers list for the locations
    val raiderSprite:Sprite = TradeViewer.spriteManager.addSprite("Raider1")
    raiderSprite.attachToNode("Badlands")

    val yukonSprite:Sprite = TradeViewer.spriteManager.addSprite("Yukon Gold!")
    yukonSprite.attachToNode("Lucky Bend")
    yukonSprite.setAttribute("ui.class", "trader")

//    EconomyEngine.doTick()
//    MissionEngine.doTick()
//        var traderPos:Double = 0.0
//        var raiderPos:Double = -15.0
    EconomyEngine.prepareIdleWorkers()
        while (true) {
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
            sleep(1000)
            EconomyEngine.doTick()
        }

/*
3 types of goods - shapes
Supply on right side, demand on left side
Demand colors red - high, yellow - mid, green - low
trader sprite with a cargo sprite indicating what good carried
 */


}