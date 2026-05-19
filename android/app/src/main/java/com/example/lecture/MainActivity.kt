package com.example.lecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lecture.ui.navigation.AppNavGraph
import com.example.lecture.ui.theme.LectureTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LectureTheme {
                AppNavGraph()
            }
        }
    }

}