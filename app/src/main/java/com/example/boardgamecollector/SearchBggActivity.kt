package com.example.boardgamecollector

import android.os.AsyncTask
import android.os.Bundle
import android.telecom.Call
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class SearchBggActivity : AppCompatActivity() {
    private val dbHandler = MyDBHandler(this, null, null, 1)
    var gamesId: MutableList<String> = ArrayList()
    var gameArtists: MutableList<String> = ArrayList()
    var gameDesigners: MutableList<String> = ArrayList()
    var gameExpansions: MutableList<String> = ArrayList()
    var checkText: String? = null
    val prefsUsername: String? = null
    var idToUpdate: MutableList<Int> = ArrayList()
    var rankToUpdate: MutableList<Int> = ArrayList()
    val prefsName = "savedName"

    private fun clearDescription(description: String): String{
        var result = ""
        var stop = false
        for (i in description)
            if(i == '&')
                stop = true
            else if (i == ';')
                stop = false
            else if (!stop)
                result += i
        return result
    }

    private inner class FileDownloader : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val dd = DetailsDownloader()
            dd.execute()
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val textInput: TextInputEditText = findViewById(R.id.textInput3)
                val username = textInput.text
                getSharedPreferences(prefsName, MODE_PRIVATE).edit().putString(prefsUsername, username.toString()).apply();
                if (checkText != username.toString()) {
                    checkText = username.toString()
                    val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$username")
                    val connection = url.openConnection()
                    connection.connect()
                    val lengthOfFile = connection.contentLength
                    val isStream = url.openStream()
                    val directory = File("$filesDir/XML")
                    if (!directory.exists()) directory.mkdir()
                    val fos = FileOutputStream("$directory/data.xml")
                    val data = ByteArray(1024)
                    var count = 0
                    var total: Long = 0
                    var progress = 0
                    count = isStream.read(data)
                    while (count != -1) {
                        total += count.toLong()
                        val progressTemp = total.toInt() * 100 / lengthOfFile
                        if (progressTemp % 10 == 0 && progress != progressTemp)
                            progress = progressTemp
                        fos.write(data, 0, count)
                        count = isStream.read(data)
                    }
                    isStream.close()
                    fos.close()
                    findGame()
                }
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }

        private fun findGame() {
            val inDir = File("$filesDir/XML")
            if (!inDir.exists()) inDir.mkdir()
            if (inDir.exists()) {
                val file = File(inDir, "data.xml")
                if (file.exists()) {
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)
                        if (itemNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = itemNode as Element
                            var id = elem.getAttribute("objectid")
                            gamesId.add(id)
                        }
                    }
                }
            }
        }
    }

    private inner class DetailsDownloader : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            displayResult()
            val loading: ProgressBar = findViewById(R.id.progressBar2)
            loading.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                for (id in gamesId){
                      val url = URL("https://www.boardgamegeek.com/xmlapi2/thing?id=$id&stats=1")
                    val connection = url.openConnection()
                    connection.connect()
                    val lengthOfFile = connection.contentLength
                    val isStream = url.openStream()
                    val directory = File("$filesDir/XML")
                     if (!directory.exists()) directory.mkdir()
                    val fos = FileOutputStream("$directory/game.xml")
                    val data = ByteArray(1024)
                    var count = 0
                    var total: Long = 0
                    var progress = 0
                    count = isStream.read(data)
                    while (count != -1) {
                        total += count.toLong()
                        val progressTemp = total.toInt() * 100 / lengthOfFile
                        if (progressTemp % 10 == 0 && progress != progressTemp)
                            progress = progressTemp
                        fos.write(data, 0, count)
                        count = isStream.read(data)
                    }
                    isStream.close()
                    fos.close()
                    val game = findDetails()
                    game.bggId = id.toInt()
                    if (!dbHandler.checkIfGameExists(game.title.toString()))
                        dbHandler.addGame(game)
                    for (artist in gameArtists)
                        dbHandler.addArtist(game.title.toString(), artist)
                    for (designer in gameDesigners)
                        dbHandler.addDesigner(game.title.toString(), designer)
                    for(expansion in gameExpansions)
                        dbHandler.addExpansion(expansion,game.title.toString())
                    dbHandler.addRank(game.rank,game.title.toString())
                }
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }

        private fun findDetails(): Game {
            gameArtists = ArrayList()
            gameDesigners = ArrayList()
            gameExpansions = ArrayList()
            var game = Game()
            val inDir = File("$filesDir/XML")
            if (!inDir.exists()) inDir.mkdir()
            if (inDir.exists()) {
                val file = File(inDir, "game.xml")
                if (file.exists()) {
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    val ranks: NodeList = xmlDoc.getElementsByTagName("ranks")
                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)
                        if (itemNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = itemNode as Element
                            when(elem.getAttribute("type")){
                                "boardgameexpansion" -> game.type = "dodatek"
                                "boardgame" -> game.type = "podstawowa"
                                else -> game.type = "mieszana"
                            }
                            val children = elem.childNodes
                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    when (node.nodeName) {
                                        "thumbnail" -> {
                                            game.image = node.textContent
                                        }
                                        "description" -> {
                                            game.description = clearDescription(node.textContent)
                                        }
                                        "yearpublished" -> {
                                            game.year = node.getAttribute("value").toInt()
                                        }
                                        "link" -> {
                                            if (node.getAttribute("type") == "boardgamedesigner")
                                                gameDesigners.add(node.getAttribute("value"))
                                            else if (node.getAttribute("type") == "boardgameartist")
                                                gameArtists.add(node.getAttribute("value"))
                                            else if (node.getAttribute("type") == "boardgameexpansion")
                                                gameExpansions.add(node.getAttribute("value"))
                                        }
                                        "name" -> {
                                            if (node.getAttribute("type") == "primary")
                                                game.originalTitle = node.getAttribute("value")
                                            game.title = game.originalTitle
                                        }

                                    }
                                }
                            }
                        }
                    }
                    for (i in 0 until ranks.length) {
                        val ranksNode: Node = ranks.item(i)
                        if (ranksNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = ranksNode as Element
                            val children = elem.childNodes
                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    when (node.nodeName) {
                                        "rank" -> {
                                            if (node.getAttribute("name") == "boardgame"){
                                                if (node.getAttribute("value") != "Not Ranked")
                                                    game.rank = node.getAttribute("value").toInt()
                                                else
                                                    game.rank = 0
                                            }


                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return game
        }
    }

    private inner class RankingDownloader : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val rd2 = RankingDownloader2()
            rd2.execute()
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val textInput: TextInputEditText = findViewById(R.id.textInput3)
                val username = textInput.text
                getSharedPreferences(prefsName, MODE_PRIVATE).edit().putString(prefsUsername, username.toString()).apply();
                val url = URL("https://www.boardgamegeek.com/xmlapi2/collection?username=$username&stats=1")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val directory = File("$filesDir/XML")
                if (!directory.exists()) directory.mkdir()
                val fos = FileOutputStream("$directory/dataStats.xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progressTemp = total.toInt() * 100 / lengthOfFile
                    if (progressTemp % 10 == 0 && progress != progressTemp)
                        progress = progressTemp
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
                updateRank()
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }

        private fun updateRank() {
            val inDir = File("$filesDir/XML")
            idToUpdate = ArrayList()
            rankToUpdate = ArrayList()
            if (!inDir.exists()) inDir.mkdir()
            if (inDir.exists()) {
                val file = File(inDir, "dataStats.xml")
                if (file.exists()) {
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    val ranks: NodeList = xmlDoc.getElementsByTagName("ranks")
                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)
                        if (itemNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = itemNode as Element
                            idToUpdate.add(elem.getAttribute("objectid").toInt())
                        }
                    }
                    for (i in 0 until ranks.length) {
                        val ranksNode: Node = ranks.item(i)
                        if (ranksNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = ranksNode as Element
                            val children = elem.childNodes
                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    when (node.nodeName) {
                                        "rank" -> {
                                            if (node.getAttribute("name") == "boardgame")
                                                if (node.getAttribute("value") != "Not Ranked")
                                                    rankToUpdate.add(node.getAttribute("value").toInt())
                                                else
                                                    rankToUpdate.add(0)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (i in 0 until idToUpdate.size)
                        if(dbHandler.checkIfGameExistsBgg(idToUpdate[i])){
                            dbHandler.updateGameRank(idToUpdate[i],rankToUpdate[i])
                            dbHandler.addRank(rankToUpdate[i],dbHandler.findGameFromBggId(idToUpdate[i]))
                        }
                }
            }
        }
    }

    private inner class RankingDownloader2 : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            displayResult2()
            val loading: ProgressBar = findViewById(R.id.progressBar2)
            loading.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                for (id in dbHandler.readGames2())
                    if(id !in idToUpdate){
                        val url = URL("https://www.boardgamegeek.com/xmlapi2/thing?id=$id&stats=1")
                        val connection = url.openConnection()
                        connection.connect()
                        val lengthOfFile = connection.contentLength
                        val isStream = url.openStream()
                        val directory = File("$filesDir/XML")
                        if (!directory.exists()) directory.mkdir()
                        val fos = FileOutputStream("$directory/game.xml")
                        val data = ByteArray(1024)
                        var count = 0
                        var total: Long = 0
                        var progress = 0
                        count = isStream.read(data)
                        while (count != -1) {
                            total += count.toLong()
                            val progressTemp = total.toInt() * 100 / lengthOfFile
                            if (progressTemp % 10 == 0 && progress != progressTemp)
                                progress = progressTemp
                            fos.write(data, 0, count)
                            count = isStream.read(data)
                        }
                        isStream.close()
                        fos.close()
                        updateRank()
                    }
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }

        private fun updateRank() {
            gameArtists = ArrayList()
            gameDesigners = ArrayList()
            gameExpansions = ArrayList()
            var rank: Int = 0
            var id: Int = 0
            val inDir = File("$filesDir/XML")
            if (!inDir.exists()) inDir.mkdir()
            if (inDir.exists()) {
                val file = File(inDir, "game.xml")
                if (file.exists()) {
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    val ranks: NodeList = xmlDoc.getElementsByTagName("ranks")
                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)
                        if (itemNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = itemNode as Element
                            id = elem.getAttribute("id").toInt()
                        }
                    }
                    for (i in 0 until ranks.length) {
                        val ranksNode: Node = ranks.item(i)
                        if (ranksNode.nodeType == Node.ELEMENT_NODE) {
                            val elem = ranksNode as Element
                            val children = elem.childNodes
                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    when (node.nodeName) {
                                        "rank" -> {
                                            if (node.getAttribute("name") == "boardgame"){
                                                if (node.getAttribute("value") != "Not Ranked")
                                                    rank = node.getAttribute("value").toInt()
                                                else
                                                    rank = 0
                                            }


                                        }
                                    }
                                }
                            }
                        }
                    }
                    dbHandler.updateGameRank(id,rank)
                    dbHandler.addRank(rank,dbHandler.findGameFromBggId(id))
                }
            }
        }
    }

    private fun displayResult(){
        if(gamesId.isNotEmpty())
            Toast.makeText(this, "Dodano gry do kolekcji", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(this, "Nie znaleziono gier", Toast.LENGTH_LONG).show()
    }

    private fun displayResult2(){
        Toast.makeText(this, "Pobrano aktualny ranking", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_bgg)
        val textInput: TextInputEditText = findViewById(R.id.textInput3)
        val pref = getSharedPreferences(prefsName, MODE_PRIVATE)
        textInput.setText(pref.getString(prefsUsername, null))
        val searchGameButton: Button = findViewById(R.id.SubmitButton3)
        searchGameButton.setOnClickListener{
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Chcesz pobrac kolekcje uzytkownika: \"" + textInput.text + "\"?")
            builder.setPositiveButton("Tak") { _, _ ->
                run {
                    val fd = FileDownloader()
                    fd.execute()
                    val loading: ProgressBar = findViewById(R.id.progressBar2)
                    loading.visibility = View.VISIBLE
                }
            }
            builder.setNegativeButton("Nie") { _, _ -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        val updateButton: Button = findViewById(R.id.updateButton)
        updateButton.setOnClickListener{
            val rd = RankingDownloader()
            rd.execute()
            val loading: ProgressBar = findViewById(R.id.progressBar2)
            loading.visibility = View.VISIBLE

        }
    }
}