package com.ostaragame.systems.economy.utility

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class LocationDataLoader {

    private fun loadCsvRecords(fileName: String, headers: Class<out Enum<*>>): List<CSVRecord> {
        val resource: URL? = LocationDataLoader::class.java.getResource(fileName)
        val csvFile: File? = resource?.toURI()?.let { Paths.get(it).toFile() }
        val formatBuilder: CSVFormat.Builder = CSVFormat.Builder.create(CSVFormat.DEFAULT)
        formatBuilder.setHeader(headers)
        formatBuilder.setSkipHeaderRecord(true)

        if (csvFile == null) {
            error("CSV File not found $fileName")
        }
        val reader = Files.newBufferedReader(csvFile.toPath())
        val csvParser = CSVParser(reader, formatBuilder.build())
        return csvParser.records
    }

    fun loadLocationListIntoGraph(graph: Graph, worldTradeMap: WorldTradeMap) {
        println("Loading world locations")
        val list: List<CSVRecord> = loadCsvRecords("/Locations1.csv", LocationHeaders::class.java)
        for (record:CSVRecord in list) {
            val nearLocationName:String = record.get(LocationHeaders.Name)
            val nearLocation:Location
            if (nearLocationName in worldTradeMap.locations) {
                nearLocation = worldTradeMap.locations.getValue(nearLocationName)
            } else {
                nearLocation = Location(
                    nearLocationName, WorldTradeMap.getNextLocationId(),
                    mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
                )
                worldTradeMap.locations[nearLocationName] = nearLocation
            }
            //println("Loading $name")
            val node: Node = graph.addNode(nearLocationName)
            node.setAttribute("ui.label", nearLocationName)
            //node.setAttribute("ui.class", record.get(LocationHeaders.Size) )
            val lineParser:CSVParser = CSVParser.parse(record.get(LocationHeaders.Connections), CSVFormat.DEFAULT)
            for (lineRecord:CSVRecord in lineParser.records) {
                for (distantLocationName:String in lineRecord.toList()) {
                    graph.addEdge("$nearLocationName-$distantLocationName", nearLocationName, distantLocationName)
                    var distantLocation:Location
                    if (distantLocationName in worldTradeMap.locations) {
                        distantLocation = worldTradeMap.locations.getValue(distantLocationName)
                    } else {
                        distantLocation = Location(distantLocationName, WorldTradeMap.getNextLocationId(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
                        worldTradeMap.locations[distantLocationName] = distantLocation
                    }
                    val connection = Connection(nearLocation, distantLocation,-1.0f,Infrastructure.NONE, Terrain.PLAINS,Weather.CLEAR, mutableListOf())
                    nearLocation.connections.add(connection)
                    //TODO Reciprocal connections in distantLocation
                }
            }
        }
    }



    fun loadTradeGoods(library: TradeLibrary) {
        println("Loading Trade Goods")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoods.csv",TradeGoodHeaders::class.java)
        for (record:CSVRecord in list) {
            val key = record.get(TradeGoodHeaders.Name)
            library.tradeGoods[key] = TradeGood(key,
                TradeGoodType.valueOf(record.get(TradeGoodHeaders.Type)),
                record.get(TradeGoodHeaders.Cost).toFloat(),
                record.get(TradeGoodHeaders.Volume).toFloat())
        }
    }

    fun loadTradeGoodDemands(library: TradeLibrary, worldTradeMap: WorldTradeMap) {
        println("Loading Trade Good Demands")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoodDemands.csv", TradeGoodDemandHeaders::class.java)
        for (record:CSVRecord in list) {
            val locationName = record.get(TradeGoodDemandHeaders.LocationName)
            val location = worldTradeMap.locations[locationName]
            if (location == null) {
                println("WARNING: No location found for $locationName")
                continue
            }
            val tradeGoodName = record.get(TradeGoodDemandHeaders.TradeGoodName)
            val tradeGood = library.tradeGoods[tradeGoodName]
            if (tradeGood == null) {
                println("WARNING: No Trade Good found for $tradeGoodName")
                continue
            }
            val tradeGoodDemand = TradeGoodDemand(tradeGood,
                location,
                record.get(TradeGoodDemandHeaders.UnitsDemanded).toFloat(),
                record.get(TradeGoodDemandHeaders.DemandRate).toFloat(),
                record.get(TradeGoodDemandHeaders.CeilingPrice).toFloat(),
            )
            location.demand.add(tradeGoodDemand)
        }
    }

    fun loadTradeGoodSupply(library: TradeLibrary, worldTradeMap: WorldTradeMap) {
        println("Loading Trade Good Supply")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoodSupply.csv",TradeGoodSupplyHeaders::class.java)
        for (record:CSVRecord in list) {
            val locationName = record.get(TradeGoodSupplyHeaders.LocationName)
            val location = worldTradeMap.locations[locationName]
            if (location == null) {
                println("WARNING: No location found for $locationName")
                continue
            }
            val tradeGoodName = record.get(TradeGoodSupplyHeaders.TradeGoodName)
            val tradeGood = library.tradeGoods[tradeGoodName]
            if (tradeGood == null) {
                println("WARNING: No Trade Good found for $tradeGoodName")
                continue
            }
            val tradeGoodSupply = TradeGoodSupply(tradeGood,
                location,
                record.get(TradeGoodSupplyHeaders.Inventory).toFloat(),
                record.get(TradeGoodSupplyHeaders.InventoryMax).toFloat(),
                record.get(TradeGoodSupplyHeaders.RestockRateBase).toFloat(),
                record.get(TradeGoodSupplyHeaders.RestockRate).toFloat(),
            )
            location.supply.add(tradeGoodSupply)
        }
    }

    enum class LocationHeaders:CSVHeader {
        Name, Connections, Size
    }

    enum class TradeGoodHeaders:CSVHeader {
        Name, Type, Cost, Volume
    }

    enum class TradeGoodDemandHeaders:CSVHeader {
        LocationName,TradeGoodName,UnitsDemanded,DemandRate,CeilingPrice
    }

    enum class TradeGoodSupplyHeaders:CSVHeader {
        LocationName,TradeGoodName,Inventory,InventoryMax,RestockRateBase,RestockRate
    }
}

interface CSVHeader
