package com.chni.cardiochek.service;

import android.view.View;

/**
 * Created by JerryYin on 8/5/16.
 */
public interface onBLEItemClickListener extends View.OnClickListener {

    void onClick(View v, int position, int group);

}
