package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    private val dbHandler = MyDBHandler(this, null, null, 1)
    private var sortColumn = "title"
    private var titleToDelete: MutableList<String> = ArrayList()
    private var deleteMode = false
    private var emptyTable = true
    private var showExpansions = true

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun writeData() {
        var data = dbHandler.readGames(sortColumn)
        val tableLayout: TableLayout = findViewById(R.id.tableLayout3)
        tableLayout.removeAllViews()
        titleToDelete = ArrayList()
        emptyTable = data.size==0
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        for(i in 0 until data.size) {
            if ((showExpansions && data[i].type == "dodatek") || data[i].type !="dodatek") {
                val verticalLayout = LinearLayout(this)
                val tableRow = TableRow(this)
                val titleView = TextView(this)
                val rankView = TextView(this)
                val descriptionView = TextView(this)
                val checkBox = CheckBox(this)
                val imageView = ImageView(this)
                val params = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
                )
                params.setMargins(10, 10, 10, 40)
                checkBox.minWidth = 0
                checkBox.minHeight = 0
                checkBox.layoutParams = params
                rankView.layoutParams = params
                imageView.layoutParams = params
                verticalLayout.layoutParams = params
                verticalLayout.orientation = LinearLayout.VERTICAL

                var title = "<b>${data[i].title} </b> (${data[i].year})"
                titleView.text = Html.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY)
                rankView.text = data[i].rank.toString()
                if (data[i].type == "dodatek")
                    rankView.text = "dod."
                if (data[i].description?.length!! > 200)
                    descriptionView.text = data[i].description?.substring(0, 200) + "..."
                else
                    descriptionView.text = data[i].description
                Picasso.get().load(data[i].image).resize(200, 0).into(imageView)

                if (deleteMode)
                    tableRow.addView(checkBox)
                tableRow.addView(rankView)
                verticalLayout.addView(titleView)
                verticalLayout.addView(descriptionView)
                tableRow.addView(verticalLayout)
                tableRow.addView(imageView)
                tableLayout.addView(tableRow)

                titleView.textSize = 17F
                rankView.textSize = 17F
                tableRow.minimumHeight = 250
                if (deleteMode) {
                    descriptionView.maxWidth = (screenWidth * 0.55).toInt()
                    titleView.maxWidth = (screenWidth * 0.55).toInt()
                } else {
                    descriptionView.maxWidth = (screenWidth * 0.65).toInt()
                    titleView.maxWidth = (screenWidth * 0.65).toInt()
                }
                rankView.minWidth = (screenWidth * 0.07).toInt()
                checkBox.minimumWidth = 0
                checkBox.minimumHeight = 0
                rankView.gravity = Gravity.CENTER
                checkBox.gravity = Gravity.CENTER
                tableLayout.gravity = Gravity.CENTER

                tableRow.setOnClickListener {
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("chosenGame", data[i].title)
                    startActivity(intent)
                }
                checkBox.setOnClickListener {
                    if (checkBox.isChecked)
                        titleToDelete.add(data[i].title.toString())
                    else
                        titleToDelete.remove(data[i].title.toString())
                }
            }
        }
        val tableRow = TableRow(this)
        tableRow.minimumHeight = 200
        tableLayout.addView(tableRow)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        writeData()

        val searchButton: FloatingActionButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener{
            val intent = Intent(this, SearchBggActivity::class.java)
            startActivity(intent)
        }

        val locationButton: FloatingActionButton = findViewById(R.id.locationButton)
        locationButton.setOnClickListener{
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener{
            for(title in titleToDelete){
                dbHandler.deleteGame(title)
            }
            deleteMode = false
            writeData()
            saveButton.visibility = Button.INVISIBLE
        }

        val deleteButton: FloatingActionButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener{
            deleteMode = !deleteMode
            if(deleteMode)
                saveButton.visibility = Button.VISIBLE
            else
                saveButton.visibility = Button.INVISIBLE
            writeData()
            if(emptyTable)
                saveButton.visibility = Button.INVISIBLE
        }

        val addButton: FloatingActionButton = findViewById(R.id.addButton)
        addButton.setOnClickListener{
            val intent = Intent(this, AddGameActivity::class.java)
            startActivity(intent)
        }

        val selectSpinner: Spinner = findViewById(R.id.selectSpinner)
        var options = arrayOf("tytuł", "ranking", "data wydania")
        selectSpinner.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                options
        )
        selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                sortColumn = options[position]
                writeData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val showExpansionsCheckbox: CheckBox = findViewById(R.id.showExpansionsCheckbox)
        showExpansionsCheckbox.isChecked = true
        showExpansionsCheckbox.setOnClickListener {
            showExpansions = showExpansionsCheckbox.isChecked
            writeData()
        }

    }

    override fun onRestart() {
        super.onRestart()
        writeData()
    }
}
