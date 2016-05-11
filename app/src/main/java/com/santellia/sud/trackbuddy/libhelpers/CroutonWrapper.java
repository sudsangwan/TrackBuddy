package com.santellia.sud.trackbuddy.libhelpers;

import android.app.Activity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Sudarshan on 11/03/16.
 */
public class CroutonWrapper {
    Activity activity;

    //constructor
    public CroutonWrapper(Activity ac) {

        this.activity = ac;
    }

    public void croutonAlert(int stringId){
        Crouton.makeText(activity, stringId, Style.ALERT).show();
    }

    public void croutonAlert(String text){
        Crouton.makeText(activity, text, Style.ALERT).show();
    }

    public void croutonInfo(int stringId){
        Crouton.makeText(activity, stringId, Style.INFO).show();
    }

    public void croutonInfo(String text){
        Crouton.makeText(activity, text, Style.INFO).show();
    }

    public void croutonConfirm(int stringId){
        Crouton.makeText(activity, stringId, Style.CONFIRM).show();
    }

    public void croutonConfirm(String text){
        Crouton.makeText(activity, text, Style.CONFIRM).show();
    }
}
