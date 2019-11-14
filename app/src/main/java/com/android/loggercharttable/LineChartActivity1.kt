package com.android.loggercharttable

//import android.util.Log

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.DashPathEffect
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.loggercharttable.dataclasses.ErrorResponse
import com.android.loggercharttable.dataclasses.POSTResult
import com.android.loggercharttable.dataclasses.Point
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_linechart.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.cert.CertificateException
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class LineChartActivity1 : DemoBase(), OnChartValueSelectedListener {
    private val values = ArrayList<Entry>()
    private var okHttpClient = OkHttpClient()
    private var chart: LineChart? = null
    internal var points_count: String? = null
    internal var result: Int = 0
    private lateinit var response_model:List<Point>
    val tableLayout by lazy { TableLayout(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        if (intent.getStringExtra("orientation")=="land") {
            requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else if (intent.getStringExtra("orientation")=="port") {
            requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_linechart)
        //setContentView(R.layout.activity_linechart)
        points_count = intent.getStringExtra("points_number")
        result = Integer.parseInt(points_count!!)
        callGetPoints()
        title = "Gregory Ivanyuk Test App"
        drawingFun()
        //Handler().postDelayed({ onBackPressed()  },5000)
    }
    private fun drawingFun() {
        run {
            // // Chart Style // //
            chart = findViewById(R.id.chart1)

            // background color
            chart!!.setBackgroundColor(Color.WHITE)

            // disable description text
            chart!!.description.isEnabled = false

            // enable touch gestures
            chart!!.setTouchEnabled(true)

            // set listeners
            chart!!.setOnChartValueSelectedListener(this)
            chart!!.setDrawGridBackground(false)

            // create marker to display box when values are selected
            val mv = MyMarkerView(this, R.layout.custom_marker_view)

            // Set the marker to the chart
            mv.chartView = chart
            chart!!.marker = mv

            // enable scaling and dragging
            chart!!.isDragEnabled = true
            chart!!.setScaleEnabled(true)
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            chart!!.setPinchZoom(true)
        }

        val xAxis: XAxis
        run {
            // // X-Axis Style // //
            xAxis = chart!!.xAxis

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f)
            // axis range
            xAxis.axisMaximum = 100f
            xAxis.axisMinimum = -100f
        }

        val yAxis: YAxis
        run {
            // // Y-Axis Style // //
            yAxis = chart!!.axisLeft

            // disable dual axis (only use LEFT axis)
            chart!!.axisRight.isEnabled = false

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f)

            // axis range
            yAxis.axisMaximum = 100f
            yAxis.axisMinimum = -100f
        }



    }

    companion object {
        fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }

                return builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
    private fun onUiToast(text_toast:String){
        Thread(Runnable {
            // try to touch View of UI thread
            runOnUiThread(java.lang.Runnable {
                if (text_toast!="") {
                    val toast = Toast.makeText(applicationContext,text_toast, Toast.LENGTH_LONG)
                    toast.show()
                    finish()
                    //toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
                }
            })
        }).start()
    }
    fun callGetPoints(){
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val gson = GsonBuilder().setPrettyPrinting().create()
        val precalc = "{}"
        //val precalc = PATCH_BODY(valid_from = insurance_issue_date.text.toString(),valid_to = SharedPreferenceHelper.getSharedPreferencesString(context!!,"insurance_end_date",""))
        val jsonPrecalc: String = gson.toJson(precalc)
        val body = RequestBody.create(JSON, jsonPrecalc)
        val okHttpClient = getUnsafeOkHttpClient().build()
        val request = Request.Builder()
            .url("https://demo.bankplus.ru/mobws/json/pointsList?version=1.1&count="+result)
            .tag("date_patch")
            .post(body)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //Log.i("LOG_TAG",e.toString())
                onUiToast("Проверьте подключение к Интернет")
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    val responseData = response.body()!!.string()
                    val jsonObject = JSONObject(responseData)
                    val result_value = gson.fromJson(jsonObject.toString(), POSTResult::class.java).result
                    response_model = gson.fromJson(jsonObject.toString(), POSTResult::class.java).response.points.sortedWith(compareByDescending{ it.x }).reversed()
                    //insurance_list.clear()
                    try {
                        if (result_value==0) {
                            //Log.i("POINTS",response_model.toString())
                            //Log.i("POINTS",sortedList.toString())
                            // add data
                            for (i in response_model.indices) {
                                values.add(Entry(response_model[i].x, response_model[i].y))
                            }
                            Thread(Runnable {
                            // try to touch View of UI thread
                                runOnUiThread(java.lang.Runnable {
                                setData()
                                // draw points over time
                                chart!!.animateX(1500)
                                // get the legend (only possible after setting data)
                                val l = chart!!.legend
                                // draw legend entries as lines
                                l.form = LegendForm.LINE
                                    //creating adapter
                                    val lp = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                    tableLayout.apply {
                                        layoutParams = lp
                                        isShrinkAllColumns = true
                                    }
                                    for (i in 0 until result) {

                                        val row = TableRow(applicationContext)
                                        row.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT)

                                        for (j in 0 until 1) {
                                            val count_number = i+1
                                            val text_view = TextView(applicationContext)
                                            text_view.apply {
                                                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                                    TableRow.LayoutParams.WRAP_CONTENT)
                                                text = "Точка :"+count_number.toString()+" X : "+response_model[i].x.toString()+" Y : "+response_model[i].y.toShort()
                                            }
                                            row.addView(text_view)
                                        }
                                        tableLayout.addView(row)
                                    }
                                    linearLayout.addView(tableLayout)
                                })

                            }).start()
                        } else {
                            val result_value_error = gson.fromJson(jsonObject.toString(), ErrorResponse::class.java).response
                            if(result_value_error.result==-1){
                                onUiToast(result_value_error.message)
                            } else if (result_value_error.result==-100){
                                onUiToast("неверные параметры запроса")
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } catch (e: JSONException) {

                }

            }
        })
    }

    object OkHttpUtils {
        fun cancelCallWithTag(client: OkHttpClient, tag: String) {
            for (call in client.dispatcher().queuedCalls()) {
                if (call.request().tag()!!.equals(tag))
                    call.cancel()
            }
            for (call in client.dispatcher().runningCalls()) {
                if (call.request().tag()!!.equals(tag))
                    call.cancel()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Cancel previous call(s) if they are running or queued
        OkHttpUtils.cancelCallWithTag(okHttpClient, "date_patch")
    }
    private fun setData() {
        
        val set1: LineDataSet

        if (chart!!.data != null && chart!!.data.dataSetCount > 0) {
            set1 = chart!!.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart!!.data.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1")

            set1.setDrawIcons(false)

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area
/*            set1.setDrawFilled(true)
            set1.fillFormatter =
                IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }*/

            // set color of filled area
/*            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                val drawable = ContextCompat.getDrawable(this, R.drawable.fade_red)
                set1.fillDrawable = drawable
            } else {
                set1.fillColor = Color.BLACK
            }*/

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            chart!!.data = data
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.viewHH -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    Uri.parse("https://hh.ru/resume/6c7ba511ff0119a1430039ed1f6f757a514337")
                startActivity(i)
            }
            R.id.actionToggleValues -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    set.setDrawValues(!set.isDrawValuesEnabled)
                }

                chart!!.invalidate()
            }
            R.id.actionToggleIcons -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    set.setDrawIcons(!set.isDrawIconsEnabled)
                }

                chart!!.invalidate()
            }
            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data.isHighlightEnabled = !chart!!.data.isHighlightEnabled
                    chart!!.invalidate()
                }
            }
            R.id.actionToggleFilled -> {

                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    if (set.isDrawFilledEnabled)
                        set.setDrawFilled(false)
                    else
                        set.setDrawFilled(true)
                }
                chart!!.invalidate()
            }
            R.id.actionToggleCircles -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    if (set.isDrawCirclesEnabled)
                        set.setDrawCircles(false)
                    else
                        set.setDrawCircles(true)
                }
                chart!!.invalidate()
            }
            R.id.actionToggleCubic -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                chart!!.invalidate()
            }
            R.id.actionToggleStepped -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                chart!!.invalidate()
            }
            R.id.actionToggleHorizontalCubic -> {
                val sets = chart!!.data
                    .dataSets

                for (iSet in sets) {

                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                chart!!.invalidate()
            }
            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled)
                    chart!!.setPinchZoom(false)
                else
                    chart!!.setPinchZoom(true)

                chart!!.invalidate()
            }
            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }
            R.id.animateX -> {
                chart!!.animateX(2000)
            }
            R.id.animateY -> {
                chart!!.animateY(2000, Easing.EaseInCubic)
            }
            R.id.animateXY -> {
                chart!!.animateXY(2000, 2000)
            }
            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveToGallery()
                } else {
                    chart?.let { requestStoragePermission(it) }
                }
            }
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(chart!!, "LineChartActivity1")
    }


    override fun onValueSelected(e: Entry, h: Highlight) {
        //Log.i("Entry selected", e.toString())
        //Log.i("LOW HIGH", "low: " + chart!!.lowestVisibleX + ", high: " + chart!!.highestVisibleX)
/*        Log.i(
            "MIN MAX",
            "xMin: " + chart!!.xChartMin + ", xMax: " + chart!!.xChartMax + ", yMin: " + chart!!.yChartMin + ", yMax: " + chart!!.yChartMax
        )*/
    }

    override fun onNothingSelected() {
        //Log.i("Nothing selected", "Nothing selected.")
    }
}

