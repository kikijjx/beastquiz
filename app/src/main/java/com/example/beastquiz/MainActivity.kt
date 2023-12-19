package com.example.beastquiz

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.beastquiz.ui.theme.BeastquizTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


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
{
    fun getFeature(index: Int): String {
        if (index == 1) return feature1
        else if (index == 2) return feature2
        else if (index == 3) return feature3
        else if (index == 4) return feature4
        else if (index == 5) return feature5
        else if (index == 6) return feature6
        else if (index == 7) return feature7
        else return feature8
    }
}
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var beasts by mutableStateOf<List<Beast>>(emptyList())

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            val fetchedBeasts = fetchData()
            withContext(Dispatchers.Main) {
                beasts = fetchedBeasts
            }
        }

        setContent {
            BeastquizTheme {
                val navController = rememberNavController()
                val items = listOf(
                    BottomNavigationItem(
                        title = "Животные",
                        selectedIcon = Icons.Filled.List,
                        unselectedIcon = Icons.Outlined.List,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        title = "Игра",
                        selectedIcon = Icons.Filled.PlayArrow,
                        unselectedIcon = Icons.Outlined.PlayArrow,
                        hasNews = false,
                    ),
                )
                var SelectedItemIndex by rememberSaveable {
                    mutableStateOf(0)
                }
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = SelectedItemIndex == index,
                                        onClick = {
                                                  SelectedItemIndex = index
                                            navController.navigate(item.title)
                                        },
                                        label = {
                                            Text(text = item.title)
                                        },
                                        icon = {
                                            BadgedBox(badge = {}) {

                                            }
                                            Icon(
                                                imageVector = if (index == SelectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        }
                                    )

                                }
                            }

                        }
                    )
                    {
                        innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NavHost(navController = navController, startDestination = "Животные") {
                                composable("Животные") {
                                    BeastList(beasts = beasts)
                                }
                                composable("Игра") {
                                    Game(beasts = beasts)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchData(): List<Beast> {
        val client = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
            .build()

        val request = Request.Builder()
            .url("http://188.166.4.134:5000/get_data")
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
                Text(
                    text = "Животные",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            items(beasts) { beast ->
                BeastItem(beast)
            }
        }

}

fun Game(beasts: List<Beast>) {

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
            style = TextStyle(
                fontSize = 25.sp,
                fontWeight = if (beast.name == "Животные") FontWeight.Bold else FontWeight.Normal
            )
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
