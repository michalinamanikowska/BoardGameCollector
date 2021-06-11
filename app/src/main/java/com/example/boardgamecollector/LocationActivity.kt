package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


class LocationActivity : AppCompatActivity() {
    private val dbHandler = MyDBHandler(this, null, null, 1)
    private var locToDelete: MutableList<String> = ArrayList()
    private var locations: MutableList<String> = ArrayList()
    private var saveMode = false
    private var emptyTable = true

    @SuppressLint("ClickableViewAccessibility")
    private fun writeData() {
        locations = dbHandler.readLocations()
        val tableLayout: TableLayout = findViewById(R.id.tableLayout4)
        tableLayout.removeAllViews()
        locToDelete = ArrayList()
        emptyTable = locations.size==0
        for(i in 0 until locations.size){
            val row = TableRow(this)
            val locationView = TextView(this)
            val checkBox = CheckBox(this)
            locationView.textSize = 17F
            locationView.text = locations[i]
            row.updatePadding(40,12,0,12)
            if (saveMode){
                row.addView(checkBox)
                row.updatePadding(0,3,0,0)
            }
            row.addView(locationView)
            tableLayout.addView(row)

            row.setOnClickListener {
                val games = dbHandler.findGameFromLoc(locations[i])
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                if(games.isEmpty())
                    builder.setTitle("Brak gier w lokalizacji ${locations[i]}")
                else{
                    builder.setTitle("Gry w lokalizacji ${locations[i]}")
                    builder.setMessage(games.joinToString(separator = "\n"))
                }
                builder.setPositiveButton("Ok") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }

            checkBox.setOnClickListener{
                if(checkBox.isChecked)
                    locToDelete.add(locations[i])
                else
                    locToDelete.remove(locations[i])
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        writeData()

        val saveButton: Button = findViewById(R.id.saveButton2)
        saveButton.setOnClickListener{
            val cantDelete: MutableList<String> = ArrayList()
            for(loc in locToDelete){
                if(dbHandler.checkIfLocDel(loc))
                    dbHandler.deleteLocation(loc)
                else
                    cantDelete.add(loc)
            }
            if(cantDelete.isNotEmpty())
                Toast.makeText(this, "Nie można usunąć lokalizacji: " + cantDelete.joinToString(separator = ", "), Toast.LENGTH_LONG).show()
            saveMode = false
            writeData()
            saveButton.visibility = Button.INVISIBLE
        }

        val deleteButton: FloatingActionButton = findViewById(R.id.deleteButton2)
        deleteButton.setOnClickListener{
            saveMode = !saveMode
            if(saveMode)
                saveButton.visibility = Button.VISIBLE
            else
                saveButton.visibility = Button.INVISIBLE
            writeData()
            if(emptyTable)
                saveButton.visibility = Button.INVISIBLE
        }

        val editButton: FloatingActionButton = findViewById(R.id.editButton2)
        editButton.setOnClickListener{
            var chosenLoc: String = ""
            val table = TableLayout(this)
            val newLoc = TextInputEditText(this)
            newLoc.textSize = 16F
            table.updatePadding(5,0,0,0)
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Wybierz lokalizację do edycji:")
            val spinner = Spinner(this)
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
                    newLoc.setText(chosenLoc)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            table.addView(spinner)
            table.addView(newLoc)
            builder.setView(table)
            builder.setPositiveButton("Zapisz") { _, _ ->
                if(newLoc.text.toString() != chosenLoc){
                    if(!dbHandler.checkIfLocExists(newLoc.text.toString())) {
                        dbHandler.editLocation(chosenLoc,newLoc.text.toString())
                        writeData()
                    }
                    else
                        Toast.makeText(this, "Podana lokalizacja już istnieje", Toast.LENGTH_LONG).show()
                }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        val submitButton: Button = findViewById(R.id.submitButton2)
        submitButton.setOnClickListener{
            val textInput: TextInputEditText = findViewById(R.id.textInput2)
            if(!dbHandler.checkIfLocExists(textInput.text.toString())){
                dbHandler.addLocation(textInput.text.toString())
                textInput.text?.clear()
                writeData()
            }
            else
                Toast.makeText(this, "Podana lokalizacja już istnieje", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRestart() {
        super.onRestart()
        writeData()
    }
}