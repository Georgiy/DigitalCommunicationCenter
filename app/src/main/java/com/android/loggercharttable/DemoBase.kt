package com.android.loggercharttable

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.github.mikephil.charting.charts.Chart
import com.google.android.material.snackbar.Snackbar

/**
 * Base class of all Activities of the Demo Application.
 *
 * @author Philipp Jahoda
 */
abstract class DemoBase : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    protected lateinit var tfRegular: Typeface
    protected lateinit var tfLight: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tfRegular = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf")
        tfLight = Typeface.createFromAsset(assets, "OpenSans-Light.ttf")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery()
            } else {
                Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    protected fun requestStoragePermission(view: View) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Snackbar.make(
                view,
                "Write permission is required to save image to gallery",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        this@DemoBase,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_STORAGE
                    )
                }.show()
        } else {
            Toast.makeText(applicationContext, "Permission Required!", Toast.LENGTH_SHORT)
                .show()
            ActivityCompat.requestPermissions(
                this@DemoBase,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_STORAGE
            )
        }
    }

    protected fun saveToGallery(chart: Chart<*>, name: String) {
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70))
            Toast.makeText(
                applicationContext, "Saving SUCCESSFUL!",
                Toast.LENGTH_SHORT
            ).show()
        else
            Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT)
                .show()
    }

    protected abstract fun saveToGallery()

    companion object {


        private val PERMISSION_STORAGE = 0
    }
}
