package cl.oursour.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.oursour.app.ui.navigation.AppNavHost
import cl.oursour.app.ui.theme.OurSourTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as OurSourApp).container
        setContent {
            OurSourTheme {
                AppNavHost(container)
            }
        }
    }
}
