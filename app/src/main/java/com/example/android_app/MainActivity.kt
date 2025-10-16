package com.example.android_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    private var currentInput = "0"
    private var currentOperator = ""
    private var firstOperand = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViews()
    }
    private fun setupViews(){
        resultText = findViewById(R.id.textView2)
        findViewById<Button>(R.id.button26).setOnClickListener { appendNumber("0") }
        findViewById<Button>(R.id.button21).setOnClickListener { appendNumber("1") }
        findViewById<Button>(R.id.button22).setOnClickListener { appendNumber("2") }
        findViewById<Button>(R.id.button25).setOnClickListener { appendNumber("3") }
        findViewById<Button>(R.id.button19).setOnClickListener { appendNumber("4") }
        findViewById<Button>(R.id.button20).setOnClickListener { appendNumber("5") }
        findViewById<Button>(R.id.button24).setOnClickListener { appendNumber("6") }
        findViewById<Button>(R.id.button1).setOnClickListener  { appendNumber("7") }
        findViewById<Button>(R.id.button18).setOnClickListener { appendNumber("8") }
        findViewById<Button>(R.id.button23).setOnClickListener { appendNumber("9") }
        findViewById<Button>(R.id.button30).setOnClickListener { setOperator("+") }
        findViewById<Button>(R.id.button29).setOnClickListener { setOperator("-") }
        findViewById<Button>(R.id.button28).setOnClickListener { setOperator("*") }
        findViewById<Button>(R.id.button35).setOnClickListener { setOperator("/") }
        findViewById<Button>(R.id.button31).setOnClickListener { calculate() }
        findViewById<Button>(R.id.button32).setOnClickListener { clear() }
        findViewById<Button>(R.id.button27).setOnClickListener { addDecimal() }
        findViewById<Button>(R.id.button34).setOnClickListener { calculatePercentage() }
        findViewById<Button>(R.id.button33).setOnClickListener { toggleSign() }

        updateDisplay()
    }

    private fun appendNumber(number: String) {
        if (currentInput == "0") {
            currentInput = number
        } else {
            currentInput += number
        }
        updateDisplay()
    }
    private fun setOperator(operator: String) {
        if (currentOperator.isEmpty()) {
            firstOperand = currentInput.replace(",", ".").toDouble()
            currentInput = "0"
        } else {
            calculate()
        }
        currentOperator = operator
    }
    private fun calculate() {
        if (currentOperator.isNotEmpty()) {
            val secondOperand = currentInput.replace(",", ".").toDouble()
            val result = when (currentOperator) {
                "+" -> firstOperand + secondOperand
                "-" -> firstOperand - secondOperand
                "*" -> firstOperand * secondOperand
                "/" -> if (secondOperand != 0.0) firstOperand / secondOperand else Double.NaN
                else -> 0.0
            }

            currentInput = if (result.isNaN()) "Error" else {
                if (result % 1 == 0.0) {
                    result.toInt().toString()
                } else {
                    result.toString().replace(".", ",")
                }
            }
            currentOperator = ""
            firstOperand = 0.0
            updateDisplay()
        }
    }
    private fun clear() {
        currentInput = "0"
        currentOperator = ""
        firstOperand = 0.0
        updateDisplay()
    }
    private fun addDecimal() {
        if (!currentInput.contains(",")) {
            currentInput += ","
            updateDisplay()
        }
    }
    private fun toggleSign() {
        if (currentInput != "0" && currentInput != "Error") {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1)
            } else {
                currentInput = "-$currentInput"
            }
            updateDisplay()
        }
    }
    private fun calculatePercentage() {
        if (currentInput != "Error") {
            val number = currentInput.replace(",", ".").toDouble()
            val percentage = number / 100.0

            currentInput = if (percentage % 1 == 0.0) {
                percentage.toInt().toString()
            } else {
                percentage.toString().replace(".", ",")
            }
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        resultText.text = currentInput
    }
}