package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var operand: Double? = null
    private var pendingOp: String? = null
    private var freshInput = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wireButtons()
    }

    private fun wireButtons() {
        val digits = mapOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
        )
        digits.forEach { (btn, d) -> btn.setOnClickListener { appendDigit(d) } }

        binding.btnDot.setOnClickListener { appendDecimal() }
        binding.btnClear.setOnClickListener { clearAll() }
        binding.btnBackspace.setOnClickListener { backspace() }

        binding.btnAdd.setOnClickListener { onOperator("+") }
        binding.btnSubtract.setOnClickListener { onOperator("−") }
        binding.btnMultiply.setOnClickListener { onOperator("×") }
        binding.btnDivide.setOnClickListener { onOperator("÷") }
        binding.btnEquals.setOnClickListener { onEquals() }
    }

    private fun appendDigit(d: String) {
        if (binding.display.text.toString() == "Error") clearAll()
        if (freshInput) {
            binding.display.text = d
            freshInput = false
        } else {
            val cur = binding.display.text.toString()
            binding.display.text = if (cur == "0" && d != "0") d else cur + d
        }
    }

    private fun appendDecimal() {
        if (binding.display.text.toString() == "Error") {
            clearAll()
            binding.display.text = "0."
            freshInput = false
            return
        }
        if (freshInput) {
            binding.display.text = "0."
            freshInput = false
            return
        }
        val cur = binding.display.text.toString()
        if (!cur.contains('.')) binding.display.text = "$cur."
    }

    private fun backspace() {
        if (binding.display.text.toString() == "Error") {
            clearAll()
            return
        }
        if (freshInput) return
        val cur = binding.display.text.toString()
        if (cur.length <= 1) {
            binding.display.text = "0"
            freshInput = true
        } else {
            binding.display.text = cur.dropLast(1)
        }
    }

    private fun clearAll() {
        operand = null
        pendingOp = null
        freshInput = true
        binding.display.text = "0"
    }

    private fun displayValue(): Double? =
        binding.display.text.toString().toDoubleOrNull()

    private fun onOperator(symbol: String) {
        if (binding.display.text.toString() == "Error") {
            clearAll()
            return
        }
        val op = mapSymbolToOp(symbol) ?: return

        if (!freshInput) {
            val value = displayValue() ?: return
            if (operand != null && pendingOp != null) {
                val result = applyOp(operand!!, pendingOp!!, value) ?: run {
                    showError()
                    return
                }
                binding.display.text = formatResult(result)
                operand = result
            } else {
                operand = value
            }
        }
        pendingOp = op
        freshInput = true
    }

    private fun onEquals() {
        if (binding.display.text.toString() == "Error") return
        if (operand == null || pendingOp == null) return
        val value = displayValue() ?: return
        val result = applyOp(operand!!, pendingOp!!, value) ?: run {
            showError()
            return
        }
        binding.display.text = formatResult(result)
        operand = null
        pendingOp = null
        freshInput = true
    }

    private fun mapSymbolToOp(s: String): String? = when (s) {
        "+" -> "+"
        "−" -> "-"
        "×" -> "*"
        "÷" -> "/"
        else -> null
    }

    private fun applyOp(left: Double, op: String, right: Double): Double? = when (op) {
        "+" -> left + right
        "-" -> left - right
        "*" -> left * right
        "/" -> if (right == 0.0) null else left / right
        else -> null
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val longVal = value.toLong()
        if (abs(value - longVal.toDouble()) < 1e-12 && abs(value) < 1e15) {
            return longVal.toString()
        }
        val s = value.toString()
        return if (s.contains('E') || s.contains('e')) s else s.trimEnd('0').trimEnd('.')
    }

    private fun showError() {
        binding.display.text = "Error"
        operand = null
        pendingOp = null
        freshInput = true
    }
}
