package com.noteremind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.noteremind.app.data.AppRepo
import com.noteremind.app.ui.AppRoot
import com.noteremind.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRepo.init(this)
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }
}
