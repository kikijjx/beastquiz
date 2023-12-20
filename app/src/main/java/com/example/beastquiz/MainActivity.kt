package com.example.beastquiz

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import java.io.Serializable
import java.net.ProtocolException
import java.net.SocketTimeoutException


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
    public var features = listOf(feature1, feature2, feature3, feature4, feature5, feature6, feature7, feature8)
    fun getFeature(index: Int): String {
        return features.getOrNull(index - 1) ?: ""
    }
}
class BeastViewModel : ViewModel() {
    var beasts: MutableState<List<Beast>> = mutableStateOf(emptyList())
    var loading: MutableState<Boolean> = mutableStateOf(true)
    var serverDown: MutableState<Boolean> = mutableStateOf(false)

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
    private val viewModel: BeastViewModel by viewModels()

    //private var beasts by mutableStateOf<List<Beast>>(emptyList())
    //private var loading by mutableStateOf(true)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("beasts", ArrayList(viewModel.beasts.value) as Serializable)
        outState.putBoolean("loading", viewModel.loading.value)
        outState.putBoolean("serverDown", viewModel.serverDown.value)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.beasts.value = savedInstanceState.getSerializable("beasts") as? List<Beast> ?: emptyList()
        viewModel.loading.value = savedInstanceState.getBoolean("loading", true)
        viewModel.serverDown.value = savedInstanceState.getBoolean("serverDown", false)
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val fetchedBeasts = fetchData()
                withContext(Dispatchers.Main) {
                    viewModel.beasts.value = fetchedBeasts
                    viewModel.loading.value = false
                    viewModel.serverDown.value = false
                }
            }
            catch (exception: SocketTimeoutException) {
                viewModel.serverDown.value = true
            }
            catch (exception: ProtocolException) {
                viewModel.serverDown.value = true
            }
        }

        setContent {
            BeastquizTheme {
                val navController = rememberNavController()

                if (viewModel.loading.value && !viewModel.loading.value) {
                    SplashScreen(false)
                } else if (viewModel.serverDown.value) {
                    SplashScreen(true)
                } else {
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
                        { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = "Животные"
                                ) {
                                    composable("Животные") {
                                        BeastList(beasts = viewModel.beasts.value)
                                    }
                                    composable("Игра") {
                                        Game(beasts = viewModel.beasts.value)
                                    }
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
fun SplashScreen(serverDown: Boolean) {
    var rotationState by remember { mutableStateOf(0f) }
    val rotationAnimation = rememberInfiniteTransition()

    val rotation by rotationAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    LaunchedEffect(Unit) {
        rotationState = rotation
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val painter: Painter = painterResource(id = R.drawable.ic_launcher_monochrome)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .rotate(rotationState)
            )
            if (serverDown) {
                Text(text = stringResource(R.string.server_down), style = MaterialTheme.typography.headlineLarge)
                Text(text = stringResource(R.string.reconnect), style = MaterialTheme.typography.bodyLarge)
            }
            else {
                Text(text = stringResource(R.string.loading), style = MaterialTheme.typography.headlineLarge)
            }
        }
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

@Composable
fun Game(beasts: List<Beast>) {
    var confirmedFeatures by remember { mutableStateOf(emptyList<String>()) }
    var i by remember { mutableStateOf(0) }
    var animalsData by remember { mutableStateOf(beasts) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (confirmedFeatures.size < 3) {
            GuessFeatureSection(
                beast = animalsData.random(),
                onFeatureSelected = { feature ->
                    confirmedFeatures = confirmedFeatures + feature
                    i++
                },
                onFeatureRejected = { feature ->
                    animalsData = animalsData.filter { beast ->
                        feature !in beast.features
                    }
                },

            )
        }
        else {
            // Вызывайте MainGameSection, когда найдено 3 признака
            MainGameSection(
                beasts = beasts,
                confirmedFeatures = confirmedFeatures,
                onGameEnd = {
                    // Логика окончания игры
                    // Можете сбросить игру, если это необходимо
                    confirmedFeatures = emptyList()
                    animalsData = beasts
                }
            )
        }
    }
}

@Composable
fun GuessFeatureSection(
    beast: Beast,
    onFeatureSelected: (String) -> Unit,
    onFeatureRejected: (String) -> Unit,

) {
    var currentFeatureIndex by remember { mutableStateOf(0) }

    Column {
        Text(
            text = "Подходит ли признак '${beast.getFeature(currentFeatureIndex + 1)}'?",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = {
                    onFeatureSelected(beast.getFeature(currentFeatureIndex + 1))
                    currentFeatureIndex++
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text("Да")
            }

            Button(
                onClick = {
                    onFeatureRejected(beast.getFeature(currentFeatureIndex + 1))
                    currentFeatureIndex++
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text("Нет")
            }
        }



    }
}
@Composable
fun MainGameSection(
    beasts: List<Beast>,
    onGameEnd: () -> Unit,
    confirmedFeatures: List<String>
) {
    var confirmedFeatures1 by remember { mutableStateOf(confirmedFeatures) }
    var animalsData by remember { mutableStateOf(beasts) }
    var gameInProgress by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }

    if (gameInProgress && animalsData.isNotEmpty() && confirmedFeatures1.size < 8) {
        val correctBeast = animalsData.random()


        var flag = true;
        if (currentQuestionIndex < correctBeast.features.size) {
        Column {
            Text(
                text = "Это животное - ${correctBeast.name}? (Да/Нет)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(
                    onClick = {
                        onGameEnd()
                        gameInProgress = false
                        flag = false;
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Да")
                }

                Button(
                    onClick = {
                        animalsData = animalsData.filter { it != correctBeast }
                        currentQuestionIndex++
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Нет")
                }
            }

            if (flag == false){
            Text(
                text = "Подходит ли признак '${correctBeast.features[currentQuestionIndex]}'?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(
                    onClick = {
                        confirmedFeatures1 = confirmedFeatures1.toMutableList().apply {
                            add(correctBeast.features[currentQuestionIndex])
                        }
                        currentQuestionIndex++
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Да")
                }

                Button(
                    onClick = {
                        currentQuestionIndex++
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Нет")
                }
            }
              }
            }
        }
    }
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
                fontWeight = FontWeight.Normal
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
