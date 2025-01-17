/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.fyp14017.hku/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.fyp14017.hku/license_additional_term
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.hku/licenses/>.
 *
 *    This file incorporates work covered by the following copyright and  
 *    permission notice: 
 *
 *		   	Copyright 2010 Guenther Hoelzl, Shawn Brown
 *
 *		   	This file is part of MINDdroid.
 *
 * 		  	MINDdroid is free software: you can redistribute it and/or modify
 * 		  	it under the terms of the GNU Affero General Public License as
 * 		  	published by the Free Software Foundation, either version 3 of the
 *   		License, or (at your option) any later version.
 *
 *   		MINDdroid is distributed in the hope that it will be useful,
 *   		but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   		GNU Affero General Public License for more details.
 *
 *   		You should have received a copy of the GNU Affero General Public License
 *   		along with MINDdroid.  If not, see <http://www.gnu.hku/licenses/>.
 */
package hku.fyp14017.blencode.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.stage.PreStageActivity;

import java.util.ArrayList;
import java.util.Set;

import hku.fyp14017.blencode.stage.PreStageActivity;

@SuppressLint("NewApi")
public class DeviceListActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    public static final String PAIRING = "pairing";
    public static final String AUTO_CONNECT = "auto_connect";
    public static final String DEVICE_NAME_AND_ADDRESS = "device_infos";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter btAdapter;
    private BluetoothGatt bg;
    private ArrayList<BluetoothDevice> newDevices, paired;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;
    private boolean autoConnect = true;
    private static ArrayList<String> autoConnectIDs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (autoConnectIDs.size() == 0) {

            autoConnectIDs.add(BtCommunicator.OUI_LEGO);
        }
        autoConnect = this.getIntent().getExtras().getBoolean(AUTO_CONNECT);
        //Log.i("bto", autoConnect + "");
        if (autoConnect) {
            this.setVisible(false);
        }
        newDevices = new ArrayList<BluetoothDevice>();
        paired = new ArrayList<BluetoothDevice>();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(hku.fyp14017.blencode.R.layout.device_list);
        setTitle("Select BLE device");

        setResult(Activity.RESULT_CANCELED);

        Button scanButton = (Button) findViewById(hku.fyp14017.blencode.R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
                view.setVisibility(View.GONE);
            }
        });

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, hku.fyp14017.blencode.R.layout.device_name);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, hku.fyp14017.blencode.R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(hku.fyp14017.blencode.R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(pairedDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(hku.fyp14017.blencode.R.id.new_devices);
        newDevicesListView.setAdapter(newDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(deviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            findViewById(hku.fyp14017.blencode.R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                paired.add(device);
            }
        }

        if (pairedDevices.size() == 0) {
            String noDevices = getResources().getText(hku.fyp14017.blencode.R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
        this.setVisible(true);
		/*
		 * if (autoConnect) {
		 * // String info = ((TextView) v).getText().toString();
		 * // if (info.lastIndexOf('-') != info.length() - 18) {
		 * // return;
		 * // }
		 * 
		 * btAdapter.cancelDiscovery();
		 * Intent intent = new Intent();
		 * Bundle data = new Bundle();
		 * data.putString(DEVICE_NAME_AND_ADDRESS, legoNXT.getName() + "-" + legoNXT.getAddress());
		 * data.putString(EXTRA_DEVICE_ADDRESS, legoNXT.getAddress());
		 * data.putBoolean(PAIRING, false);
		 * data.putBoolean(AUTO_CONNECT, true);
		 * intent.putExtras(data);
		 * setResult(RESULT_OK, intent);
		 * finish();
		 * // this.setVisible(false);
		 * } else {
		 * this.setVisible(true);
		 * }
		 * autoConnect = true;
		 */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }
    }

    private void doDiscovery() {

        setProgressBarIndeterminateVisibility(true);
        setTitle(hku.fyp14017.blencode.R.string.scanning);

        findViewById(hku.fyp14017.blencode.R.id.title_new_devices).setVisibility(View.VISIBLE);

        btAdapter.startDiscovery();
    }

    private OnItemClickListener deviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {

            String info = ((TextView) view).getText().toString();
            if (info.lastIndexOf('-') != info.length() - 18) {
                return;
            }

            btAdapter.cancelDiscovery();

            String address = info.substring(info.lastIndexOf('-') + 1);
            BluetoothDevice sensorTag = newDevices.get(arg2);
            PreStageActivity.sensorTag = sensorTag;
            PreStageActivity.bleDeviceName = sensorTag.getName();

            DeviceListActivity.this.unregisterReceiver(receiver);

            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(DEVICE_NAME_AND_ADDRESS, info);
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING, av.getId() == hku.fyp14017.blencode.R.id.new_devices);
            data.putBoolean(AUTO_CONNECT, false);
            intent.putExtras(data);
            setResult(RESULT_OK, intent);
            finish();

        }
    };

    private OnItemClickListener pairedDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {

            String info = ((TextView) view).getText().toString();
            if (info.lastIndexOf('-') != info.length() - 18) {
                return;
            }

            btAdapter.cancelDiscovery();

            String address = info.substring(info.lastIndexOf('-') + 1);
            BluetoothDevice sensorTag = paired.get(arg2);
            PreStageActivity.sensorTag = sensorTag;
            PreStageActivity.bleDeviceName = sensorTag.getName();

            DeviceListActivity.this.unregisterReceiver(receiver);

            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(DEVICE_NAME_AND_ADDRESS, info);
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING, av.getId() == hku.fyp14017.blencode.R.id.new_devices);
            data.putBoolean(AUTO_CONNECT, false);
            intent.putExtras(data);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ((device.getBondState() != BluetoothDevice.BOND_BONDED)) {
                    //Log.d("yathu", device.getName());
                    if(device.getName() != null) {
                        //if (device.getName().equals("SensorTag") || device.getName().equals("MIDbot eCard")) {
                        newDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                        newDevices.add(device);
                        //}
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("Select SensorTag device");
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(hku.fyp14017.blencode.R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see android.bluetooth.BluetoothAdapter.LeScanCallback#onLeScan(android.bluetooth.BluetoothDevice, int, byte[])
     */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        // TODO Auto-generated method stub
        System.out.println("LE device");
        if (device.getName().equals("SensorTag")) {
            System.out.println("SensorTag device");
            newDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
        }
    }

    /*private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				*//*
				 * Once successfully connected, we must next discover all the services on the
				 * device before we can read and write their characteristics.
				 *//*
                //Toast.makeText(getApplicationContext(), "Connected to SensorTag", Toast.LENGTH_SHORT).show();
                Log.d("dev", "Connected to SensorTag");
                gatt.discoverServices();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("dev", "Services done");
            }

        }
    };*/

}
