package com.shinbash.tpk.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.shinbash.tpk.R

class InputWebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_web)

        findViewById<Button>(R.id.go).setOnClickListener {
            startActivity(AppFullScreenWebViewActivity.newIntent(this, findViewById<EditText>(R.id.webEt).text.toString().trim()))
        }
    }
}