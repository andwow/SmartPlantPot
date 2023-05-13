package com.timusandrei.smartpot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.timusandrei.smartpot.adapters.SmartPotAdapter
import com.timusandrei.smartpot.models.SmartPot
import kotlin.collections.ArrayList

class MainMenu : AppCompatActivity() {

    private val smartPots : ArrayList<SmartPot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val addDevice : TextView = findViewById(R.id.add_device)
        addDevice.setOnClickListener(View.OnClickListener { _ ->
            val switchActivityIntent = Intent(this, AddDevice::class.java)
            startActivity(switchActivityIntent)
        })
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.smartpots_recyclerview)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        val adapter = SmartPotAdapter(
            smartPots
        ) { smartPot -> showSmartPot(smartPot) }
        recyclerView.adapter = adapter
        eventChangeListener(adapter)
    }

    private fun eventChangeListener(adapter : SmartPotAdapter) {

        smartPots.clear()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val path = "/Users/" + userId + "/SmartPots"
        val db = FirebaseFirestore.getInstance()

        if(userId != null) {
            db.collection(path).addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return
                    }
                for(dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val smartPot = dc.document.toObject(SmartPot::class.java)
                        smartPots.add(smartPot)
                    }
                }
                    adapter.notifyDataSetChanged()
                }
            })
        }

    }

    private fun showSmartPot(smartPot: SmartPot) {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        switchActivityIntent.putExtra("productId", smartPot.productId)
        startActivity(switchActivityIntent)
    }

}