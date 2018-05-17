package com.zxhl.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Administrator on 2018/1/17.
 */

public class ShowKeyboard {
    public static void hideKeyboard(View v){
        InputMethodManager input= (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input.isActive()) {
            input.hideSoftInputFromWindow( v.getApplicationWindowToken() , 0 );
        }
    }
}
