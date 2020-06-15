package com.example.banhmi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private static final String TAG = "MAIN_ACTIVITY";
    private Button buttonHost;
    private Button buttonClient;
    private EditText editSessionID, editPlayerName;
    private ListView listView;
    private ArrayList<String> list;
    ArrayAdapter arrayAdapter;
    Intent intent;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        sharedPref = getSharedPreferences("PlayerName", Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        list = new ArrayList();
        setUpViews();
    }

    private void setUpViews() {
        editSessionID = findViewById(R.id.editSessionID);
        editPlayerName = findViewById(R.id.editTextName);
        editPlayerName.setText(sharedPref.getString("PlayerName",""));
        buttonHost = findViewById(R.id.buttonHost);
        buttonHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(readInPlayerName()){
                intent = new Intent(MainActivity.this, Host.class);
                startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Bitte gib zuerst deinen Namen ein", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonClient = findViewById(R.id.buttonClient);
        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validCheck(editSessionID.getText().toString())) {
                    if(readInPlayerName()) {
                        Client.setSessionID(editSessionID.getText().toString());
                        intent = new Intent(MainActivity.this, Client.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Bitte gib zuerst deinen Namen ein", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Bitte wähle eine Session ID aus", Toast.LENGTH_SHORT).show();
                }
            }
        });
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView = findViewById(R.id.listView);
        getSessions();
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"Item ausgeählt: "+ list.get(position));
                editSessionID.setText(list.get(position));
            }
        });
    }

    private boolean readInPlayerName(){
        if(!editPlayerName.getText().toString().equals("")) {
            prefEditor.putString("pName", editPlayerName.getText().toString());
            prefEditor.commit();
            return true;
        }
        return false;
    }

    private boolean validCheck(String sessionID){
        if(list.contains(sessionID)) {
            return true;
        }
        return false;
    }

    private void getSessions(){
        db.collection("CardDeck")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        Log.d(TAG,"Sessions gefunden starte Auflistung");
                        for(QueryDocumentSnapshot document : value){
                            //if(document.get("Player")!=null)
                            if(!list.contains(document.getId())) {
                                list.add(document.getId());
                            }
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
