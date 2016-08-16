package com.chni.cardiochek.adapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
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
import java.util.zip.Inflater;

/**
 * Created by JerryYin on 8/4/16.
 * 服务适配器
 */
public class ServiceAdapter extends BaseAdapter {

    private List<BluetoothGattService> mServiceList;
    private Context mContext;


    public ServiceAdapter(List<BluetoothGattService> mServiceList, Context mContext) {
        this.mServiceList = mServiceList;
        this.mContext = mContext;
    }

    public BluetoothGattService getService(int position){
        if (mServiceList.size()>0)
            return mServiceList.get(position);
        return null;
    }

    public void addService(BluetoothGattService service) {
        if(!mServiceList.contains(service)) {
            mServiceList.add(service);
        }
    }

    static class ViewHolder {
        TextView service;
        TextView uuid;
        TextView type;
    }

    @Override
    public int getCount() {
        return mServiceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mServiceList.get(position);
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
            viewHolder.service = (TextView) view.findViewById(R.id.service_id);
            viewHolder.uuid = (TextView) view.findViewById(R.id.service_uuid);
            viewHolder.type = (TextView) view.findViewById(R.id.service_type);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothGattService service = mServiceList.get(position);
        String serviceId = "服务" + String.valueOf(service.getInstanceId());
        UUID uuid = service.getUuid();
        int type = service.getType();
        viewHolder.service.setText(serviceId);
        viewHolder.uuid.setText(uuid.toString());
        viewHolder.type.setText(String.valueOf(type));

        return view;
    }
}
