package com.example.banhmi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class Rules extends AppCompatActivity {
    TextView textViewRules;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        setUpViews();
    }

    private void setUpViews(){
        textViewRules = findViewById(R.id.textViewRules);
        textViewRules.setText("Regelwerk: /n Ziel ist es pro Runde möglichst wenig Strafpunkte zu Sammeln. Jede Karte gibt den angezeigten Wert als Strafpunkt " +
                        "(7=7p,...10=10p,B=1ßp;D=10p), Der König und das Ass geben weniger Punkte (K=0p,A=1p). Bildet der Spieler ein Pärchen, ergibt dieses ebenfalls 0 Punkte. "+
                "Der Spieler welcher zuerst 50 Punkte erreicht verliert das Spiel. /n"+
                "Spielverlauf: /n"+
                        "es werden zu Beginn 4 Karten gezogen. 2 Davon dürfen angeschaut werden, danach werden sie wieder verdeckt, dann beginnt der erste Spieler mit seinem Zug /n"+
                "Zug: /n"+
                "Der Spieler zieht eine Karte von einem der beiden Stapel /n"+
                "Er wählt ob er die Karte behalten will /n"+
                "Behält er die Karte, tauscht er diese mit einer eigenen, noch verdeckten Karte /n"+
                "Ende des Zuges");
    }

}
