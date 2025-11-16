package com.example.android_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.pm.ActivityInfo
<<<<<<< HEAD
=======
import android.graphics.Color
import kotlin.random.Random
>>>>>>> origin/1

class MainActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    private var currentInput = "0"
<<<<<<< HEAD
    private var currentOperator = ""
    private var firstOperand = 0.0
=======
>>>>>>> origin/1
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
<<<<<<< HEAD
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
=======
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
            findViewById<Button>(R.id.button31).setOnClickListener {
                calculate()
                change_color()}
            findViewById<Button>(R.id.button32).setOnClickListener { clear() }
            findViewById<Button>(R.id.button27).setOnClickListener { addDecimal() }
            findViewById<Button>(R.id.button34).setOnClickListener { calculatePercentage() }
            findViewById<Button>(R.id.button33).setOnClickListener { toggleSign() }

            updateDisplay()
        }
    private fun change_color() {
        val buttons = listOf(
            R.id.button26, R.id.button21, R.id.button22, R.id.button25,
            R.id.button19, R.id.button20, R.id.button24, R.id.button1,
            R.id.button18, R.id.button23, R.id.button30, R.id.button29,
            R.id.button28, R.id.button35, R.id.button32, R.id.button27,
            R.id.button34, R.id.button33
        )
        buttons.forEach { buttonId ->
            val color = Color.rgb(
                Random.nextInt(255),
                Random.nextInt(255),
                Random.nextInt(255)
            )
            findViewById<Button>(buttonId).setBackgroundColor(color)
        }
>>>>>>> origin/1
    }

    private fun appendNumber(number: String) {
        when {
            currentInput == "0" || currentInput == "Error" -> {
                currentInput = number
            }
<<<<<<< HEAD
            currentInput == "-0" -> {
                currentInput = "-$number"
            }
            currentInput.endsWith(",0") -> {
                currentInput = currentInput.dropLast(1) + number
            }
            currentInput.last().toString() in setOf("+", "-", "*", "/") -> {
                val operator = currentInput.last().toString()
                currentInput = number
                currentOperator = operator
            }
=======

            currentInput == "-0" -> {
                currentInput = "-$number"
            }

            currentInput.endsWith(",0") -> {
                currentInput = currentInput.dropLast(2) + number
            }

>>>>>>> origin/1
            else -> {
                currentInput += number
            }
        }
        updateDisplay()
    }
    private fun setOperator(operator: String) {
        if (currentInput == "Error" || currentInput.isEmpty() || currentInput == "-") {
<<<<<<< HEAD
            return}
        val lastChar = currentInput.last().toString()
        if (lastChar == "+" || lastChar == "-" || lastChar == "*" || lastChar == "/") {
            currentInput = currentInput.dropLast(1) + operator
            currentOperator = operator
            updateDisplay()
            return}
        if (currentOperator.isNotEmpty()) {
                calculate()}
        firstOperand = currentInput.replace(",", ".").toDouble()
        currentInput += operator
        currentOperator = operator
        updateDisplay()}
    private fun calculate() {
        if (currentOperator.isNotEmpty()) {
            val operatorIndex = currentInput.indexOf(currentOperator)
            val secondOperandStr = currentInput.substring(operatorIndex + 1)
            if (secondOperandStr.isNotEmpty()) {
                val secondOperand = secondOperandStr.replace(",", ".").toDouble()
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
=======
            return
        }
        val lastChar = currentInput.last().toString()
        if (lastChar == "+" || lastChar == "-" || lastChar == "*" || lastChar == "/") {
            currentInput = currentInput.dropLast(1) + operator
        }
        else if (!hasOperator()) {
            currentInput += operator
        }
        else {
            return
        }
        updateDisplay()
    }
    private fun hasOperator(): Boolean {
        return currentInput.contains('+') || currentInput.contains('-') ||
                currentInput.contains('*') || currentInput.contains('/')
    }
    private fun hasTwoNumbersAndOperator(): Boolean {
        val operators = setOf('+', '-', '*', '/')
        var operatorCount = 0
        for (char in currentInput) {
            if (char in operators) {
                operatorCount++
            }
        }
        if (operatorCount > 0) {
            val parts = currentInput.split('+', '-', '*', '/')
            if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                return true
            }
        }

        return false
    }
    private fun calculate() {
        try {
            val expression = currentInput.replace(",", ".")
            val operator = when {
                expression.contains("+") -> "+"
                expression.contains("-") -> "-"
                expression.contains("*") -> "*"
                expression.contains("/") -> "/"
                else -> ""
            }
            if (operator.isNotEmpty()) {
                val parts = expression.split(operator)
                if (parts.size == 2) {
                    val first = parts[0].toDouble()
                    val second = parts[1].toDouble()
                    val result = when (operator) {
                        "+" -> first + second
                        "-" -> first - second
                        "*" -> first * second
                        "/" -> if (second != 0.0) first / second else Double.NaN
                        else -> Double.NaN
                    }
                    currentInput = if (result.isNaN()) "Error" else {
                        formatResult(result)
                    }
                }
            }
            updateDisplay()
        } catch (e: Exception) {
            currentInput = "Error"
            updateDisplay()
        }
    }

    private fun formatResult(result: Double): String {
        return if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            var formatted = "%.10f".format(result).replace(",", "")
            formatted = formatted.replace("0*$".toRegex(), "")
            if (formatted.endsWith(".")) {
                formatted = formatted.dropLast(1)
            }
            formatted.replace(".", ",")
>>>>>>> origin/1
        }
    }
    private fun clear() {
        currentInput = "0"
<<<<<<< HEAD
        currentOperator = ""
        firstOperand = 0.0
        updateDisplay()
    }
    private fun addDecimal() {
        if (!currentInput.contains(",")) {
            if (currentInput == "0" || currentInput == "Error") {
                currentInput = "0,"
            } else if (currentInput == "-0") {
                currentInput = "-0,"
            } else {
                currentInput += ","
            }
=======
        updateDisplay()
    }
    private fun addDecimal() {
        if (currentInput == "Error") {
            currentInput = "0,"
            updateDisplay()
            return
        }
        val lastNumber = currentInput.split('+', '-', '*', '/').last()
        if (!lastNumber.contains(",")) {
            currentInput += ","
>>>>>>> origin/1
            updateDisplay()
        }
    }
    private fun toggleSign() {
<<<<<<< HEAD
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
=======
        if (currentInput == "0" || currentInput == "Error") {
            return
        }
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
            val result = number / 100
            currentInput = if (result % 1 == 0.0) {
                result.toInt().toString()
            } else {
                result.toString().replace(".", ",")
            }
            updateDisplay()
        } else {
            currentInput = "Error"
            updateDisplay()
        }
    }
    private fun updateDisplay() {
        resultText.text = currentInput
    }
}
>>>>>>> origin/1
