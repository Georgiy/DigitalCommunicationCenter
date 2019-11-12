package com.android.loggercharttable

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_first_page.*

class FirstPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_page)
        first_button.setOnClickListener {
            if (!textView.text.isNullOrEmpty()&&Integer.parseInt(textView.text.toString())<=200&&Integer.parseInt(textView.text.toString())>=1) {
                val intent = Intent(this, LineChartActivity1::class.java)
                // To pass any data to next activity
                intent.putExtra("points_number", textView.text.toString())
                // start your next activity
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext,"Введите число точек от 1 до 200",Toast.LENGTH_LONG).show()
            }

        }
    }
}
