package com.example.qrtriplecheck
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class MainActivity : Activity() {
 private val values = mutableListOf<String>()
 private lateinit var status: TextView
 private lateinit var lines: List<TextView>
 private lateinit var scanButton: Button
 private var scanning = false
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,40,32,24); setBackgroundColor(Color.WHITE) }
  container.addView(TextView(this).apply { text = "QR코드 3개 비교 검사"; textSize = 23f; setTextColor(Color.BLACK); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-1,90))
  status = TextView(this).apply { textSize = 25f; gravity = Gravity.CENTER; setTextColor(Color.DKGRAY) }
  container.addView(status, LinearLayout.LayoutParams(-1,110))
  lines = (0 until 3).map { TextView(this).apply { textSize = 16f; setTextColor(Color.BLACK); setPadding(12,16,12,16); setTextIsSelectable(true); container.addView(this,LinearLayout.LayoutParams(-1,-2)) } }
  scanButton = Button(this).apply { setOnClickListener { scanNext() } }
  container.addView(scanButton,LinearLayout.LayoutParams(-1,-2))
  container.addView(Button(this).apply { text = "초기화 / 다음 제품"; setOnClickListener { if (!scanning) { values.clear(); refresh() } } },LinearLayout.LayoutParams(-1,-2))
  setContentView(container)
  refresh()
 }
 private fun scanNext() {
  if (scanning || values.size >= 3) return
  scanning = true
  scanButton.isEnabled = false
  GmsBarcodeScanning.getClient(this).startScan()
   .addOnSuccessListener { barcode ->
    val raw = barcode.rawValue ?: ""
    if (barcode.format != Barcode.FORMAT_QR_CODE) Toast.makeText(this,"QR코드만 인식할 수 있습니다.",Toast.LENGTH_SHORT).show()
    else if (raw.length < 12) Toast.makeText(this,"12자리 미만입니다. 다시 스캔하세요.",Toast.LENGTH_LONG).show()
    else values.add(raw)
   }
   .addOnFailureListener { error: Exception -> Toast.makeText(this,"스캔 오류: ${error.localizedMessage}",Toast.LENGTH_LONG).show() }
   .addOnCompleteListener { scanning = false; refresh() }
 }
 private fun refresh() {
  lines.forEachIndexed { index,line ->
   val value = values.getOrNull(index)
   line.text = if (value == null) "QR ${index+1}: 미스캔" else "QR ${index+1}: $value\n마지막 12자리: ${value.takeLast(12)}"
  }
  if (values.size < 3) {
   status.text = "검사 대기 (${values.size}/3)"
   status.setTextColor(Color.DKGRAY)
   scanButton.text = "QR ${values.size+1} 스캔"
   scanButton.visibility = View.VISIBLE
   scanButton.isEnabled = !scanning
  } else {
   val matched = values.map { it.takeLast(12) }.distinct().size == 1
   status.text = if (matched) "PASS ✓" else "FAIL ✕"
   status.setTextColor(if (matched) Color.rgb(0,135,60) else Color.RED)
   scanButton.visibility = View.GONE
  }
 }
}