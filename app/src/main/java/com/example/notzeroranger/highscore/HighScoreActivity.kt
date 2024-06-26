package com.example.notzeroranger.highscore

import com.example.notzeroranger.R
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notzeroranger.MainActivity
import com.example.notzeroranger.SettingsActivity
import com.example.notzeroranger.database.DemoDbHeper
import com.example.notzeroranger.service.GameSound
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class HighScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.highscore_board_layout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // move to settings
        val returnButton = findViewById<Button>(R.id.returnButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        var highScoreList = ArrayList<HighScore>()
        val dbHelper = DemoDbHeper(this)
        val db = dbHelper.readableDatabase
        val projection = arrayOf("name", "score")
        var cursor:Cursor = db.query(com.example.notzeroranger.database.HighScore.PlayerEntry.TABLE_NAME,
            projection,null,null,null,null,"score")
        try {
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex("name")
                val scoreIndex = cursor.getColumnIndex("score")
                do {
                    val name = cursor.getString(nameIndex)
                    val score = cursor.getString(scoreIndex)
                    highScoreList.add(HighScore(name, score.toLong()))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {}
        finally {
            cursor.close()
            db.close()
        }
        // filler data


        //sort the list from top to low
        highScoreList.sortByDescending { it.score }
        try {
            val fos: FileOutputStream = openFileOutput("highScoreList", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(highScoreList)
            oos.close()
            fos.close()
            val fis: FileInputStream = openFileInput("highScoreList")
            //since an object is being read,not just string, use Object input
            val ois = ObjectInputStream(fis)
            @Suppress("UNCHECKED_CAST")
            highScoreList = ois.readObject() as ArrayList<HighScore>
            ois.close()
            fis.close()
        } catch (e: IOException) {
            println(e.message)
        } catch (e: ClassNotFoundException) {
            println(e.message)
        }

        val customAdapter = HighScoreAdapter(highScoreList)
        //initiate recycler view
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = customAdapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
    }
}