package com.example.android_app

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    private var currentInput = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViews()
    }

    private fun setupViews() {
        resultText = findViewById(R.id.textView2)
        findViewById<Button>(R.id.button26).setOnClickListener { appendNumber("0") }
        findViewById<Button>(R.id.button21).setOnClickListener { appendNumber("1") }
        findViewById<Button>(R.id.button22).setOnClickListener { appendNumber("2") }
        findViewById<Button>(R.id.button25).setOnClickListener { appendNumber("3") }
        findViewById<Button>(R.id.button19).setOnClickListener { appendNumber("4") }
        findViewById<Button>(R.id.button20).setOnClickListener { appendNumber("5") }
        findViewById<Button>(R.id.button24).setOnClickListener { appendNumber("6") }
        findViewById<Button>(R.id.button1).setOnClickListener { appendNumber("7") }
        findViewById<Button>(R.id.button18).setOnClickListener { appendNumber("8") }
        findViewById<Button>(R.id.button23).setOnClickListener { appendNumber("9") }
        findViewById<Button>(R.id.button30).setOnClickListener { setOperator("+") }
        findViewById<Button>(R.id.button29).setOnClickListener { setOperator("-") }
        findViewById<Button>(R.id.button28).setOnClickListener { setOperator("*") }
        findViewById<Button>(R.id.button35).setOnClickListener { setOperator("/") }
        findViewById<Button>(R.id.button31).setOnClickListener {
            calculate()
            changeButtonColors()
        }
        findViewById<Button>(R.id.button32).setOnClickListener { clear() }
        findViewById<Button>(R.id.button27).setOnClickListener { addDecimal() }
        findViewById<Button>(R.id.button34).setOnClickListener { calculatePercentage() }
        findViewById<Button>(R.id.button33).setOnClickListener { toggleSign() }

        updateDisplay()
    }

    private fun changeButtonColors() {
        val buttons = listOf(
            R.id.button26, R.id.button21, R.id.button22, R.id.button25,
            R.id.button19, R.id.button20, R.id.button24, R.id.button1,
            R.id.button18, R.id.button23, R.id.button30, R.id.button29,
            R.id.button28, R.id.button35, R.id.button32, R.id.button27,
            R.id.button34, R.id.button33
        )
        buttons.forEach { buttonId ->
            val color = Color.rgb(
                Random.nextInt(256),
                Random.nextInt(256),
                Random.nextInt(256)
            )
            findViewById<Button>(buttonId).setBackgroundColor(color)
        }
    }

    private fun appendNumber(number: String) {
        if (currentInput == "0" || currentInput == "Error") {
            currentInput = number
        } else {
            currentInput += number
        }
        updateDisplay()
    }

    private fun setOperator(operator: String) {
        if (currentInput == "Error" || currentInput.isEmpty()) return

        val lastChar = currentInput.last()
        if (lastChar.isDigit() || lastChar == ',') {
            if (hasOperator()) {
                calculate()
            }
            currentInput += operator
            updateDisplay()
        } else if (lastChar in setOf('+', '-', '*', '/')) {
            currentInput = currentInput.dropLast(1) + operator
            updateDisplay()
        }
    }

    private fun hasOperator(): Boolean {
        return currentInput.contains('+') || currentInput.contains('-') ||
                currentInput.contains('*') || currentInput.contains('/')
    }

    private fun calculate() {
        if (currentInput == "Error") return

        try {
            val expression = currentInput.replace(",", ".")
            val operator = when {
                expression.drop(1).contains("+") -> "+"
                expression.drop(1).contains("-") -> "-"
                expression.drop(1).contains("*") -> "*"
                expression.drop(1).contains("/") -> "/"
                else -> ""
            }

            if (operator.isNotEmpty()) {
                val parts = expression.split(operator)
                if (parts.size >= 2) {
                    val firstPart = parts.dropLast(1).joinToString(operator)
                    val secondPart = parts.last()
                    if (firstPart.isNotEmpty() && secondPart.isNotEmpty()){
                        val first = firstPart.toDouble()
                        val second = secondPart.toDouble()

                        val result = when (operator) {
                            "+" -> first + second
                            "-" -> first - second
                            "*" -> first * second
                            "/" -> if (second != 0.0) first / second else Double.NaN
                            else -> Double.NaN
                        }
                        currentInput = if (result.isNaN()) "Error" else formatResult(result)
                    }
                }
            }
        } catch (e: Exception) {
            currentInput = "Error"
        }
        updateDisplay()
    }

    private fun formatResult(result: Double): String {
        return if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString().replace(".", ",")
        }
    }

    private fun clear() {
        currentInput = "0"
        updateDisplay()
    }

    private fun addDecimal() {
        if (currentInput == "Error") {
            currentInput = "0,"
        } else {
            val lastNumber = currentInput.split('+', '-', '*', '/').last()
            if (!lastNumber.contains(",")) {
                currentInput += ","
            }
        }
        updateDisplay()
    }

    private fun toggleSign() {
        if (currentInput == "0" || currentInput == "Error") return

        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1)
        } else {
            currentInput = "-$currentInput"
        }
        updateDisplay()
    }

    private fun calculatePercentage() {
        if (currentInput == "Error") return

        val number = currentInput.replace(",", ".").toDoubleOrNull()
        if (number != null) {
            val result = number / 100.0
            currentInput = formatResult(result)
        } else {
            currentInput = "Error"
        }
        updateDisplay()
    }

    private fun updateDisplay() {
        resultText.text = currentInput
    }
}