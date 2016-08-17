package com.chni.cardiochek.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.chni.cardiochek.R;
import com.chni.cardiochek.adapter.ExpandableListViewAdapter;
import com.chni.cardiochek.service.BluetoothLeService;
import com.chni.cardiochek.view.CustomExpandableListView;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity implements View.OnClickListener {
    private final static String TAG = DeviceControlActivity.class
            .getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private static final int REFRESH_SERVICE = 0x01;

    private TextView mConnectionState;
    private TextView mDataField;
    private Button mBtnSend;
    private EditText mTxtMsg;
    private CustomExpandableListView mListView;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    public static BluetoothGatt mBluetoothGatt;
    public static BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    //    private ServiceAdapter mServiceAdapter;
    private List<BluetoothGattService> mServiceList = new ArrayList<>();
    private ExpandableListViewAdapter mExpandableListViewAdapter;

    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a result of read or notification operations.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBluetoothLeService.enableTXNotification();
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                try {
//                    displayData(new String(data, "UTF-8"));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

//        java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mTxtMsg = (EditText) findViewById(R.id.edit_text_msg);

        // 显示当前服务
        mListView = (CustomExpandableListView) findViewById(R.id.list_service);
//        mServiceAdapter = new ServiceAdapter(mServiceList, this);
        mExpandableListViewAdapter = new ExpandableListViewAdapter(mServiceList, this);
        mListView.setAdapter(mExpandableListViewAdapter);
//        mListView.setOnItemClickListener(this);


        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean bll = bindService(gattServiceIntent, mServiceConnection,
                BIND_AUTO_CREATE);
        if (bll) {
            Log.d(TAG, "---------------");
        } else {
            Log.d(TAG, "===============");
        }

//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String s = BodyCHOLRead(sb.toString());
//                Log.d(TAG, "onClick: 结果"+s);
//            }
//        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mServiceList.clear();
//                mServiceAdapter.notifyDataSetChanged();
                mExpandableListViewAdapter.notifyDataSetChanged();
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    StringBuffer sb = new StringBuffer();

    private void displayData(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sb.append(data);
                Log.d(TAG, "displayData: " + sb.toString());
                mDataField.setText(data);
                Toast.makeText(getApplicationContext(), "data = " + data, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.d(TAG, "displayGattServices: " + uuid);
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
//                if (uuid.contains("fff4")) {
                Log.d(TAG, "cha_uuid = " + uuid);
//                if (uuid.equals(BluetoothLeService.TX_CHAR_UUID)) {
////                    Log.d("console", "2gatt Characteristic: " + uuid);
//                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
//////                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
//                }

            }
        }

        List<BluetoothGattService> serviceList = mBluetoothLeService.getSupportedGattServices();
        List<List<BluetoothGattCharacteristic>> chaLists = new ArrayList<>();
        for (BluetoothGattService s : gattServices) {
            mExpandableListViewAdapter.addService(s);
            List<BluetoothGattCharacteristic> characteristicList = s.getCharacteristics();
            Log.d(TAG, "charac.size = : " + characteristicList.size());
            chaLists.add(characteristicList);
        }
        mExpandableListViewAdapter.addChildList(chaLists);
        mExpandableListViewAdapter.notifyDataSetChanged();
    }

    /**
     * 广播过滤器、
     * 处理服务所激发的各种事件
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);   //连接一个GATT服务
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);    //从GATT服务中断开连接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED); //查找GATT服务
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);   //从服务中接受数据
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        return intentFilter;
    }

    /**
     * 将16进制 转换成10进制
     *
     * @param str
     * @return
     */
    public static String print10(String str) {

        StringBuffer buff = new StringBuffer();
        String array[] = str.split(" ");
        for (int i = 0; i < array.length; i++) {
            int num = Integer.parseInt(array[i], 16);
            buff.append(String.valueOf((char) num));
        }
        return buff.toString();
    }

    /**
     * byte转16进制
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }


    /**
     * 分析胆固醇数据
     *
     * @param data
     * @return
     */
    public static String BodyCHOLRead(String data) {
        // 根据换行符分割
        String[] datas = data.split(print10("0A"));
        for (int i = 0; i < datas.length; i++) {
            Log.d(TAG, String.format("split[%s]:%s", i, datas[i]));
        }
        String unit = "";
        String data7 = datas[7].split("\"")[1].split(":")[1].trim();
        if (data7.contains("mmol/L")) {
            unit = "mmol/L";
        }

        StringBuilder sbr = new StringBuilder();
        for (int i = 7, j = 0; i < 11; i++, j++) {
            String values = datas[i].split("\"")[1].split(":")[1].trim();//207 mg/dL
            String[] results = values.split(" +");
            System.out.println("值~~~~~" + values + "分割长度:" + results.length);
            String value = "----";

            if (results.length == 3) {
                sbr.append(results[0]);
                value = results[1];
            } else if (results.length == 2) {
                value = results[0];
            }

            if ("----".equals(value)) {
                sbr.append(value).append(",");
            } else if (i != 11 && "mg/dl".equals(unit)) {
                sbr.append(unitConversion(value, j)).append(",");
            } else if (i != 11 && "mmol/L".equals(unit)) {
                sbr.append(value).append(",");
            } else if (i != 11 && "g/L".equals(unit)) {
                sbr.append(unitConversion(String.valueOf(Double.parseDouble(value) * 100), j)).append(",");
            } else {
                sbr.append(value).append(",");
            }
        }
        Log.d(TAG, "血脂4项测量结果:" + sbr);
        return sbr.substring(0, sbr.length() - 1);
    }

    private static String unitConversion(String input, int type) {
        double value = Double.parseDouble(input);
        NumberFormat df = NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(2);
        //*胆固醇、高密度脂蛋白、低密度脂蛋白的换算都一样：1mmol/L=38.7mg/dL；
        //*甘油三脂是1mmol/L=88.6mg/dL

        if (type == 0) {
            return df.format(value / 38.7);
        }
        if (type == 1) {
            return df.format(value / 88.6);
        }
        if (type == 2) {
            return df.format(value / 38.7);
        }
        if (type == 3) {
            return df.format(value / 38.7);
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message = mTxtMsg.getText().toString();
                    //                        Message msg = new Message();
//                        msg.what = 1;
//                        Handler.sendMessage(msg);
                    // send data to service
                    mBluetoothLeService.writeRXCharacteristic(message);
                }
            }).start();
        }
    }

    /**
     * service 单击
     * @param parent
     * @param view
     * @param position
     * @param id
     */
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        BluetoothGattService service = mServiceList.get(position);
//        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//        for (BluetoothGattCharacteristic characteristic : characteristicList){
//            Log.d(TAG, "------characteristic-------");
//            Log.d(TAG, "id = "+ characteristic.getInstanceId());
//            Log.d(TAG, "uuid = "+ characteristic.getUuid().toString());
//            Log.d(TAG, "permissions = "+ characteristic.getPermissions());
//            Log.d(TAG, "properties = "+ characteristic.getProperties());
//            Log.d(TAG, "writeType = "+ characteristic.getWriteType());
//
//
//            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
//            for (BluetoothGattDescriptor descriptor: descriptorList){
//                Log.d(TAG, "---descriptors---");
//                Log.d(TAG, "permission = "+ descriptor.getPermissions());
//                Log.d(TAG, "uuid = "+ descriptor.getUuid().toString());
//                Log.d(TAG, "value = "+ descriptor.getValue().toString());
//
//            }
//        }

//    }


//    private Handler mHandle = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case REFRESH_SERVICE:
//                    mServiceList = mBluetoothLeService.getSupportedGattServices();
//                    mServiceAdapter.notifyDataSetChanged();
//                    break;
//            }
//        }
//    };
}
