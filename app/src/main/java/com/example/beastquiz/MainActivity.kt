package com.example.beastquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beastquiz.ui.theme.BeastquizTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class Beast(
    val feature1: String,
    val feature2: String,
    val feature3: String,
    val feature4: String,
    val feature5: String,
    val feature6: String,
    val feature7: String,
    val feature8: String,
    val name: String
)

class MainActivity : ComponentActivity() {
    private var beasts by mutableStateOf<List<Beast>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            val fetchedBeasts = fetchData()
            // Update the state on the main thread
            withContext(Dispatchers.Main) {
                beasts = fetchedBeasts
            }
        }

        setContent {
            BeastquizTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BeastList(beasts = beasts)
                }
            }
        }
    }

    private fun fetchData(): List<Beast> {
        val client = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS)) // Разрешить незашифрованный и современный TLS трафик
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.101:5000/get_data")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        val beastList = mutableListOf<Beast>()

        responseBody?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonBeast = jsonArray.getJSONObject(i)
                val beast = Beast(
                    jsonBeast.getString("feature1"),
                    jsonBeast.getString("feature2"),
                    jsonBeast.getString("feature3"),
                    jsonBeast.getString("feature4"),
                    jsonBeast.getString("feature5"),
                    jsonBeast.getString("feature6"),
                    jsonBeast.getString("feature7"),
                    jsonBeast.getString("feature8"),
                    jsonBeast.getString("name")
                )
                beastList.add(beast)
            }
        }

        return beastList
    }
}

@Composable
fun BeastList(beasts: List<Beast>) {
        LazyColumn(
        ) {
            item {
                BeastItemHeader()
            }
            items(beasts) { beast ->
                BeastItem(beast)
            }
        }

}
@Composable
fun BeastItemHeader() {
    BeastItem(Beast("Признак 1", "Признак 2", "Признак 3",
        "Признак 4", "Признак 5", "Признак 6",
        "Признак 7", "Признак 8", "Название"))
}
@Composable
fun BeastItem(beast: Beast) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ClickableText(
            text = AnnotatedString(beast.name),
            onClick = {
                expanded = !expanded
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontSize = 25.sp)

        )

        if (expanded) {
            Text(
                text = "Признак 1: ${beast.feature1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 2: ${beast.feature2}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 3: ${beast.feature3}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 4: ${beast.feature4}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 5: ${beast.feature5}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 6: ${beast.feature6}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 7: ${beast.feature7}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Признак 8: ${beast.feature8}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
