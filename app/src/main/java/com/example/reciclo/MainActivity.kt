package com.example.reciclo

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.reciclo.model.Client
import com.example.reciclo.model.Worker
import java.io.Serializable

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clients:ArrayList<Client>? = intent.getParcelableArrayListExtra("CLIENTS")
        val workerButton = findViewById<Button>(R.id.worker_button)
        val clientButton = findViewById<Button>(R.id.client_button)
        val worker = intent.getSerializableExtra("WORKER") as? Worker

        clientButton.setOnClickListener{
            val intent = Intent(this, MapsActivityClient::class.java)
            intent.putExtra("WORKER", worker)
            if(clients!=null){
                intent.putParcelableArrayListExtra("CLIENTS",
                    clients)
            }
            startActivity(intent)
        }

        workerButton.setOnClickListener{
            val intent = Intent(this, MapsActivityWorker::class.java)
            intent.putExtra("WORKER", worker)
            if(clients!=null){
                intent.putParcelableArrayListExtra("CLIENTS",
                    clients)
            }
            startActivity(intent)
        }

    }
}