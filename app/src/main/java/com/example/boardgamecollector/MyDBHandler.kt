package com.example.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context, DATABASE_NAME, factory, 1) {
    companion object {
        private const val DATABASE_NAME = "gameDB.db"
        const val TABLE_GAMES = "games"
        const val GAME_TITLE = "title"
        const val GAME_ORIGINAL_TITLE = "originalTitle"
        const val GAME_YEAR = "year"
        const val GAME_DESCRIPTION = "description"
        const val GAME_ORDER_DATE = "orderDate"
        const val GAME_ADD_DATE = "addDate"
        const val GAME_PRICE = "price"
        const val GAME_SCD = "scd"
        const val GAME_EAN = "ean"
        const val GAME_BGG_ID = "bggId"
        const val GAME_CODE = "code"
        const val GAME_RANK = "rank"
        const val GAME_TYPE = "type"
        const val GAME_COMMENT = "comment"
        const val GAME_IMAGE = "image"
        const val GAME_LOCATION = "location"

        const val TABLE_LOCATIONS = "location"
        const val LOCATION_NAME = "name"

        const val TABLE_DESIGNERS = "designers"
        const val DESIGNER_NAME = "name"
        
        const val TABLE_ARTISTS = "artists"
        const val ARTIST_NAME = "name"

        const val TABLE_GAME_DESIGNERS = "gameDesigners"
        const val GAME_DESIGNER_TITLE = "title"
        const val GAME_DESIGNER_NAME = "name"

        const val TABLE_GAME_ARTISTS = "gameArtists"
        const val GAME_ARTIST_TITLE = "title"
        const val GAME_ARTIST_NAME = "name"

        const val TABLE_EXPANSIONS = "expansions"
        const val EXPANSION_ID  = "_id"
        const val EXPANSION_TITLE = "title"
        const val EXPANSION_PARENT = "parent"

        const val TABLE_RANKING = "ranking"
        const val RANKING_ID = "_id"
        const val RANKING_DATE = "date"
        const val RANKING_RANK = "rank"
        const val RANKING_TITLE = "title"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableGames = ("CREATE TABLE $TABLE_GAMES ($GAME_TITLE TEXT PRIMARY KEY, $GAME_LOCATION TEXT, $GAME_DESCRIPTION TEXT, $GAME_IMAGE TEXT, " +
                "$GAME_RANK INTEGER, $GAME_YEAR INTEGER, $GAME_ORIGINAL_TITLE TEXT, $GAME_BGG_ID INTEGER, $GAME_ORDER_DATE TEXT, $GAME_ADD_DATE TEXT, $GAME_PRICE TEXT, " +
                "$GAME_SCD TEXT, $GAME_EAN TEXT, $GAME_CODE TEXT, $GAME_TYPE TEXT, $GAME_COMMENT TEXT, FOREIGN KEY ($GAME_LOCATION) REFERENCES $TABLE_LOCATIONS($LOCATION_NAME))")
        db?.execSQL(createTableGames)
        val createTableLocation = ("CREATE TABLE $TABLE_LOCATIONS ($LOCATION_NAME TEXT PRIMARY KEY)")
        db?.execSQL(createTableLocation)
        val createTableDesigners = ("CREATE TABLE $TABLE_DESIGNERS ($DESIGNER_NAME TEXT PRIMARY KEY)")
        db?.execSQL(createTableDesigners)
        val createTableGameDesigners = ("CREATE TABLE $TABLE_GAME_DESIGNERS ($GAME_DESIGNER_TITLE TEXT, $GAME_DESIGNER_NAME TEXT, PRIMARY KEY ($GAME_DESIGNER_TITLE, $GAME_DESIGNER_NAME), " +
                "FOREIGN KEY ($GAME_DESIGNER_TITLE) REFERENCES $TABLE_GAMES($GAME_TITLE), FOREIGN KEY ($GAME_DESIGNER_NAME) REFERENCES $TABLE_DESIGNERS($DESIGNER_NAME))")
        db?.execSQL(createTableGameDesigners)
        val createTableArtists = ("CREATE TABLE $TABLE_ARTISTS ($ARTIST_NAME TEXT PRIMARY KEY)")
        db?.execSQL(createTableArtists)
        val createTableGameArtists = ("CREATE TABLE $TABLE_GAME_ARTISTS ($GAME_ARTIST_TITLE TEXT, $GAME_ARTIST_NAME TEXT, PRIMARY KEY ($GAME_ARTIST_TITLE, $GAME_ARTIST_NAME), " +
                "FOREIGN KEY ($GAME_ARTIST_TITLE) REFERENCES $TABLE_GAMES($GAME_TITLE), FOREIGN KEY ($GAME_ARTIST_NAME) REFERENCES $TABLE_DESIGNERS($DESIGNER_NAME))")
        db?.execSQL(createTableGameArtists)
        val createTableExpansions = ("CREATE TABLE $TABLE_EXPANSIONS ($EXPANSION_ID INTEGER PRIMARY KEY, $EXPANSION_TITLE TEXT, $EXPANSION_PARENT TEXT, " +
                "FOREIGN KEY ($EXPANSION_PARENT) REFERENCES $TABLE_GAMES($GAME_TITLE))")
        db?.execSQL(createTableExpansions)
        val createTableRanks = ("CREATE TABLE $TABLE_RANKING ($RANKING_ID INTEGER PRIMARY KEY, $RANKING_DATE TEXT, $RANKING_RANK INTEGER, $RANKING_TITLE TEXT, " +
                "FOREIGN KEY ($RANKING_TITLE) REFERENCES $TABLE_GAMES($GAME_TITLE))")
        db?.execSQL(createTableRanks)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DESIGNERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_DESIGNERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ARTISTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_ARTISTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPANSIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RANKING")
        onCreate(db)
    }

    fun readGames(sort: String?): MutableList<Game>{
        var column: String? = null
        when(sort){
            "ranking" -> column = GAME_RANK
            "tytuł" -> column = GAME_TITLE
            "data wydania" -> column = GAME_YEAR
        }
        var list: MutableList<Game> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES ORDER BY $column"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do {
                var game = Game()
                game.title = result.getString(result.getColumnIndex(GAME_TITLE))
                game.description = result.getString(result.getColumnIndex(GAME_DESCRIPTION))
                game.image = result.getString(result.getColumnIndex(GAME_IMAGE))
                game.rank = result.getInt(result.getColumnIndex(GAME_RANK))
                game.year = result.getInt(result.getColumnIndex(GAME_YEAR))
                game.type = result.getString(result.getColumnIndex(GAME_TYPE))
                list.add(game)
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return list
    }

    fun readGames2(): MutableList<Int>{
        var games: MutableList<Int> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do {
                games.add(result.getInt(result.getColumnIndex(GAME_BGG_ID)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return games
    }

    fun addGame(game: Game){
        val values = ContentValues()
        values.put(GAME_TITLE, game.title)
        values.put(GAME_LOCATION, game.location)
        values.put(GAME_DESCRIPTION, game.description)
        values.put(GAME_IMAGE, game.image)
        values.put(GAME_RANK, game.rank)
        values.put(GAME_YEAR, game.year)
        values.put(GAME_ORIGINAL_TITLE, game.originalTitle)
        values.put(GAME_BGG_ID, game.bggId)
        values.put(GAME_ORDER_DATE,game.orderDate)
        values.put(GAME_ADD_DATE,game.addDate)
        values.put(GAME_PRICE,game.price)
        values.put(GAME_SCD,game.scd)
        values.put(GAME_EAN,game.ean)
        values.put(GAME_CODE,game.code)
        values.put(GAME_TYPE,game.type)
        values.put(GAME_COMMENT,game.comment)
        val db = this.writableDatabase
        db.insert(TABLE_GAMES, null, values)
        db.close()
    }

    fun findGame(title: String): Game{
        var game = Game()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            game.title = result.getString(result.getColumnIndex(GAME_TITLE))
            game.location = result.getString(result.getColumnIndex(GAME_LOCATION))
            game.description = result.getString(result.getColumnIndex(GAME_DESCRIPTION))
            game.image = result.getString(result.getColumnIndex(GAME_IMAGE))
            game.rank = result.getInt(result.getColumnIndex(GAME_RANK))
            game.year = result.getInt(result.getColumnIndex(GAME_YEAR))
            game.originalTitle = result.getString(result.getColumnIndex(GAME_ORIGINAL_TITLE))
            game.bggId = result.getInt(result.getColumnIndex(GAME_BGG_ID))
            game.orderDate = result.getString(result.getColumnIndex(GAME_ORDER_DATE))
            game.addDate = result.getString(result.getColumnIndex(GAME_ADD_DATE))
            game.price = result.getString(result.getColumnIndex(GAME_PRICE))
            game.scd = result.getString(result.getColumnIndex(GAME_SCD))
            game.ean = result.getString(result.getColumnIndex(GAME_EAN))
            game.code = result.getString(result.getColumnIndex(GAME_CODE))
            game.type = result.getString(result.getColumnIndex(GAME_TYPE))
            game.comment = result.getString(result.getColumnIndex(GAME_COMMENT))
            result.close()
         }
        db.close()
        return game
    }

    fun findGameFromLoc(location: String): MutableList<String>{
        var games: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_LOCATION = ?"
        val result = db.rawQuery(query, arrayOf(location))
        if (result.moveToFirst()){
            do {
                games.add(result.getString(result.getColumnIndex(GAME_TITLE)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return games
    }

    fun findGameFromBggId(bggId: Int): String{
        var title: String = ""
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_BGG_ID = $bggId"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            title = result.getString(result.getColumnIndex(GAME_TITLE))
            result.close()
        }
        db.close()
        return title
    }

    fun updateGameRank(bggId: Int, newRank: Int){
        val db = this.readableDatabase
        var values = ContentValues()
        values.put(GAME_RANK, newRank)
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_BGG_ID = $bggId"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            db.update(TABLE_GAMES, values, "$GAME_BGG_ID = $bggId", null)
        }
        result.close()
        db.close()
    }

    fun updateGameLoc(title: String, newLocation: String){
        val db = this.readableDatabase
        var values = ContentValues()
        values.put(GAME_LOCATION, newLocation)
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            db.update(TABLE_GAMES, values, "$GAME_TITLE = ?", arrayOf(title))
        }
        result.close()
        db.close()
    }

    fun updateWholeGame(game: Game){
        val db = this.readableDatabase
        var values = ContentValues()
        values.put(GAME_LOCATION, game.location)
        values.put(GAME_DESCRIPTION, game.description)
        values.put(GAME_IMAGE, game.image)
        values.put(GAME_RANK, game.rank)
        values.put(GAME_YEAR, game.year)
        values.put(GAME_ORIGINAL_TITLE, game.originalTitle)
        values.put(GAME_BGG_ID, game.bggId)
        values.put(GAME_ORDER_DATE,game.orderDate)
        values.put(GAME_ADD_DATE,game.addDate)
        values.put(GAME_PRICE,game.price)
        values.put(GAME_SCD,game.scd)
        values.put(GAME_EAN,game.ean)
        values.put(GAME_CODE,game.code)
        values.put(GAME_TYPE,game.type)
        values.put(GAME_COMMENT,game.comment)
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(game.title))
        if (result.moveToFirst()){
            db.update(TABLE_GAMES, values, "$GAME_TITLE = ?", arrayOf(game.title))
        }
        result.close()
        db.close()
    }

    fun deleteGame(title: String) {
        val db = this.writableDatabase
        db.delete(TABLE_GAMES, "$GAME_TITLE = ?", arrayOf(title))
        db.delete(TABLE_EXPANSIONS,"$EXPANSION_PARENT = ?", arrayOf(title))
        db.delete(TABLE_RANKING,"$RANKING_TITLE = ?", arrayOf(title))
        db.delete(TABLE_GAME_ARTISTS,"$GAME_ARTIST_TITLE = ?", arrayOf(title))
        db.delete(TABLE_GAME_DESIGNERS,"$GAME_DESIGNER_TITLE = ?", arrayOf(title))
        db.close()
    }

    fun checkIfGameExists(title: String): Boolean{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            db.close()
            return true
        }
        db.close()
        return false
    }

    fun checkIfGameExistsBgg(bggId: Int): Boolean{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_BGG_ID = $bggId"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            db.close()
            return true
        }
        db.close()
        return false
    }

    fun addLocation(name: String){
        val values = ContentValues()
        values.put(LOCATION_NAME, name)
        val db = this.writableDatabase
        db.insert(TABLE_LOCATIONS, null, values)
        db.close()
    }

    fun deleteLocation(name: String) {
        val db = this.writableDatabase
        db.delete(TABLE_LOCATIONS, "$LOCATION_NAME = ?", arrayOf(name))
        db.close()
    }

    fun editLocation(name: String, newName: String){
        var db = this.writableDatabase
        val db2 = this.readableDatabase
        var values = ContentValues()
        values.put(LOCATION_NAME, newName)
        db.update(TABLE_LOCATIONS, values, "$LOCATION_NAME = ?", arrayOf(name))
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_LOCATION = ?"
        val result = db2.rawQuery(query, arrayOf(name))
        if (result.moveToFirst()){
            do {
                values = ContentValues()
                values.put(GAME_LOCATION, newName)
                db.update(TABLE_GAMES, values, "$GAME_TITLE = ?", arrayOf(result.getString(result.getColumnIndex(GAME_TITLE))))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        db2.close()
    }

    fun readLocations(): MutableList<String>{
        var locations: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_LOCATIONS"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do {
                locations.add(result.getString(result.getColumnIndex(LOCATION_NAME)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return locations
    }

    fun checkIfLocDel(name: String): Boolean{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAMES WHERE $GAME_LOCATION = ?"
        val result = db.rawQuery(query, arrayOf(name))
        if (result.moveToFirst()){
            result.close()
            db.close()
            return false
        }
        result.close()
        db.close()
        return true
    }

    fun checkIfLocExists(name: String): Boolean{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_LOCATIONS WHERE $LOCATION_NAME = ?"
        val result = db.rawQuery(query, arrayOf(name))
        if (result.moveToFirst()){
            result.close()
            db.close()
            return true
        }
        result.close()
        db.close()
        return false
    }

    fun addArtist(title: String, name: String){
        var exists = false
        val db = this.readableDatabase
        var query = "SELECT * FROM $TABLE_ARTISTS WHERE $ARTIST_NAME = ?"
        val result = db.rawQuery(query, arrayOf(name))
        if (result.moveToFirst())
            exists = true
        result.close()

        var exists2 = false
        query = "SELECT * FROM $TABLE_GAME_ARTISTS WHERE $GAME_ARTIST_TITLE = ? AND $GAME_ARTIST_NAME = ?"
        val result2 = db.rawQuery(query, arrayOf(title,name))
        if (result2.moveToFirst())
            exists2 = true
        result2.close()
        db.close()

        val values = ContentValues()
        val values2 = ContentValues()
        values.put(ARTIST_NAME, name)
        values2.put(GAME_ARTIST_TITLE, title)
        values2.put(GAME_ARTIST_NAME, name)
        val db3 = this.writableDatabase
        if(!exists)
            db3.insert(TABLE_ARTISTS, null, values)
        if(!exists2)
            db3.insert(TABLE_GAME_ARTISTS, null, values2)
        db3.close()
    }

    fun updateArtists(artists: String, title: String){
        var db = this.writableDatabase
        db.delete(TABLE_GAME_ARTISTS, "$GAME_ARTIST_TITLE = ?", arrayOf(title))
        for(artist in artists.split("\n"))
            addArtist(title,artist)
        db.close()
    }

    fun addDesigner(title: String, name: String){
        var exists = false
        val db = this.readableDatabase
        var query = "SELECT * FROM $TABLE_DESIGNERS WHERE $DESIGNER_NAME = ?"
        val result = db.rawQuery(query, arrayOf(name))
        if (result.moveToFirst())
            exists = true
        result.close()

        var exists2 = false
        query = "SELECT * FROM $TABLE_GAME_DESIGNERS WHERE $GAME_DESIGNER_TITLE = ? AND $GAME_DESIGNER_NAME = ?"
        val result2 = db.rawQuery(query, arrayOf(title,name))
        if (result2.moveToFirst())
            exists2 = true
        result2.close()
        db.close()

        val values = ContentValues()
        val values2 = ContentValues()
        values.put(DESIGNER_NAME, name)
        values2.put(GAME_DESIGNER_TITLE, title)
        values2.put(GAME_DESIGNER_NAME, name)
        val db3 = this.writableDatabase
        if(!exists)
            db3.insert(TABLE_DESIGNERS, null, values)
        if(!exists2)
            db3.insert(TABLE_GAME_DESIGNERS, null, values2)
        db3.close()
    }

    fun updateDesigners(designers: String, title: String){
        var db = this.writableDatabase
        db.delete(TABLE_GAME_DESIGNERS, "$GAME_DESIGNER_TITLE = ?", arrayOf(title))
        for(artist in designers.split("\n"))
            addDesigner(title,artist)
        db.close()
    }

    fun findDesigners(title: String): String{
        var designers: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAME_DESIGNERS WHERE $GAME_DESIGNER_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            do {
                designers.add(result.getString(result.getColumnIndex(GAME_DESIGNER_NAME)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return designers.joinToString(separator = "\n")
    }

    fun findArtists(title: String): String{
        var artists: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_GAME_ARTISTS WHERE $GAME_ARTIST_TITLE = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            do {
                artists.add(result.getString(result.getColumnIndex(GAME_ARTIST_NAME)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return artists.joinToString(separator = "\n")
    }

    fun addExpansion(title: String, parent: String){
        val values = ContentValues()
        values.put(EXPANSION_TITLE, title)
        values.put(EXPANSION_PARENT, parent)
        val db = this.writableDatabase
        db.insert(TABLE_EXPANSIONS, null, values)
        db.close()
    }

    fun updateExpansions(expansions: String, parent: String){
        var db = this.writableDatabase
        db.delete(TABLE_EXPANSIONS, "$EXPANSION_PARENT = ?", arrayOf(parent))
        for(expansion in expansions.split("\n"))
            addExpansion(expansion,parent)
        db.close()
    }

    fun findExpansions(title: String): String{
        var expansions: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_EXPANSIONS WHERE $EXPANSION_PARENT = ?"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            do {
                expansions.add(result.getString(result.getColumnIndex(EXPANSION_TITLE)))
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return expansions.joinToString(separator = "\n")
    }

    fun addRank(rank: Int, title: String){
        val values = ContentValues()
        values.put(RANKING_DATE, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE).toString())
        values.put(RANKING_RANK, rank)
        values.put(RANKING_TITLE, title)
        val db = this.writableDatabase
        db.insert(TABLE_RANKING, null, values)
        db.close()
    }

    fun findRanking(title: String): String{
        var ranking: MutableList<String> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RANKING WHERE $RANKING_TITLE = ? ORDER BY $RANKING_DATE DESC"
        val result = db.rawQuery(query, arrayOf(title))
        if (result.moveToFirst()){
            do {
                var rank = ""
                if(result.getInt(result.getColumnIndex(RANKING_RANK)) == -1)
                    rank = "Not Ranked"
                else
                    rank = result.getInt(result.getColumnIndex(RANKING_RANK)).toString()
                val line = result.getString(result.getColumnIndex(RANKING_DATE)) +
                        ": " + rank
                ranking.add(line)
            } while(result.moveToNext())
            result.close()
        }
        db.close()
        return ranking.joinToString(separator = "\n")
    }
}