package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
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
import java.time.LocalDate
import javax.xml.parsers.DocumentBuilderFactory


class AddGameActivity : AppCompatActivity() {
    private val dbHandler = MyDBHandler(this, null, null, 1)
    var gamesTitle: MutableList<String> = ArrayList()
    var gamesId: MutableList<String> = ArrayList()
    var gameArtists: MutableList<String> = ArrayList()
    var gameDesigners: MutableList<String> = ArrayList()
    var gameExpansions: MutableList<String> = ArrayList()
    var chosenId: Int? = null
    var chosenGame = Game()

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

    private inner class FileDownloader: AsyncTask<String, Int, String>(){

        override fun onPreExecute(){
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            displayGames()
            val loading: ProgressBar = findViewById(R.id.progressBar)
            loading.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String {
            try{
                val textInput: TextInputEditText = findViewById(R.id.textInput)
                val text = textInput.text
                val url = URL("https://www.boardgamegeek.com/xmlapi2/search?query=$text&type=boardgame")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val directory = File("$filesDir/XML")
                if(!directory.exists()) directory.mkdir()
                val fos = FileOutputStream("$directory/search.xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while(count != -1){
                    total += count.toLong()
                    val progressTemp = total.toInt()*100/lengthOfFile
                    if (progressTemp%10==0 && progress!=progressTemp)
                        progress = progressTemp
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
                findGame()
            }catch (e: MalformedURLException){
                return "Malformed URL"
            }catch (e: FileNotFoundException){
                return "File not found"
            }catch (e: IOException){
                return "IO Exception"
            }
            return "success"
        }

        private fun findGame(){
            gamesTitle = ArrayList()
            gamesId = ArrayList()
            val inDir = File("$filesDir/XML")
            if(!inDir.exists()) inDir.mkdir()
            if(inDir.exists()){
                val file = File(inDir, "search.xml")
                if(file.exists()){
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    for (i in 0 until items.length){
                        val itemNode: Node = items.item(i)
                        if(itemNode.nodeType == Node.ELEMENT_NODE){
                            val elem = itemNode as Element
                            gamesId.add(elem.getAttribute("id"))
                            val children = elem.childNodes
                            for (j in 0 until children.length){
                                val node = children.item(j)
                                if(node is Element){
                                    when(node.nodeName){
                                        "name" -> {
                                            gamesTitle.add(node.getAttribute("value"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class DetailsDownloader: AsyncTask<String, Int, String>(){

        override fun onPreExecute(){
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            loadGame()
        }

        override fun doInBackground(vararg params: String?): String {
            try{
                val url = URL("https://www.boardgamegeek.com/xmlapi2/thing?id=$chosenId&stats=1")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val directory = File("$filesDir/XML")
                if(!directory.exists()) directory.mkdir()
                val fos = FileOutputStream("$directory/game.xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while(count != -1){
                    total += count.toLong()
                    val progressTemp = total.toInt()*100/lengthOfFile
                    if (progressTemp%10==0 && progress!=progressTemp)
                        progress = progressTemp
                    fos.write(data, 0, count)
                        count = isStream.read(data)
                }
                isStream.close()
                fos.close()
                findDetails()
            }catch (e: MalformedURLException){
                return "Malformed URL"
            }catch (e: FileNotFoundException){
                return "File not found"
            }catch (e: IOException){
                return "IO Exception"
            }
            return "success"
        }

        private fun findDetails(){
            gamesTitle = ArrayList()
            gamesId = ArrayList()
            val inDir = File("$filesDir/XML")
            if(!inDir.exists()) inDir.mkdir()
            if(inDir.exists()){
                val file = File(inDir, "game.xml")
                if(file.exists()){
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                            file
                    )
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    val ranks: NodeList = xmlDoc.getElementsByTagName("ranks")
                    for (i in 0 until items.length){
                        val itemNode: Node = items.item(i)
                        if(itemNode.nodeType == Node.ELEMENT_NODE){
                            val elem = itemNode as Element
                            when(elem.getAttribute("type")){
                                "boardgameexpansion" -> chosenGame.type = "dodatek"
                                "boardgame" -> chosenGame.type = "podstawowa"
                                else -> chosenGame.type = "mieszana"
                            }
                            val children = elem.childNodes
                            for (j in 0 until children.length){
                                val node = children.item(j)
                                if(node is Element){
                                    when(node.nodeName){
                                        "thumbnail" -> {
                                            chosenGame.image = node.textContent
                                        }
                                        "description" -> {
                                            chosenGame.description = clearDescription(node.textContent)
                                        }
                                        "yearpublished" -> {
                                            chosenGame.year = node.getAttribute("value").toInt()
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
                                                chosenGame.originalTitle = node.getAttribute("value")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (i in 0 until ranks.length){
                        val ranksNode: Node = ranks.item(i)
                        if(ranksNode.nodeType == Node.ELEMENT_NODE){
                            val elem = ranksNode as Element
                            val children = elem.childNodes
                            for (j in 0 until children.length){
                                val node = children.item(j)
                                if(node is Element){
                                    when(node.nodeName){
                                        "rank" -> {
                                            if (node.getAttribute("name") == "boardgame") {
                                                if (node.getAttribute("value") != "Not Ranked")
                                                    chosenGame.rank = node.getAttribute("value").toInt()
                                                else
                                                    chosenGame.rank = 0
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showFields(){
        val scrollGames: ScrollView = findViewById(R.id.scrollGames)
        val scrollFields: ScrollView = findViewById(R.id.scrollFields)
        scrollGames.visibility = View.GONE
        scrollFields.visibility = View.VISIBLE
        val game = Game()
        game.rank = 0

        val titleView: TextInputEditText = findViewById(R.id.titleNewView)
        val originalTitleView: TextInputEditText = findViewById(R.id.originalTitleNewView)
        val descriptionView: TextInputEditText = findViewById(R.id.descriptionNewView)
        val designersView: TextInputEditText = findViewById(R.id.designersNewView)
        val artistsView: TextInputEditText = findViewById(R.id.artistsNewView)
        val expansionsView: TextInputEditText = findViewById(R.id.expansionsNewView)
        val priceView: TextInputEditText = findViewById(R.id.priceNewView)
        val scdView: TextInputEditText = findViewById(R.id.scdNewView)
        val eanView: TextInputEditText = findViewById(R.id.eanNewView)
        val codeView: TextInputEditText = findViewById(R.id.codeNewView)
        val commentView: TextInputEditText = findViewById(R.id.commentNewView)
        val yearPicker: NumberPicker = findViewById(R.id.yearNewPicker)
        val spinner: Spinner = findViewById(R.id.spinner)
        val spinner2: Spinner = findViewById(R.id.spinner2)
        val addDatePicker: DatePicker = findViewById(R.id.addDatePicker)
        val orderDatePicker: DatePicker = findViewById(R.id.orderDatePicker)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2030
        yearPicker.value = 2000
        yearPicker.wrapSelectorWheel = false
        game.year = 2000
        val locations = dbHandler.readLocations()
        val types = arrayOf("podstawowa", "dodatek", "mieszana")
        var chosenLoc: String? = null
        var chosenType: String? = null
        var addDate: String? = null
        var orderDate: String? = null

        spinner.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                locations
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                chosenLoc = locations[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinner2.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                types
        )
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                chosenType = types[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        addDatePicker.setOnDateChangedListener(){ _, year, month, day ->
            addDate = LocalDate.of(year, month + 1, day).toString()
        }

        orderDatePicker.setOnDateChangedListener(){ _, year, month, day ->
            orderDate = LocalDate.of(year, month + 1, day).toString()
        }

        yearPicker.setOnValueChangedListener(){ _, _, newVal ->
            game.year = newVal
        }

        val saveButton: Button = findViewById(R.id.saveNewButton)
        saveButton.setOnClickListener {
            game.title = titleView.text.toString()
            game.originalTitle = originalTitleView.text.toString()
            game.description = descriptionView.text.toString()
            game.price = priceView.text.toString()
            game.scd = scdView.text.toString()
            game.ean = eanView.text.toString()
            game.comment = commentView.text.toString()
            game.code = codeView.text.toString()
            game.type = chosenType
            game.location = chosenLoc
            game.addDate = addDate
            game.orderDate = orderDate
            for (artist in artistsView.text?.split("\n")!!)
                dbHandler.addArtist(game.title.toString(), artist)
            for (designer in designersView.text?.split("\n")!!)
                dbHandler.addDesigner(game.title.toString(), designer)
            for (expansion in expansionsView.text?.split("\n")!!)
                dbHandler.addExpansion(expansion, game.title.toString())

            if(game.title == "")
                Toast.makeText(this, "Tytuł gry nie może być pusty", Toast.LENGTH_LONG).show()
            else{
                if(!dbHandler.checkIfGameExists(game.title.toString())){
                    dbHandler.addGame(game)
                    Toast.makeText(this, "Dodano do kolekcji: \"" + game.title + "\"", Toast.LENGTH_LONG).show()
                    scrollFields.visibility = View.GONE
                }
                else
                    Toast.makeText(this, "Podana gra jest już w Twojej kolekcji", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    private fun displayGames(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val scrollGames: ScrollView = findViewById(R.id.scrollGames)
        val scrollFields: ScrollView = findViewById(R.id.scrollFields)
        val tableLayout: TableLayout = findViewById(R.id.tableLayout3)
        scrollGames.visibility = View.VISIBLE
        scrollFields.visibility = View.GONE
        tableLayout.removeAllViewsInLayout()
        tableLayout.visibility = View.VISIBLE
        for (i in 0 until gamesTitle.size){
            val row = TableRow(this)
            val titleView = TextView(this)
            titleView.maxWidth = displayMetrics.widthPixels - 50
            titleView.text = gamesTitle[i]
            titleView.textSize = 17F
            row.addView(titleView)
            tableLayout.addView(row)
            row.setOnClickListener {
                chosenId = gamesId[i].toInt()
                chosenGame.title = gamesTitle[i]
                chosenGame.bggId = chosenId
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("Chcesz dodać grę: \"" + gamesTitle[i] + "\" do swojej kolekcji?")
                builder.setPositiveButton("Tak") { _, _ ->
                    run {
                        tableLayout.removeAllViewsInLayout()
                        tableLayout.visibility = View.GONE
                        val dd = DetailsDownloader()
                        dd.execute()
                    }
                }
                builder.setNegativeButton("Nie") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }
        }
        val addNewButton: Button = findViewById(R.id.AddNewButton)
        addNewButton.setOnClickListener {
            showFields()
        }
    }

    private fun loadGame(){
        if(!dbHandler.checkIfGameExists(chosenGame.title.toString())){
            dbHandler.addGame(chosenGame)
            Toast.makeText(this, "Dodano do kolekcji: \"" + chosenGame.title + "\"", Toast.LENGTH_SHORT).show()
            for (artist in gameArtists)
                dbHandler.addArtist(chosenGame.title.toString(), artist)
            for (designer in gameDesigners)
                dbHandler.addDesigner(chosenGame.title.toString(), designer)
            for(expansion in gameExpansions)
                dbHandler.addExpansion(expansion, chosenGame.title.toString())
            dbHandler.addRank(chosenGame.rank, chosenGame.title.toString())
        }
        else
            Toast.makeText(this, "Podana gra jest już w Twojej kolekcji", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_game)

        val searchGameButton: Button = findViewById(R.id.SubmitButton)
        searchGameButton.setOnClickListener{
            val fd = FileDownloader()
            fd.execute()
            val loading: ProgressBar = findViewById(R.id.progressBar)
            loading.visibility = View.VISIBLE
        }
    }
}