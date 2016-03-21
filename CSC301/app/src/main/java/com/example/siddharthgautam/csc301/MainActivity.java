package com.example.siddharthgautam.csc301;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.Event;
import ca.toronto.csc301.chat.GroupController;

public class MainActivity extends AppCompatActivity implements Serializable{

    private BluetoothAdapter bluetooth;
    private static Set<BluetoothDevice> devices;
    private Set<BluetoothDevice> connectedDevices;
    Button scan, contacts;
    ListView devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Creates a hash set to store the devices found during scanning
        devices = new HashSet<BluetoothDevice>();

        scan = (Button)findViewById(R.id.scanButton);
        contacts = (Button)findViewById(R.id.deviceList);

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        //add paired devices to the list
        Set<BluetoothDevice> d = bluetooth.getBondedDevices();
        Iterator<BluetoothDevice> i = d.iterator();

        while(i.hasNext()) {
            BluetoothDevice device = i.next();
            devices.add(device);
        }

        devicesList = (ListView)findViewById(R.id.listView);

        // Check if Bluetooth is supported. If so enable it if necessary
        if (bluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device",
                    Toast.LENGTH_LONG).show();
            //System.exit(1);
        }

        if (!bluetooth.isEnabled()) {
            Intent start = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(start, 1);
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_LONG).show();
        }

        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //this.registerReceiver(receiver, filter);

        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String device_name = bluetooth.getName();
                if (!device_name.contains("BlueM-")) {
                    bluetooth.setName("BlueM-" + device_name);
                }
                findNewDevices();
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openContacts();
            }
        });

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "I will connect", Toast.LENGTH_LONG).show();
                String deviceName = devicesList.getItemAtPosition(position).toString();
                if (deviceName != null) {
                    connectDevice(deviceName);
                }
            }
        });
        ConnectionsList.getInstance().setHandler(mHandler);
        ConnectionsList.getInstance().accept();
        Toast.makeText(getApplicationContext(),"Connecting to paired devices..." +
                " asking for all network devices upon connect", Toast.LENGTH_LONG).show();
        BluetoothAdapter local = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> paired = local.getBondedDevices();
        Iterator<BluetoothDevice> it = paired.iterator();
        while(it.hasNext()){
            BluetoothDevice dev = it.next();
            ConnectionsList.makeConnectionTo(dev);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String address = null;
            switch (msg.what) {
                case 1:
                    byte[] readBuf = (byte[]) msg.obj;
                    Event e;
                    try{
                        e = (Event) Event.deserialize(readBuf);
                        int type = e.getType();
                        switch(type) {
                            case 1:
                                Toast.makeText(getApplicationContext(), "Recieved a broadcast event", Toast.LENGTH_LONG).show();
                                String m = e.getMessage();
                                if(e.isClientAllowed(bluetooth.getAddress())){
                                    chatActivity.getInstance().recieveMessage(m, e.getSender(), e);
                                    //this client can see it
                                }
                                //code to forward
                                ConnectionsList.getInstance().sendEvent(e);
                                break;
                            case 2:
                                Toast.makeText(getApplicationContext(), "Some device asked for a devices update, sending them", Toast.LENGTH_LONG).show();
                                ConnectionsList.getInstance().sendEvent(e);
                                break;
                            case 3:
                                Toast.makeText(getApplicationContext(), "Recieved a network devices event update", Toast.LENGTH_LONG).show();
                                ConnectionsList.getInstance().sendEvent(e);
                                break;
                            case 4:
                                Toast.makeText(getApplicationContext(), "a new device joined the network", Toast.LENGTH_LONG).show();
                                ConnectionsList.getInstance().sendEvent(e);
                                break;
                            case 5:
                                HandleType5(e);
                                break;

                        }

                    }
                    catch(Exception ex) {

                    }
                    break;

            }
        }
    };

    public void HandleType5(Event event){
        Toast.makeText(getApplicationContext(), "you have been added to a group chat", Toast.LENGTH_LONG).show();
        GroupController.getInstance().addGroupChat(event.getGroupChat());
        event.removeFronAllowedClients(bluetooth.getAddress());
        HashSet<String> allowedClients = event.getAllowedClients();
        for(String client : allowedClients){
            if(ConnectionsList.getInstance().isDeviceInNetwork(client)){
                event.removeFronAllowedClients(client);
            } else {
                allowedClients.remove(client);
            }
        }
        ConnectionsList.getInstance().sendEvent(event);
    }

    //Get devices
    public static BluetoothDevice getDeviceByName(String name) {
        for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }

    //Go to contacts activity
    public void openContacts() {
        startActivity(new Intent(MainActivity.this, contactsActivity.class));
    }

    public void findNewDevices(){
        bluetooth.startDiscovery();
    }


    // Reciever for discovering new devices..
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null) {
                    devices.add(device);
                    Toast.makeText(getApplicationContext(), "New Device Discovered", Toast.LENGTH_LONG).show();
                    listDevices();
                }

            }
        }
    };
    //Pairs with a bluetooth device with deviceName.
    public void connectDevice(String deviceName){
        //Checks if name matches
        for(BluetoothDevice device : devices){
            if(device.getName().equals(deviceName)){
                try{
                    //Connects
                    Method m = device.getClass().getMethod("createBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                    Toast.makeText(getApplicationContext(), device.getName(), Toast.LENGTH_LONG).show();
                    try {
                        connectedDevices.add(device);
                    } catch (NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "cannot add null devices", Toast.LENGTH_LONG);
                    }
                    //connectedDevices.add(device);
                    //Exception handling
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Disconnects paired device with name deviceName.
    public void disconnectDevice(String deviceName){
        //Checks if name matches
        for(BluetoothDevice device : devices) {
            if (device.getName().equals(deviceName))
                try {
                    //Connects
                    Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                    try {
                        connectedDevices.remove(device);
                    } catch (NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "cannot add null devices", Toast.LENGTH_LONG);
                    }
                    //Exception handling
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
    }

    //Lists all the devices found during scanning.
    public void listDevices(){

        ArrayList deviceList = new ArrayList();

        for(BluetoothDevice blueDevice : devices) {
            String name = blueDevice.getName();
            deviceList.add(blueDevice.getName());
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, deviceList);
        devicesList.setAdapter(adapter);
    }
    // Connects with a perticular device
    public void connect() {
        for (BluetoothDevice bt : devices) {
            Toast.makeText(getApplicationContext(), bt.getAddress(), Toast.LENGTH_LONG).show();
        }
    }
    //Starts a chat.
    public void startChat(View view) {
        //Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();
        devices = bluetooth.getBondedDevices();
        ArrayList deviceList = new ArrayList();

        for(BluetoothDevice blueDevice : devices) {
            String name = blueDevice.getName();
            deviceList.add(blueDevice.getName());
            //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();
        }
        //Intent intent = new Intent(MainActivity.this, chatActivity.class);
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra("BLUETOOTH_VALUE", bluetooth.toString());

        MainActivity.this.startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResume(){
        File resourceLocation = new File(this.getApplicationContext().getFilesDir().getAbsolutePath());

        super.onResume();
    }

}