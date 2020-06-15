package com.example.banhmi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Host extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseFirestore db;
    TextView textView;
    Button btnHostGame, btnJoinGame;
    Intent intent;
    Spinner spinnerNumberOfPlayers;
    int numberOfPlayers;
    boolean sessionExists;
    private static final String TAG = "HOST";
    private static final String[] cardDeck =  {"Kz7","Kz8","Kz9","Kz10","Kz11","Kz12","Kz0","Kz1",
                                                "Ka7","Ka8","Ka9","Ka10","Ka11","Ka12","Ka0","Ka1",
                                                 "Hz7","Hz8","Hz9","Hz10","Hz11","Hz12","Hz0","Hz1",
                                                 "Pi7","Pi8","Pi9","Pi10","Pi11","Pi12","Pi0","Pi1"};

    public void shuffle(){
        Collections.shuffle(Arrays.asList(cardDeck));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        setUpViews();
        db = FirebaseFirestore.getInstance();
    }

    private void setUpViews() {
        textView = findViewById(R.id.textView);
        spinnerNumberOfPlayers = findViewById(R.id.spinnerNumberOfPlayers);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Host.this,android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.stringArray));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumberOfPlayers.setAdapter(arrayAdapter);
        spinnerNumberOfPlayers.setOnItemSelectedListener(this);
        btnHostGame = findViewById(R.id.btnHostGame);
        btnHostGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpGame();
            }
        });
        btnJoinGame = findViewById(R.id.btnJoinGame);
        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sessionExists) {
                    intent = new Intent(Host.this, Client.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Bitte wähle zuerst 'Host Game'",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpGame(){
        shuffle();
        uploadNewDeck();
    }

    private void uploadNewDeck(){

        Map<String, Object> cardDeckOnline = new HashMap<>();
        // erste Karte wird automatisch übersprungen und zu droped card
        for(int i = 1; i < cardDeck.length; ++i) {
            cardDeckOnline.put("Card"+(i+100), cardDeck[i]);
        }
// Add a new document with a generated ID
        db.collection("CardDeck")
                .add(cardDeckOnline)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        String id = documentReference.getId();
                        Client.setSessionID(id);
                        Client.setHost(true);
                        sessionExists = true;
                        uploadPlayer(id);
                        textView.setText("Deine Gamesession ID: " + id);
                        uploadGamestate(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void uploadPlayer(String id){
        Map<String, Object> playerOnline = new HashMap<>();
        playerOnline.put("p1","p1");
        playerOnline.put("p2","p2");
        if(numberOfPlayers>=3) playerOnline.put("p3","p3");
        if(numberOfPlayers>=4) playerOnline.put("p4","p4");
        db.collection("CardDeck").document(id).collection("Player").document("PlayerDoc").set(playerOnline);
    }

    private void uploadGamestate(String id){

        //GameState gs = GameState.getInstance(id);
        //gs.setUpDroped(cardDeck[0]);
        Map<String,Object> game = new HashMap<>();
        game.put("p1","");
        game.put("p2","");
        game.put("p3","");
        game.put("p4","");
        game.put("droped",cardDeck[0]);
        game.put("turn",1);
        game.put("tmp","");
        game.put("NOP",numberOfPlayers);
        db.collection("CardDeck").document(id).collection("Player").document("Gamestate").set(game);
        Log.d(TAG,"Setup finished, session set:"+id);
       // erste Karte von Deck nehmen und auf Gamestate droped verschieben;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        numberOfPlayers = Integer.parseInt(parent.getItemAtPosition(position).toString());
        Log.d(TAG,"NumberOfPlayers set on: "+numberOfPlayers);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
