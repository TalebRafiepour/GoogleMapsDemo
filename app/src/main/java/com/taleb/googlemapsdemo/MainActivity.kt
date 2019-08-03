package com.taleb.googlemapsdemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }


    private fun initUI() {

        if (isPlayServiceAvailable()){
            openMapButton.setOnClickListener {
                openMap()
            }
        }
    }


    private fun openMap() {
        //open map
        val intent = Intent(this,MapActivity::class.java)
        startActivity(intent)
    }




    private fun isPlayServiceAvailable() : Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        when {
            available == ConnectionResult.SUCCESS -> {
                //everything is ok and user can request google map
                return true
            }
            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                //an error accured but we can resolve that

                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,available, ERROR_DIALOG_REQUEST)
                dialog.show()
            }
            else -> Toast.makeText(this,"You can't make map request!",Toast.LENGTH_LONG).show()
        }

        return false
    }


    companion object{
        private const val ERROR_DIALOG_REQUEST = 9001
    }
}
