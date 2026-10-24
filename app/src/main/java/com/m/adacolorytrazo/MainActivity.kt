package com.m.adacolorytrazo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.m.adacolorytrazo.audio.BackgroundMusic
import com.m.adacolorytrazo.ui.AnimalsFlow
import com.m.adacolorytrazo.ui.ApprovedPrototypeScreen
import com.m.adacolorytrazo.ui.BodyAssembleScreen
import com.m.adacolorytrazo.ui.ColorsFlow
import com.m.adacolorytrazo.ui.FreeDrawScreen
import com.m.adacolorytrazo.ui.FruitsFlow
import com.m.adacolorytrazo.ui.MemoriceScreen
import com.m.adacolorytrazo.ui.PrototypeHotspot
import com.m.adacolorytrazo.ui.RecognizeAnimalsScreen
import com.m.adacolorytrazo.ui.RecognizeProduceScreen
import com.m.adacolorytrazo.ui.RecognizeTransportScreen
import com.m.adacolorytrazo.ui.SplashCute
import com.m.adacolorytrazo.ui.TransportFlow
import com.m.adacolorytrazo.ui.approvedImageForScreen

private enum class Screen {
    Splash,
    ApprovedMain, ApprovedCreate, ApprovedDiscover, ApprovedAda,
    ApprovedBody, ApprovedMemorice, ApprovedFreeDraw,
    Animals, Fruits, Transport, Colors, FreeDraw,
    RecognizeAnimals, RecognizeProduce, RecognizeTransport,
    BodyAssemble, Memorice, WeatherGame
}

private val Screen.isApprovedReference: Boolean
    get() = when (this) {
        Screen.ApprovedMain, Screen.ApprovedCreate, Screen.ApprovedDiscover,
        Screen.ApprovedAda, Screen.ApprovedBody, Screen.ApprovedMemorice,
        Screen.ApprovedFreeDraw -> true
        else -> false
    }

private fun hotspot(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    label: String,
    onClick: () -> Unit
) = PrototypeHotspot(left, top, right, bottom, label, onClick)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                var screen by rememberSaveable { mutableStateOf(Screen.Splash) }
                var returnScreen by rememberSaveable { mutableStateOf(Screen.ApprovedMain) }
                val stateHolder = rememberSaveableStateHolder()
                val context = LocalContext.current.applicationContext
                val lifecycleOwner = LocalLifecycleOwner.current
                val isSplash = screen == Screen.Splash

                val openFromReference: (Screen) -> Unit = { destination ->
                    returnScreen = screen
                    screen = destination
                }
                val backToPrevious = { screen = returnScreen }
                val backToMain = { screen = Screen.ApprovedMain }

                BackHandler(enabled = screen != Screen.ApprovedMain && screen != Screen.Splash) {
                    screen = when (screen) {
                        Screen.ApprovedCreate, Screen.ApprovedDiscover, Screen.ApprovedAda,
                        Screen.ApprovedFreeDraw -> Screen.ApprovedMain
                        Screen.ApprovedBody, Screen.ApprovedMemorice -> Screen.ApprovedAda
                        else -> returnScreen
                    }
                }

                DisposableEffect(lifecycleOwner, context, isSplash) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> {
                                if (isSplash) BackgroundMusic.stop() else BackgroundMusic.start(context)
                            }
                            Lifecycle.Event.ON_STOP -> BackgroundMusic.stop()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    if (isSplash) {
                        BackgroundMusic.stop()
                    } else if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        BackgroundMusic.start(context)
                    }
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        BackgroundMusic.stop()
                    }
                }

                if (isSplash) {
                    SplashCute(onFinish = backToMain)
                } else {
                    Scaffold(
                        contentWindowInsets = if (screen.isApprovedReference || screen == Screen.BodyAssemble) {
                            WindowInsets(0, 0, 0, 0)
                        } else {
                            WindowInsets.safeDrawing
                        }
                    ) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) {
                            val stateKey = if (screen == Screen.BodyAssemble) {
                                "${screen.name}_demo_parts_v2"
                            } else {
                                screen.name
                            }
                            stateHolder.SaveableStateProvider(stateKey) {
                                when (screen) {
                                    Screen.Splash -> Unit
                                    Screen.ApprovedMain -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("main"),
                                        screenDescription = "Menú principal aprobado de Ada, Color y Trazo",
                                        hotspots = listOf(
                                            hotspot(0.02f, 0.23f, 0.39f, 0.41f, "Reconocer animales") {
                                                openFromReference(Screen.RecognizeAnimals)
                                            },
                                            hotspot(0.64f, 0.22f, 0.98f, 0.42f, "Abrir Crear") {
                                                screen = Screen.ApprovedCreate
                                            },
                                            hotspot(0.00f, 0.43f, 0.38f, 0.66f, "Reconocer transportes") {
                                                openFromReference(Screen.RecognizeTransport)
                                            },
                                            hotspot(0.65f, 0.43f, 1.00f, 0.66f, "Abrir Dibujo libre") {
                                                screen = Screen.ApprovedFreeDraw
                                            },
                                            hotspot(0.01f, 0.67f, 0.40f, 0.90f, "Abrir Jugar con Ada") {
                                                screen = Screen.ApprovedAda
                                            },
                                            hotspot(0.62f, 0.65f, 1.00f, 0.90f, "Vestir a Ada según el clima") {
                                                openFromReference(Screen.WeatherGame)
                                            }
                                        )
                                    )
                                    Screen.ApprovedCreate -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("create"),
                                        screenDescription = "Pantalla aprobada Crear",
                                        hotspots = listOf(
                                            hotspot(0.02f, 0.01f, 0.21f, 0.13f, "Volver al menú principal", backToMain),
                                            hotspot(0.05f, 0.14f, 0.50f, 0.42f, "Colorear animales") {
                                                openFromReference(Screen.Animals)
                                            },
                                            hotspot(0.50f, 0.14f, 0.96f, 0.42f, "Colorear frutas") {
                                                openFromReference(Screen.Fruits)
                                            },
                                            hotspot(0.05f, 0.42f, 0.50f, 0.70f, "Colorear transportes") {
                                                openFromReference(Screen.Transport)
                                            },
                                            hotspot(0.50f, 0.42f, 0.96f, 0.70f, "Abrir Dibujo libre") {
                                                screen = Screen.ApprovedFreeDraw
                                            },
                                            hotspot(0.24f, 0.70f, 0.77f, 0.99f, "Aprender los colores") {
                                                openFromReference(Screen.Colors)
                                            }
                                        )
                                    )
                                    Screen.ApprovedDiscover -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("discover"),
                                        screenDescription = "Pantalla aprobada Descubrir",
                                        hotspots = listOf(
                                            hotspot(0.02f, 0.02f, 0.21f, 0.13f, "Volver al menú principal", backToMain),
                                            hotspot(0.02f, 0.20f, 0.50f, 0.53f, "Reconocer animales") {
                                                openFromReference(Screen.RecognizeAnimals)
                                            },
                                            hotspot(0.50f, 0.20f, 0.98f, 0.53f, "Reconocer frutas y verduras") {
                                                openFromReference(Screen.RecognizeProduce)
                                            },
                                            hotspot(0.21f, 0.53f, 0.79f, 0.90f, "Reconocer transportes") {
                                                openFromReference(Screen.RecognizeTransport)
                                            }
                                        )
                                    )
                                    Screen.ApprovedAda -> ApprovedPrototypeScreen(
                                        imageRes = R.drawable.approved_04_play_ada_no_weather,
                                        screenDescription = "Pantalla aprobada Jugar con Ada",
                                        hotspots = listOf(
                                            hotspot(0.00f, 0.04f, 0.20f, 0.17f, "Volver al menú principal", backToMain),
                                            hotspot(0.02f, 0.19f, 0.51f, 0.53f, "Armar el cuerpo") {
                                                screen = Screen.ApprovedBody
                                            },
                                            hotspot(0.49f, 0.19f, 0.98f, 0.53f, "Jugar Memorice") {
                                                screen = Screen.ApprovedMemorice
                                            }
                                        )
                                    )
                                    Screen.ApprovedBody -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("body"),
                                        screenDescription = "Maqueta aprobada Armar el cuerpo. Toca la silueta o la bandeja para jugar.",
                                        hotspots = listOf(
                                            hotspot(0.01f, 0.01f, 0.21f, 0.13f, "Volver a Jugar con Ada") {
                                                screen = Screen.ApprovedAda
                                            },
                                            hotspot(0.10f, 0.13f, 0.91f, 0.96f, "Iniciar el juego interactivo Armar el cuerpo") {
                                                openFromReference(Screen.BodyAssemble)
                                            }
                                        )
                                    )
                                    Screen.ApprovedMemorice -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("memorice"),
                                        screenDescription = "Maqueta aprobada Memorice. Toca el tablero para iniciar una partida.",
                                        hotspots = listOf(
                                            hotspot(0.01f, 0.01f, 0.19f, 0.13f, "Volver a Jugar con Ada") {
                                                screen = Screen.ApprovedAda
                                            },
                                            hotspot(0.08f, 0.12f, 0.93f, 0.91f, "Iniciar el juego interactivo Memorice") {
                                                openFromReference(Screen.Memorice)
                                            }
                                        )
                                    )
                                    Screen.ApprovedFreeDraw -> ApprovedPrototypeScreen(
                                        imageRes = approvedImageForScreen("free_draw"),
                                        screenDescription = "Maqueta aprobada Dibujo libre. Toca el lienzo para dibujar.",
                                        hotspots = listOf(
                                            hotspot(0.01f, 0.01f, 0.21f, 0.13f, "Volver a Crear") {
                                                screen = Screen.ApprovedCreate
                                            },
                                            hotspot(0.12f, 0.16f, 0.96f, 0.75f, "Abrir el lienzo interactivo de Dibujo libre") {
                                                openFromReference(Screen.FreeDraw)
                                            }
                                        )
                                    )
                                    Screen.Animals -> AnimalsFlow(onBack = backToPrevious)
                                    Screen.Fruits -> FruitsFlow(onBack = backToPrevious)
                                    Screen.Transport -> TransportFlow(onBack = backToPrevious)
                                    Screen.Colors -> ColorsFlow(onBack = backToPrevious)
                                    Screen.FreeDraw -> FreeDrawScreen(onBack = backToPrevious)
                                    Screen.RecognizeAnimals -> RecognizeAnimalsScreen(onBack = backToPrevious)
                                    Screen.RecognizeProduce -> RecognizeProduceScreen(onBack = backToPrevious)
                                    Screen.RecognizeTransport -> RecognizeTransportScreen(onBack = backToPrevious)
                                    Screen.BodyAssemble -> BodyAssembleScreen(onBack = backToPrevious)
                                    Screen.WeatherGame -> GameScreen(onBack = backToPrevious)
                                    Screen.Memorice -> MemoriceScreen(onBack = backToPrevious)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
