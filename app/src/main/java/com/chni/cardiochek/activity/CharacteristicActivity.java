package com.chni.cardiochek.activity;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.widget.ListView;

import com.chni.cardiochek.R;
import com.chni.cardiochek.service.BluetoothLeService;

import java.util.List;

public class CharacteristicActivity extends Activity {


    private ListView mListView;


    private List<BluetoothGattCharacteristic> mCharacteristicList;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chracteristic);

        initViews();

    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.list_characteristic);


    }
}
