package com.relationshipradar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.relationshipradar.app.ui.RadarNavHost
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.theme.RadarTheme

class MainActivity : ComponentActivity() {
    private val vm: RadarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val openPerson = intent?.getLongExtra("personId", -1L)?.takeIf { it > 0 }
        val openLog = intent?.getBooleanExtra("openLog", false) == true
        setContent {
            RadarTheme { RadarNavHost(vm, openPerson, openLog) }
        }
    }
}
