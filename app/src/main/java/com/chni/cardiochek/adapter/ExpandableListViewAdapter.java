package com.chni.cardiochek.adapter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chni.cardiochek.R;
import com.chni.cardiochek.activity.DeviceControlActivity;
import com.chni.cardiochek.service.BluetoothLeService;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by JerryYin on 8/5/16.
 */
public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "ExpandableListViewAdapter";
    private Context context;
    private List<BluetoothGattService> mHeadList;
    private List<List<BluetoothGattCharacteristic>> mChildList;

    private BluetoothGattService mCurService;
    private BluetoothGattCharacteristic mCurCharacteristic;


//    private class GroupHolder {
//        TextView textView;
//        ImageView imageView;
//    }
//
//    private class ChildHolder {
//        TextView textView;
//    }

    static class HeadViewHolder {
        TextView service;
        TextView uuid;
        TextView type;
    }

    static class ChildViewHolder {
        TextView characteristicId;
        TextView uuid;
        TextView property;
    }


    public ExpandableListViewAdapter(List<BluetoothGattService> mHeadList, Context context) {
        this.mHeadList = mHeadList;
        this.context = context;
    }

    public void addChildList(List<List<BluetoothGattCharacteristic>> childList) {
        mChildList = childList;
    }

    public void addService(BluetoothGattService s) {
        if (mHeadList.contains(s))
            return;
        mHeadList.add(s);
    }

    @Override
    public int getGroupCount() {
        return mHeadList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mHeadList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        HeadViewHolder groupHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_service, null);
            groupHolder = new HeadViewHolder();
            groupHolder.service = (TextView) convertView.findViewById(R.id.service_id);
            groupHolder.uuid = (TextView) convertView.findViewById(R.id.service_uuid);
            groupHolder.type = (TextView) convertView.findViewById(R.id.service_type);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (HeadViewHolder) convertView.getTag();
        }
        BluetoothGattService service = mHeadList.get(groupPosition);
        groupHolder.service.setText("服务" + String.valueOf(service.getInstanceId()));
        groupHolder.uuid.setText(service.getUuid().toString());
        groupHolder.type.setText(String.valueOf(service.getType()));
        return convertView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_characteristic, null);
            childHolder = new ChildViewHolder();
            childHolder.characteristicId = (TextView) convertView.findViewById(R.id.characteristic_id);
            childHolder.uuid = (TextView) convertView.findViewById(R.id.uuid);
            childHolder.property = (TextView) convertView.findViewById(R.id.readable);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildViewHolder) convertView.getTag();
        }
        final BluetoothGattCharacteristic characteristic = mChildList.get(groupPosition).get(childPosition);
        childHolder.characteristicId.setText("特征" + String.valueOf(characteristic.getInstanceId()));
        childHolder.uuid.setText(characteristic.getUuid().toString());
        childHolder.property.setText(String.valueOf(characteristic.getProperties()));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "------characteristic-------");
                Log.d(TAG, "id = " + characteristic.getInstanceId());
                Log.d(TAG, "uuid = " + characteristic.getUuid().toString());
                Log.d(TAG, "permissions = " + characteristic.getPermissions());
                Log.d(TAG, "properties = " + characteristic.getProperties());
                Log.d(TAG, "writeType = " + characteristic.getWriteType());
                Log.d(TAG, "value =" + characteristic.getValue());

                List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptorList) {
                    Log.d(TAG, "---descriptors---");
                    Log.d(TAG, "permission = " + descriptor.getPermissions());
                    Log.d(TAG, "uuid = " + descriptor.getUuid().toString());
                    Log.d(TAG, "value = " + descriptor.getValue());
                }

                final EditText editText = new EditText(context);
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setView(editText)
                        .setTitle("send data to ble :")
                        .setPositiveButton("send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String data = editText.getText().toString();
                                /**
                                 * 三个条件
                                 * 数据不为空
                                 * 特定服务
                                 * 特定特征
                                 */
                                if (!TextUtils.isEmpty(data) &&
                                        mHeadList.get(groupPosition).getUuid().equals(BluetoothLeService.RX_SERVICE_UUID) &&
                                            characteristic.getUuid().equals(BluetoothLeService.RX_CHAR_UUID)) {
                                    try {
                                        characteristic.setValue(data.getBytes("UTF-8"));

                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    if (DeviceControlActivity.mBluetoothLeService == null) {
                                        Toast.makeText(context, "service is null", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                DeviceControlActivity.mBluetoothLeService.writeCharacteristic(characteristic);
//                                                DeviceControlActivity.mBluetoothLeService.writeRXCharacteristic(data);
//                                DeviceControlActivity.mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//                                DeviceControlActivity.mBluetoothLeService.readCharacteristic(characteristic);
                                            }
                                        }.run();
                                    }
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(context, "data can not be null !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
