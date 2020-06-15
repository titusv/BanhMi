package com.example.banhmi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Client extends AppCompatActivity {

    FirebaseFirestore db;
    private static final String TAG = "CLIENT";
    private static boolean host = false;
    Button btnTakeNewCard,btnTakeDropedCard,card1,card2,card3,card4,btnThrowAway,btnTakeIT;
    ImageView x1,x2,x3,x4,y1,y2,y3,y4,z1,z2,z3,z4;
    ImageView imageViewTmp;
    int player = -1;
    private Card allCards[];
    private Card ownCards[];
    private Card tmpCard;
    //private boolean setUp,
    private boolean swapIT;
    private GameState gs;
    private STEP step;
    private TextView textViewScoreX,textViewScoreX2 ,textViewScoreY,textViewScoreY2, textViewScoreZ,textViewScoreZ2; // 2 steht für total score
    private String playerNameX,playerNameY,playerNameZ;
    private static String sessionID;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;
    String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        db = FirebaseFirestore.getInstance();
        sharedPref = getSharedPreferences("PlayerName",MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        playerName = sharedPref.getString("pName","No Name");
        gs = new GameState(sessionID);
        tmpCard = new Card(0,"","",0);
        ownCards = new Card[4];
        setUpViews();
        setUpCards();
        getPlayerID();
        setUpListener();
        step = STEP.SETUP;
        Toast.makeText(getApplicationContext(),"Bitte ziehe 4 Karten vom Stapel", Toast.LENGTH_LONG).show();
        readStorage();
    }

    public static void setHost(boolean bool){
        host = bool;
    }

    private void setUpViews() {
        textViewScoreX = findViewById(R.id.textViewScoreX);
        textViewScoreY = findViewById(R.id.textViewScoreY);
        textViewScoreZ = findViewById(R.id.textViewScoreZ);
        textViewScoreX2 = findViewById(R.id.textViewScoreX2);
        textViewScoreY2 = findViewById(R.id.textViewScoreY2);
        textViewScoreZ2 = findViewById(R.id.textViewScoreZ2);
        imageViewTmp = findViewById(R.id.viewActCard);
        x1 = findViewById(R.id.x1);
        x2 = findViewById(R.id.x2);
        x3 = findViewById(R.id.x3);
        x4 = findViewById(R.id.x4);
        y1 = findViewById(R.id.y1);
        y2 = findViewById(R.id.y2);
        y3 = findViewById(R.id.y3);
        y4 = findViewById(R.id.y4);
        z1 = findViewById(R.id.z1);
        z2 = findViewById(R.id.z2);
        z3 = findViewById(R.id.z3);
        z4 = findViewById(R.id.z4);
        //SELECT
        Log.d(TAG,"btnDropedCard setUp completed"+(btnTakeDropedCard!=null));
        btnTakeNewCard = findViewById(R.id.btnTakeNewCard);
        btnTakeNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (select()){
                    getCardFromDeck();
                    //gs.updateTmp(tmpCard);
                }
            }
        });
        btnTakeDropedCard = findViewById(R.id.btnTakeDropedCard);
        btnTakeDropedCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select()&&step!=STEP.SETUP){
                    tmpCard = gs.getDroped();
                    imageViewTmp.setImageDrawable(getDrawable(tmpCard.getDrawableID()));
                    //gs.updateTmp(tmpCard);
                }
            }
        });
        //LAY_DOWN
        card1 = findViewById(R.id.card1);
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layDown())
                    openCard(card1, 0);
            }
        });
        card2 = findViewById(R.id.card2);
        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layDown())
                openCard(card2,1);
            }
        });
        card3 = findViewById(R.id.card3);
        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layDown())
                openCard(card3,2);
            }
        });
        card4 = findViewById(R.id.card4);
        card4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layDown())
                openCard(card4,3);
            }
        });

        //chose
        btnTakeIT = findViewById(R.id.btnTakeIt);
        btnTakeIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choose()) {
                    swapIT = true;
                }
            }
        });
        btnThrowAway = findViewById(R.id.btnThrowAway);
        btnThrowAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choose()) {
                    gs.setDroped(tmpCard);
                    btnTakeDropedCard.setBackground(getDrawable(tmpCard.getDrawableID()));
                    imageViewTmp.setImageDrawable(null);
                    tmpCard = null;
                    swapIT = false;
                }
            }
        });
    }

    private void setUpCards(){
        allCards = new Card[32];
        allCards[0] = new Card(0,"Hz","0", R.drawable.c0herz);
        allCards[1] = new Card(1,"Hz","1", R.drawable.c1herz);
        allCards[2] = new Card(7,"Hz","7", R.drawable.c7herz);
        allCards[3] = new Card(8,"Hz","8", R.drawable.c8herz);
        allCards[4] = new Card(9,"Hz","9", R.drawable.c9herz);
        allCards[5] = new Card(10,"Hz","10", R.drawable.c10herz);
        allCards[6] = new Card(10,"Hz","11", R.drawable.c11herz);
        allCards[7] = new Card(10,"Hz","12", R.drawable.c12herz);

        allCards[8] = new Card(0,"Pi","0", R.drawable.c0pik);
        allCards[9] = new Card(1,"Pi","1", R.drawable.c1pik);
        allCards[10] = new Card(7,"Pi","7", R.drawable.c7pik);
        allCards[11] = new Card(8,"Pi","8", R.drawable.c8pik);
        allCards[12] = new Card(9,"Pi","9", R.drawable.c9pik);
        allCards[13] = new Card(10,"Pi","10", R.drawable.c10pik);
        allCards[14] = new Card(10,"Pi","11", R.drawable.c11pik);
        allCards[15] = new Card(10,"Pi","12", R.drawable.c12pik);

        allCards[16] = new Card(0,"Kz","0", R.drawable.c0kreuz);
        allCards[17] = new Card(1,"Kz","1", R.drawable.c1kreuz);
        allCards[18] = new Card(7,"Kz","7", R.drawable.c7kreuz);
        allCards[19] = new Card(8,"Kz","8", R.drawable.c8kreuz);
        allCards[20] = new Card(9,"Kz","9", R.drawable.c9kreuz);
        allCards[21] = new Card(10,"Kz","10", R.drawable.c10kreuz);
        allCards[22] = new Card(10,"Kz","11", R.drawable.c11kreuz);
        allCards[23] = new Card(10,"Kz","12", R.drawable.c12kreuz);

        allCards[24] = new Card(0,"Ka","0", R.drawable.c0karo);
        allCards[25] = new Card(1,"Ka","1", R.drawable.c1karo);
        allCards[26] = new Card(7,"Ka","7", R.drawable.c7karo);
        allCards[27] = new Card(8,"Ka","8", R.drawable.c8karo);
        allCards[28] = new Card(9,"Ka","9", R.drawable.c9karo);
        allCards[29] = new Card(10,"Ka","10", R.drawable.c10karo);
        allCards[30] = new Card(10,"Ka","11", R.drawable.c11karo);
        allCards[31] = new Card(10,"Ka","12", R.drawable.c12karo);
        gs.setUpCards(allCards);
    }

    private void setUpOwnCards(int i){
        //Log.d(TAG,"SetUpOwnCards i: " + i);
        if(ownCards[0]==null) {
            ownCards[0] = allCards[i];
            card1.setBackground(getDrawable(ownCards[0].getDrawableID()));
            store();
        }
        else if(ownCards[1]==null) {
            ownCards[1] = allCards[i];
            card2.setBackground(getDrawable(ownCards[1].getDrawableID()));
            store();
        }
        else if(ownCards[2]==null) {
            ownCards[2] = allCards[i];
            card3.setBackground(getDrawable(R.drawable.cback));
            store();
        }
        else if(ownCards[3]==null) {
            ownCards[3] = allCards[i];
            card1.setBackground(getDrawable(R.drawable.cback));
            card2.setBackground(getDrawable(R.drawable.cback));
            card4.setBackground(getDrawable(R.drawable.cback));
            step = STEP.PAUSE;
            store();
           // setUp = false;
            Log.d(TAG,"SetupOwnCards angeschlossen:"+step.toString());
            startTurn(gs.getTurn());
        }
    }

    private void setUpListener(){
        db.collection("CardDeck").document(sessionID).collection("Player")
                //.whereEqualTo("Gamestate", "CA")
                //.document("Gamestate")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        //Log.d(TAG, "Änderung gefunden starte pull");

                        gs.pull(1);
                        gs.pull(2);
                        gs.pull(3);
                        gs.pull(4);
                        /*
                        int j = 1;
                            if(j!=player){
                                //Log.d(TAG, "player: " + player + "j: " +j);
                                for(int i = 0; i<4; ++i) {
                                    if (player1[i] != null)
                                        getImageView(j, i+1).setImageDrawable(player1[i].getDrawable());
                                }
                                ++j;
                            }
                            if(j!=player){
                                //Log.d(TAG, "player: " + player + "j: " +j);
                                for(int i = 0; i<4; ++i) {
                                    if (player2[i] != null)
                                        getImageView(j, i+1).setImageDrawable(player2[i].getDrawable());
                                }
                                ++j;
                            }
                            if(j!=player){
                                //Log.d(TAG, "player: " + player + "j: " +j);
                                for(int i = 0; i<4; ++i) {
                                    if (player3[i] != null)
                                        getImageView(j, i+1).setImageDrawable(player3[i].getDrawable());
                                }
                                ++j;
                            }
                            if(j!=player){
                                //Log.d(TAG, "player: " + player + "j: " +j);
                                for(int i = 0; i<4; ++i) {
                                    if (player4[i] != null)
                                        getImageView(j, i+1).setImageDrawable(player4[i].getDrawable());
                                }
                                ++j;


                            }

                         */
                       // if(gs.getDroped()!=null)
                       //     btnTakeDropedCard.setBackground(gs.getDroped().getDrawable());
                        //if(gs.getTmpCard()!=null)
                        //    imageViewTmp.setImageDrawable(gs.getTmpCard().getDrawable());
                    }
                });
    }

    private ImageView getImageView(int row, int cardNumber){
        Log.d(TAG,"get ImageView:"+ row+"/"+cardNumber);
        switch (row+"/"+cardNumber){
            case("1/1"): return x1;
            case("1/2"): return x2;
            case("1/3"): return x3;
            case("1/4"): return x4;
            case("2/1"): return y1;
            case("2/2"): return y2;
            case("2/3"): return y3;
            case("2/4"): return y4;
            case("3/1"): return z1;
            case("3/2"): return z2;
            case("3/3"): return z3;
            case("3/4"): return z4;
            default:
                throw new IllegalStateException("Unexpected value: " + row + "/" + cardNumber);
        }
    }

    public static void setSessionID(String id){
        sessionID = id;
        Log.d(TAG,"XXXXX Session ID gesetzt: "+sessionID);
    }

    private void getPlayerID(){
        final DocumentReference docRef = db.collection("CardDeck").document(sessionID).collection("Player").document("PlayerDoc");
        // Log.d(TAG, "Player DocID"+docRef.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data for player ID: " + document.getData());
                        if (document.get("p1")!=null && document.get("p1").toString().equals(playerName))
                            player = 1;
                        else if (document.get("p2")!=null && document.get("p2").toString().equals(playerName))
                            player = 2;
                        else if (document.get("p3")!=null && document.get("p3").toString().equals(playerName))
                            player = 3;
                        else if (document.get("p4")!=null && document.get("p4").toString().equals(playerName))
                                player = 4;
                        else if(document.get("p1")!=null && document.get("p1").toString().equals("p1"))
                            getPlayerIDHelper(1,docRef);
                        else if(document.get("p2")!=null && document.get("p2").toString().equals("p2"))
                            getPlayerIDHelper(2,docRef);
                        else if(document.get("p3")!=null && document.get("p3").toString().equals("p3"))
                            getPlayerIDHelper(3,docRef);
                        else if(document.get("p4")!=null && document.get("p4").toString().equals("p4"))
                            getPlayerIDHelper(4,docRef);
                        else {
                            Log.d(TAG, "XX  Session full ");
                            Intent intent = new Intent(Client.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Die gewählte Session ist bereits voll", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getPlayerIDHelper(int player, DocumentReference docRef){
        this.player = player;
        Map<String,Object> updates = new HashMap<>();
        updates.put("p"+player, playerName);
        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            // [START_EXCLUDE]
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
            // [START_EXCLUDE]
        });
        Log.d(TAG, "player gesetzt auf: "+ this.player);
        gs.setClient(this.player,this);
    }

    private void getCardFromDeck(){
        final DocumentReference docRef = db.collection("CardDeck").document(sessionID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "OnID  DocumentSnapshot data: " + document.getData());
                        if(document.getData().toString().length()>7) {
                            String nextFieldName = document.getData().toString().substring(1, 8);
                            String s = document.get(nextFieldName).toString();
                            Log.d(TAG, "Karte gezogen" + s);
                            for (int i = 0; i < allCards.length; ++i) {
                                if (allCards[i].getIdentifier().equals(s)) {
                                    tmpCard = new Card(allCards[i]);
                                    //Log.d(TAG,"tmpCard in Methode"+tmpCard.getIdentifier());
                                    //if(setUp){
                                    //    setUpOwnCards(i);
                                    //}
                                    if(step == STEP.SETUP){ setUpOwnCards(i);}
                                    else { imageViewTmp.setImageDrawable(getDrawable(tmpCard.getDrawableID())); }
                                    deleteCardFromDeck(docRef, nextFieldName);
                                }
                            }
                        }
                        else{
                            //Stapel leer!!!
                            Log.d(TAG, "XXX Stapel leer!");
                        }
                    } else {
                        Log.d(TAG, "OnID  No such document");
                    }
                } else {
                    Log.d(TAG, "OnID  get failed with ", task.getException());
                }
            }
        });
    }

    public void deleteCardFromDeck(DocumentReference docRef, String fieldName) {
        // [START update_delete_field]
        //DocumentReference docRef = db.collection("CardDeck").document(sessionID);
        Map<String,Object> updates = new HashMap<>();
        updates.put(fieldName, FieldValue.delete());
        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            // [START_EXCLUDE]
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
        });
        // [END update_delete_field]
    }

    private void openCard(Button card, int ownCardIndex){
        if(step!=STEP.SETUP) {
            if (swapIT) {
                Card old = new Card(ownCards[ownCardIndex]);
                Log.d(TAG, "tausche: " + tmpCard.getIdentifier() + " <=> " + old.getIdentifier());
                ownCards[ownCardIndex] = tmpCard;
                card.setBackground(getDrawable(ownCards[ownCardIndex].getDrawableID()));
                btnTakeDropedCard.setBackground(getDrawable(old.getDrawableID()));
                tmpCard = null;
                imageViewTmp.setImageDrawable(null);
                gs.updatePlayer(old,player,ownCards[ownCardIndex],ownCardIndex);
            } else {
                card.setBackground(getDrawable(ownCards[ownCardIndex].getDrawableID()));
                gs.updatePlayer(gs.getDroped(),player,ownCards[ownCardIndex],ownCardIndex);
            }
            store();
        }
    }

    public void startTurn(int turn) {
        Log.d(TAG, "start turn, player:" + this.player + " turn: " +turn);
        if(turn == this.player&&step!=STEP.SETUP) {
            if(step == STEP.PAUSE)
                step = STEP.SELECT;
            Toast.makeText(getApplicationContext(),"Du bist jetzt an der Reihe",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "start turn mit step:" + step.toString());
        }
    }

    private enum STEP {
        SELECT, CHOOSE, LAY_DOWN, PAUSE, SETUP
    }

    private boolean select(){
        switch (step){
            case SETUP:
                return true;
            case SELECT:
                step = STEP.CHOOSE;
                return true;
            case CHOOSE:
                Toast.makeText(getApplicationContext(),"Du hast bereits eine Karte gezogen, wähle Throw oder Take",Toast.LENGTH_SHORT).show();
                return false;
            case LAY_DOWN:
                Toast.makeText(getApplicationContext(),"Du hast bereits eine Karte ausgewählt, wähle die Karte die du Aufdecken möchtest",Toast.LENGTH_SHORT).show();
                return false;
            case PAUSE:
                Toast.makeText(getApplicationContext(),"Du hast Pause, warte bis die anderen Spieler ihre züge beendet haben",Toast.LENGTH_SHORT).show();
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + step);
        }
    }

    private boolean choose() {
        switch (step) {
            case CHOOSE:
                step = STEP.LAY_DOWN;
                return true;
            case LAY_DOWN:
                Toast.makeText(getApplicationContext(), "bitte wähle erst ob du die Karte behalten oder wegwerfen willst", Toast.LENGTH_SHORT).show();
                return false;
            case SELECT: case SETUP:
                Toast.makeText(getApplicationContext(), "Du hast noch keine Karte gezogen", Toast.LENGTH_SHORT).show();
                return false;
            case PAUSE:
                Toast.makeText(getApplicationContext(),"Du hast Pause, warte bis die anderen Spieler ihre züge beendet haben",Toast.LENGTH_SHORT).show();
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + step);
        }
    }

    private boolean layDown(){
        switch (step) {
            case LAY_DOWN:
                step = STEP.PAUSE;
                return true;
            case CHOOSE:
            case SELECT:
            case SETUP:
                Toast.makeText(getApplicationContext(), "wähle die Karte die du Tauschen/öffnen willst", Toast.LENGTH_SHORT).show();
                return false;
            case PAUSE:
                Toast.makeText(getApplicationContext(),"Du hast Pause, warte bis die anderen Spieler ihre züge beendet haben",Toast.LENGTH_SHORT).show();
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + step);
        }
    }

    public void setDroped(Card droped){
        Log.d(TAG,"setDroped");
        btnTakeDropedCard.setBackground(getDrawable(droped.getDrawableID()));
    }

    public void setTmpCard(Card tmpCard){
        if(tmpCard!=null) {
            tmpCard = tmpCard;
            imageViewTmp.setImageDrawable(getDrawable(tmpCard.getDrawableID()));
        }else{
            tmpCard = null;
            imageViewTmp.setImageDrawable(null);
        }
    }

    public void setScore(int scoreX, int scoreY, int scoreZ){
        textViewScoreX.setText(playerNameX +": " + scoreX);
        textViewScoreY.setText(playerNameY +": " + scoreY);
        textViewScoreZ.setText(playerNameZ +": " + scoreZ);
    }

    public void setScoreTotal(int scoreX, int scoreY, int scoreZ){
        textViewScoreX2.setText("Total: " + ( Integer.parseInt(textViewScoreX2.getText().toString().substring(7)) + scoreX));
        textViewScoreY2.setText("Total: " + ( Integer.parseInt(textViewScoreY2.getText().toString().substring(7)) + scoreY));
        textViewScoreZ2.setText("Total: " + ( Integer.parseInt(textViewScoreZ2.getText().toString().substring(7)) + scoreZ));
    }

    public void setPlayerNames(String nameX, String nameY, String nameZ){
        Log.d(TAG,"setPlayerNames"+nameX+nameY+nameZ);
        playerNameX = nameX;
        playerNameY = nameY;
        playerNameZ = nameZ;
        textViewScoreX.setText(playerNameX+": ");
        textViewScoreY.setText(playerNameY+": ");
        textViewScoreZ.setText(playerNameZ+": ");
    }

    public void setOtherPlayersOwnCards(Card[] x, Card[] y, Card[] z){ // other Player dient muss noch zugewiesen werden 1-3,
        Log.d(TAG,"setOtherPlayersOwnCards: "+ player);
        for(int i = 0; i<4; ++i){
            if(x[i]!=null)getImageView(1, i + 1).setImageDrawable(getDrawable(x[i].getDrawableID()));
            if(y[i]!=null)getImageView(2, i + 1).setImageDrawable(getDrawable(y[i].getDrawableID()));
            if(z[i]!=null)getImageView(3, i + 1).setImageDrawable(getDrawable(z[i].getDrawableID()));
        }
    }

    public void reset(){
        if(host){
            String[] cardDeck = {"Kz7","Kz8","Kz9","Kz10","Kz11","Kz12","Kz0","Kz1",
                    "Ka7","Ka8","Ka9","Ka10","Ka11","Ka12","Ka0","Ka1",
                    "Hz7","Hz8","Hz9","Hz10","Hz11","Hz12","Hz0","Hz1",
                    "Pi7","Pi8","Pi9","Pi10","Pi11","Pi12","Pi0","Pi1"};
            Collections.shuffle(Arrays.asList(cardDeck));
            Map<String, Object> cardDeckOnline = new HashMap<>();
            // erste Karte wird automatisch übersprungen und zu droped card
            for(int i = 1; i < cardDeck.length; ++i) {
                cardDeckOnline.put("Card"+(i+100), cardDeck[i]);
            }
            db.collection("CardDeck").document(sessionID).update(cardDeckOnline).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
            Map<String,Object> game = new HashMap<>();
            game.put("p1","");
            game.put("p2","");
            game.put("p3","");
            game.put("p4","");
            game.put("droped",cardDeck[0]);
            game.put("turn",1);
            game.put("tmp","");
            db.collection("CardDeck").document(sessionID).collection("Player").document("Gamestate").update(game);
        }
        tmpCard = null;
        ownCards[0] = null;
        ownCards[1] = null;
        ownCards[2] = null;
        ownCards[3] = null;
        imageViewTmp.setImageDrawable(null);
        x1.setImageDrawable(getDrawable(R.drawable.cback));
        x2.setImageDrawable(getDrawable(R.drawable.cback));
        x3.setImageDrawable(getDrawable(R.drawable.cback));
        x4.setImageDrawable(getDrawable(R.drawable.cback));
        y1.setImageDrawable(getDrawable(R.drawable.cback));
        y2.setImageDrawable(getDrawable(R.drawable.cback));
        y3.setImageDrawable(getDrawable(R.drawable.cback));
        y4.setImageDrawable(getDrawable(R.drawable.cback));
        z1.setImageDrawable(getDrawable(R.drawable.cback));
        z2.setImageDrawable(getDrawable(R.drawable.cback));
        z3.setImageDrawable(getDrawable(R.drawable.cback));
        z4.setImageDrawable(getDrawable(R.drawable.cback));
        card1.setBackground(null);
        card2.setBackground(null);
        card3.setBackground(null);
        card4.setBackground(null);
        btnTakeDropedCard.setBackground(getDrawable(R.drawable.cback));
        step = STEP.SETUP;
    }

    private void store(){
        Log.d(TAG,"store");
        for(int i = 0; i<4 ; ++i){
            if(ownCards[i]!=null) {
                prefEditor.putString("OwnCards" + i, ownCards[i].getIdentifier());
            }else{
                prefEditor.putString("OwnCards" + i, "");
            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(step);
        prefEditor.putString("Step",json);
        prefEditor.putString("LastSessionID", sessionID);
        prefEditor.commit();
    }
    private void readStorage(){
        Log.d(TAG,"ReadStorage");
        if((!sharedPref.getString("LastSessionID","").equals(""))&&sharedPref.getString("LastSessionID","").equals(sessionID)) {
            gs.setClient(player,this);
            for(int i = 0; i<ownCards.length; ++i) {
                String s = sharedPref.getString("OwnCards" + i,"");
                Log.d(TAG,"readStorage s:" +s);
                if(s != ""){
                    for(int j = 0; j<allCards.length; ++j){
                        if(s.equals(allCards[j].getIdentifier())) {
                            ownCards[i] = allCards[j];
                            Log.d(TAG,"Karte eingelesen:" +i+ "=>" + allCards[j].getIdentifier());
                        }
                    }
                }
            }
            if(ownCards[0]!=null) {
                card1.setBackground(getDrawable(R.drawable.cback));
                if(ownCards[3]==null)
                    card1.setBackground(getDrawable(ownCards[0].getDrawableID()));
            }
            if(ownCards[1]!=null) {
                card2.setBackground(getDrawable(R.drawable.cback));
                if(ownCards[3]==null)
                    card2.setBackground(getDrawable(ownCards[1].getDrawableID()));
            }
            if(ownCards[2]!=null)
                card3.setBackground(getDrawable(R.drawable.cback));
            if(ownCards[3]!=null) {
                card4.setBackground(getDrawable(R.drawable.cback));
            }
            gs.pull(1);
            gs.pull(2);
            gs.pull(3);
            gs.pull(4);
            Gson gson = new Gson();
            String json = sharedPref.getString("Step","");
            step = gson.fromJson(json,STEP.class);
            //set Names
            //setNumber of players
            //start Turn
        }
    }
}
