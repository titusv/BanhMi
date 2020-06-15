package com.example.banhmi;

import android.graphics.drawable.Drawable;
import android.media.Image;

import androidx.annotation.DrawableRes;

public class Card {
    private int value;
    private String colour;
    private String number;
    private int drawableID;

    public Card(int value, String colour, String number, int drawableID){
        this.value = value;
        this.colour = colour;
        this.number = number;
        this.drawableID = drawableID;
    }

    public Card (Card card){
        this.value = card.getValue();
        this.colour = card.getColour();
        this.number = card.getNumber();
        this.drawableID = card.getDrawableID();
    }

    public int getValue(){ return value;}

    public String getColour(){ return colour;}

    public String getNumber(){ return number;}

    public int getDrawableID() { return drawableID; }

    public  String getIdentifier(){ return colour+number; }
}
