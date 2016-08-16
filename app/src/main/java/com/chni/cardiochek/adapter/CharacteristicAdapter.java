package com.chni.cardiochek.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chni.cardiochek.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by JerryYin on 8/5/16.
 */
public class CharacteristicAdapter extends BaseAdapter{

    private List<BluetoothGattCharacteristic> mCharacteristicList;
    private Context mContext;


    public CharacteristicAdapter(List<BluetoothGattCharacteristic> mCharacteristicList, Context mContext) {
        this.mCharacteristicList = mCharacteristicList;
        this.mContext = mContext;
    }


    public BluetoothGattCharacteristic getCharacteristic(int position){
        return mCharacteristicList.get(position);
    }

    public void addCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(!mCharacteristicList.contains(characteristic)) {
            mCharacteristicList.add(characteristic);
        }
    }

    static class ViewHolder {
        TextView characteristicId;
        TextView uuid;
        TextView readable;
    }

    @Override
    public int getCount() {
        return mCharacteristicList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCharacteristicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_service, null);
            viewHolder = new ViewHolder();
            viewHolder.characteristicId = (TextView) view.findViewById(R.id.service_id);
            viewHolder.uuid = (TextView) view.findViewById(R.id.service_uuid);
            viewHolder.readable = (TextView) view.findViewById(R.id.service_type);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothGattCharacteristic characteristic = mCharacteristicList.get(position);
        String chId = "特征" + String.valueOf(characteristic.getInstanceId());
        UUID uuid = characteristic.getUuid();
        int type = characteristic.getProperties();
        viewHolder.characteristicId.setText(chId);
        viewHolder.uuid.setText(uuid.toString());
        viewHolder.readable.setText(String.valueOf(type));
        return view;
    }
}
