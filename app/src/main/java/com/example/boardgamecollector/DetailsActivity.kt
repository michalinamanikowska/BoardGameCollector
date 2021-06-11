package com.example.boardgamecollector

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.solver.state.State
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate

class DetailsActivity : AppCompatActivity() {
    private val dbHandler = MyDBHandler(this, null, null, 1)
    private var game = Game()

    private fun rankMessage(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Historia rankingu: ")
        builder.setMessage(dbHandler.findRanking(game.title.toString()))
        builder.setNegativeButton("Zamknij") { _, _ -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun updateGame(){

        val originalTitleView: TextInputEditText = findViewById(R.id.originalTitleEditView)
        val descriptionView: TextInputEditText = findViewById(R.id.descriptionEditView)
        val designersView: TextInputEditText = findViewById(R.id.designersEditView)
        val artistsView: TextInputEditText = findViewById(R.id.artistsEditView)
        val expansionsView: TextInputEditText = findViewById(R.id.expansionsEditView)
        val priceView: TextInputEditText = findViewById(R.id.priceEditView)
        val scdView: TextInputEditText = findViewById(R.id.scdEditView)
        val eanView: TextInputEditText = findViewById(R.id.eanEditView)
        val codeView: TextInputEditText = findViewById(R.id.codeEditView)
        val commentView: TextInputEditText = findViewById(R.id.commentEditView)
        val yearPicker: NumberPicker = findViewById(R.id.yearEditPicker)
        val spinner: Spinner = findViewById(R.id.spinnerEdit)
        val spinner2: Spinner = findViewById(R.id.spinnerEdit2)
        val addDatePicker: DatePicker = findViewById(R.id.addEditDatePicker)
        val orderDatePicker: DatePicker = findViewById(R.id.orderEditDatePicker)

        yearPicker.minValue = 1900
        yearPicker.maxValue = 2030
        yearPicker.value = game.year!!
        yearPicker.wrapSelectorWheel = false
        val locations = dbHandler.readLocations()

        val types = arrayOf("podstawowa","dodatek","mieszana")
        var chosenLoc: String? = null
        var chosenType: String? = null
        var addDate: String? = null
        var orderDate: String? = null

        originalTitleView.setText(game.originalTitle)
        descriptionView.setText(game.description)
        priceView.setText(game.price)
        scdView.setText(game.scd)
        eanView.setText(game.ean)
        codeView.setText(game.code)
        commentView.setText(game.comment)
        designersView.setText(dbHandler.findDesigners(game.title.toString()))
        artistsView.setText(dbHandler.findArtists(game.title.toString()))
        expansionsView.setText(dbHandler.findExpansions(game.title.toString()))

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
        if(game.location != null && game.location != "")
            spinner.setSelection(locations.indexOf(game.location))
        if(game.type != null && game.type != "")
            spinner2.setSelection(types.indexOf(game.type))

        if(game.addDate != null && game.addDate != "")
            addDatePicker.init(game.addDate!!.substring(0,4).toInt(),game.addDate!!.substring(5,7).toInt()-1,game.addDate!!.substring(8,10).toInt(),null)

        if(game.orderDate != null && game.orderDate != "")
            orderDatePicker.init(game.orderDate!!.substring(0,4).toInt(),game.orderDate!!.substring(5,7).toInt()-1,game.orderDate!!.substring(8,10).toInt(),null)

        addDatePicker.setOnDateChangedListener(){ _, year, month, day ->
            addDate = LocalDate.of(year,month+1,day).toString()
        }

        orderDatePicker.setOnDateChangedListener(){ _, year, month, day ->
            orderDate = LocalDate.of(year,month+1,day).toString()
        }

        yearPicker.setOnValueChangedListener(){ _, _, newVal ->
            game.year = newVal
        }

        val saveButton: FloatingActionButton = findViewById(R.id.submitButton4)
        saveButton.setOnClickListener {
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

            dbHandler.updateArtists(artistsView.text.toString(),game.title.toString())
            dbHandler.updateDesigners(designersView.text.toString(),game.title.toString())
            dbHandler.updateExpansions(expansionsView.text.toString(),game.title.toString())
            dbHandler.updateWholeGame(game)
            Toast.makeText(this,"Zapisano informacje o grze",Toast.LENGTH_LONG).show()
            val detailsView: ScrollView = findViewById(R.id.DetailsScrollView)
            val editView: ScrollView = findViewById(R.id.EditScrollView)
            val editButton: FloatingActionButton = findViewById(R.id.editButton3)
            detailsView.visibility = View.VISIBLE
            editView.visibility = View.GONE
            editButton.visibility = View.VISIBLE
            saveButton.visibility = View.GONE
            writeData()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun writeData() {
        val titleView: TextView = findViewById(R.id.titleView)
        val originalView: TextView = findViewById(R.id.originalView)
        val yearView: TextView = findViewById(R.id.yearView)
        val designersView: TextView = findViewById(R.id.designersView)
        val artistsView: TextView = findViewById(R.id.artistsView)
        val expansionsView: TextView = findViewById(R.id.expansionsView)
        val descriptionView: TextView = findViewById(R.id.descriptionView)
        val bggIdView: TextView = findViewById(R.id.bggIdView)
        val rankView: TextView = findViewById(R.id.rankView)
        val rankText: TextView = findViewById(R.id.textView12)
        val locView: TextView = findViewById(R.id.locView)
        val priceView: TextView = findViewById(R.id.priceView)
        val addDateView: TextView = findViewById(R.id.addDateView)
        val orderDateView: TextView = findViewById(R.id.orderDateView)
        val scdView: TextView = findViewById(R.id.scdView)
        val eanView: TextView = findViewById(R.id.eanView)
        val codeView: TextView = findViewById(R.id.codeView)
        val typeView: TextView = findViewById(R.id.typeView)
        val commentView: TextView = findViewById(R.id.commentView)

        titleView.text = game.title?.toUpperCase()
        originalView.text = game.originalTitle
        yearView.text = game.year.toString()
        designersView.text = dbHandler.findDesigners(game.title.toString())
        artistsView.text = dbHandler.findArtists(game.title.toString())
        expansionsView.text = dbHandler.findExpansions(game.title.toString())
        descriptionView.text = game.description
        priceView.text = game.price
        addDateView.text = game.addDate
        orderDateView.text = game.orderDate
        scdView.text = game.scd
        eanView.text = game.ean
        codeView.text = game.code
        typeView.text = game.type
        commentView.text = game.comment

        bggIdView.text = game.bggId.toString()
        if(game.rank == -1)
            rankView.text = "Not Ranked"
        else
            rankView.text = game.rank.toString()
        locView.text = game.location

        rankView.setOnClickListener {
            rankMessage()
        }

        rankText.setOnClickListener {
            rankMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val title: String? = intent.getStringExtra("chosenGame")
        val editButton: FloatingActionButton = findViewById(R.id.editButton3)
        val saveButton: FloatingActionButton = findViewById(R.id.submitButton4)
        game = dbHandler.findGame(title.toString())
        val detailsView: ScrollView = findViewById(R.id.DetailsScrollView)
        val editView: ScrollView = findViewById(R.id.EditScrollView)
        detailsView.visibility = View.VISIBLE
        editView.visibility = View.GONE
        editButton.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
        writeData()
        editButton.setOnClickListener{
            detailsView.visibility = View.GONE
            editView.visibility = View.VISIBLE
            editButton.visibility = View.GONE
            saveButton.visibility = View.VISIBLE
            updateGame()
        }
    }

    override fun onRestart() {
        super.onRestart()
        val editButton: FloatingActionButton = findViewById(R.id.editButton3)
        val saveButton: FloatingActionButton = findViewById(R.id.submitButton4)
        val detailsView: ScrollView = findViewById(R.id.DetailsScrollView)
        val editView: ScrollView = findViewById(R.id.EditScrollView)
        detailsView.visibility = View.VISIBLE
        editView.visibility = View.GONE
        editButton.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
        writeData()
    }
}

