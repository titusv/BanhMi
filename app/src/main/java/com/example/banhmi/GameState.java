package com.example.banhmi;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class GameState {

    //private static GameState Instance;
    private Card[] player1,player2,player3,player4,allCards;
    private Card droped,tmpCard;
    private int turn, round, evalHelper = 0 ;
    private int scoreP1 = 0,scoreP2 = 0,scoreP3 = 0,scoreP4 = 0;
    private static final String TAG = "GAMESTATE";
    private FirebaseFirestore db;
    private String sessionID;
    private Client client;
    private int playerNumber;
    private int numberOfPlayers = 1;
    DocumentReference docRef;


    public GameState(String sessionID){
        db = FirebaseFirestore.getInstance();
        player1 = new Card[4];
        player2 = new Card[4];
        player3 = new Card[4];
        player4 = new Card[4];
        allCards = new Card[32];
        droped = new Card(0,null,null,0);
        tmpCard = new Card(0,null,null,0);
        this.sessionID = sessionID;
        docRef = db.collection("CardDeck").
                document(sessionID).collection("Player").document("Gamestate");
        // Number Of Players Setzen
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        numberOfPlayers = Integer.parseInt(document.get("NOP").toString());
                    }
                }
            }
        });
        setUpPlayerNames();
        turn = 1;
        round = 1;
    }

    public void setClient(int playerNumber, Client client) {
        //if(playerNumber<=4&&playerNumber>=1&&client!=null) {
        this.client = client;
        this.playerNumber = playerNumber;
        //return true;
    //}return false;
    }

    public void setDroped(Card droped) {
            this.droped = droped;
    }

    public void setUpCards(Card[] allCards){
        this.allCards = allCards;
    }

    public void updateTmp(Card tmpCard){
        HashMap<String, Object> updates = new HashMap();
        updates.put("tmp",tmpCard.getIdentifier());
        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) { }
        });
    }

    private String makeIdentString(Card[] player){
        String s="";
        for(int i = 0; i<player.length; ++i){
            if(player[i]!=null)
                s += player[i].getIdentifier() + "$";
            else
                s+="xxxx$";
        }
        //Log.d(TAG,"MakeIdentString: "+ s);
       return s;
    }

    public int getTurn(){
        if(turn>=numberOfPlayers*4+1) {
            evaluation();
            turn = round;
            ++round;
        }
        return (turn % numberOfPlayers)+1 ;
    }

    public void evaluation(){
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // für jeden spieler
                        int p1 = evaluationHelper(readInCardString(document.get("p1").toString()));
                        int p2 = evaluationHelper(readInCardString(document.get("p2").toString()));
                        int p3 = evaluationHelper(readInCardString(document.get("p3").toString()));
                        int p4 = evaluationHelper(readInCardString(document.get("p4").toString()));
                        String s1 ="";
                        String s2 ="";
                        String s3 ="";
                        String s4 ="";
                        resetGamestate();
                        switch (playerNumber){
                            case 1: client.setScore(p2,p3,p4); break;
                            case 2: client.setScore(p1,p3,p4); break;
                            case 3: client.setScore(p1,p2,p4); break;
                            case 4: client.setScore(p1,p2,p3); break;
                        }
                        if(evalHelper>0) {
                            evalHelper = 0;
                            switch (playerNumber){
                                case 1: client.setScoreTotal(p2,p3,p4); break;
                                case 2: client.setScoreTotal(p1,p3,p4); break;
                                case 3: client.setScoreTotal(p1,p2,p4); break;
                                case 4: client.setScoreTotal(p1,p2,p3); break;
                            }
                        }
                        Log.d(TAG,"evaluation p1"+ evaluationHelper(readInCardString(document.get("p1").toString())));
                        Log.d(TAG,"evaluation p2"+ evaluationHelper(readInCardString(document.get("p2").toString())));
                        Log.d(TAG,"evaluation p3"+ evaluationHelper(readInCardString(document.get("p3").toString())));
                        Log.d(TAG,"evaluation p4"+ evaluationHelper(readInCardString(document.get("p4").toString())));
                    }
                }
            }
        });
    }

    private String setUpPlayerNames(){
        DocumentReference pDocDocRef = db.document("CardDeck/"+sessionID+"/Player/PlayerDoc");
        pDocDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    String p1 = documentSnapshot.get("p1").toString();
                    String p2 = documentSnapshot.get("p2").toString();
                    String p3 = "";
                    String p4 = "";
                    if(numberOfPlayers>=3)
                        p3 = documentSnapshot.get("p3").toString();
                    if(numberOfPlayers>=4)
                        p4 = documentSnapshot.get("p4").toString();
                    switch (playerNumber){
                        case 1: client.setPlayerNames(p2,p3,p4);break;
                        case 2: client.setPlayerNames(p1,p3,p4);break;
                        case 3: client.setPlayerNames(p1,p2,p4);break;
                        case 4: client.setPlayerNames(p1,p2,p3);break;
                    }
                }
            }
        });
        return null;
    }

    private int evaluationHelper(Card[] tmpCards){
        int result = 0;
        for(int i = 0; i < 4; ++i){
            for(int j = 0;j < 4; ++j){
                if(tmpCards[i]!=null && tmpCards[j]!=null && i!=j && tmpCards[i].getNumber().equals(tmpCards[j].getNumber())){
                    tmpCards[i] = null;
                    tmpCards[j] = null;
                }
            }
            if(tmpCards[i]!=null)
                result += tmpCards[i].getValue();
        }
        return result;
    }

    private void resetGamestate(){
        Log.d(TAG,"resetGamestate");
        for(int i = 0; i<4; ++i) {
            player1[i] = null;
            player2[i] = null;
            player3[i] = null;
            player4[i] = null;
            tmpCard = null;
            droped = null;
            client.reset();
        }
    }

    private Card[] getArrayToInt(int player){
        switch (player){
            case(1):
                return player1;
            case(2):
                return player2;
            case(3):
                return player3;
            case(4):
                return player4;
        }
        return null;
    }

    public Card getDroped(){
        /*
        DocumentReference docRef = db.collection("CardDeck")
                .document(sessionID).collection("Player").document("Gamestate");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                (task.isSuccessful())
            }
        })
         */
        return droped;
    }

    private Card getCardFromAllCards(String identifier){
        for (int i = 0; i < allCards.length; ++i) {
            if (allCards[i].getIdentifier().equals(identifier)) {
                return allCards[i];
            }
        }
        Log.d(TAG,"Card "+ identifier + " not found");
        return null;
    }

    private Card[] readInCardString(String s){
        Card[] tmpCards = new Card[4];
        int itmp = 0;
        for (int i = 0; i < s.length() && itmp < 4; ++i) {
            if (s.charAt(i) == '$') {
                String sub = s.substring(0, i);
                //Log.d(TAG, "sub: " + sub);
                s = s.substring(i + 1);
                //Log.d(TAG, "s (substring): " + s);
                if(!sub.equals("xxxx")) {
                    for (int j = 0; j < allCards.length; ++j) {
                        if (allCards[j].getIdentifier().equals(sub)) {
                            tmpCards[itmp] = new Card(allCards[j]);
                        }
                    }
                }
                i = 0;
                ++itmp;
            }
        }
        return tmpCards;
    }

    private void read(final int player){
        if(client!=null) {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Log.d(TAG, "DocumentSnapshot data for Gamestate: " + document.getData());
                            //Log.d(TAG, "DocumentSnapshot data for Gamestate playerNumber: "+playerNumber+ " document: " + document.get("p"+playerNumber));
                            String s = document.get("p" + player).toString();
                            if (s.equals("")) {
                                //Log.d(TAG, "noch kein Eintrag vorhanden für playerNumber:" + playerNumber + " : "+s );
                            } else {
                                Card[] tmpCards = readInCardString(s);
                                // Log.d(TAG, "tmp String[]: " + tmpCard[0].getIdentifier() + tmpCard[1].getIdentifier() + tmpCard[2].getIdentifier() + tmpCard[3].getIdentifier());
                                switch (player) {
                                    case (1):
                                        player1 = tmpCards;
                                        break;
                                    case (2):
                                        player2 = tmpCards;
                                        break;
                                    case (3):
                                        player3 = tmpCards;
                                        break;
                                    case (4):
                                        player4 = tmpCards;
                                        break;
                                }
                            }
                            if (document.get("droped") != null) {
                                Log.d(TAG,"nehme Droped");
                                droped = getCardFromAllCards(document.get("droped").toString());
                                client.setDroped(droped);
                            }
                            if (document.get("tmp") != null && document.get("tmp") != "") {
                                tmpCard = getCardFromAllCards(document.get("tmp").toString());
                            }
                            switch (playerNumber) {
                                case 1:
                                    client.setOtherPlayersOwnCards(player2, player3, player4);
                                    break;
                                case 2:
                                    client.setOtherPlayersOwnCards(player1, player3, player4);
                                    break;
                                case 3:
                                    client.setOtherPlayersOwnCards(player1, player2, player4);
                                    break;
                                case 4:
                                    client.setOtherPlayersOwnCards(player1, player2, player3);
                                    break;

                            }
                            turn = Integer.parseInt(document.get("turn").toString());
                            client.startTurn(getTurn());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    public Card[] pull(int player){
        Log.d(TAG,"pull playerNumber: "+player);
        read(player);
        switch (player){
            case(1):
                return player1;
            case(2):
                return player2;
            case(3):
                return player3;
            case(4):
                return player4;
            default:
                throw new IllegalStateException("Unexpected value: " + player);
        }
    }

    public void updatePlayer(Card droped, int player, Card openedOwnCard, int index){
        switch (player){
            case(1):
                player1[index] = openedOwnCard;
                break;
            case(2):
                player2[index] = openedOwnCard;
                break;
            case(3):
                player3[index] = openedOwnCard;
                break;
            case(4):
                player4[index] = openedOwnCard;
                break;
            default:
                Log.d(TAG, "Fehler in updatePlayer methode");
        }
        ++turn;
        ++evalHelper;
        Map<String,Object> updates = new HashMap<>();
        updates.put("p"+player,makeIdentString(getArrayToInt(player)));
        updates.put("turn", turn);
        if(droped != null)
            updates.put("droped",droped.getIdentifier());
        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            // [START_EXCLUDE]
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
            // [START_EXCLUDE]
        });
    }

    public void setUpListener(){
        db.collection("CardDeck").document(sessionID).collection("Player")
                //.whereEqualTo("Gamestate", "CA")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                    }
                });
    }
}

// Notes: Nur ein Client wird benötigt pro Gamestate da dieser lokal bleibt. Es ist daher auch egal welcher client gesetzt wird
