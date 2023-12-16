package com.example.beastquiz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties
val TAG = "MyApp"

data class Animal(val name: String, val feature1: String, val feature2: String,
             val feature3: String, val feature4: String, val feature5: String,
             val feature6: String, val feature7: String, val feature8: String)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun dbView(): String {
        var string1 = ""
        val url = "jdbc:postgresql://ella.db.elephantsql.com:5432/uqamzagw"
        val props = Properties()
        props.setProperty("user", "uqamzagw")
        props.setProperty("password", "HTHZUHQ9Mqlq9FRPax-p4ZivJ6JLV16y")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection: Connection =
                    DriverManager.getConnection(url, "uqamzagw", "HTHZUHQ9Mqlq9FRPax-p4ZivJ6JLV16y")
                Log.d(TAG, connection.isValid(0).toString())
                val sql = "SELECT * FROM animals"
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(sql)

                while (resultSet.next()) {
                    val name = resultSet.getString("name")
                    val feature1 = resultSet.getString("feature1")
                    val feature2 = resultSet.getString("feature2")
                    val feature3 = resultSet.getString("feature3")
                    val feature4 = resultSet.getString("feature4")
                    val feature5 = resultSet.getString("feature5")
                    val feature6 = resultSet.getString("feature6")
                    val feature7 = resultSet.getString("feature7")
                    val feature8 = resultSet.getString("feature8")
                    string1 = "Name: $name, Feature1: $feature1, Feature2: $feature2, " +
                            "Feature3: $feature3, Feature4: $feature4, " +
                            "Feature5: $feature5, Feature6: $feature6, " +
                            "Feature7: $feature7, Feature8: $feature8"
                }
                resultSet.close()
                statement.close()
                connection.close()

            } catch (e: SQLException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        return string1
    }

    fun button1_click(view: View) {
        val text1: TextView = findViewById(R.id.text1)
        text1.text = dbView()
    }
}