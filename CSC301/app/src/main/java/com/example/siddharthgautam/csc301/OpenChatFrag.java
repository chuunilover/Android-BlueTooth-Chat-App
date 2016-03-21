package com.example.siddharthgautam.csc301;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.toronto.csc301.chat.ConnectedThread;
import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.FavouriteUsers;
import ca.toronto.csc301.chat.GroupChat;

public class OpenChatFrag extends Fragment {

    ListView favouritesList;
    ArrayAdapter<String> adapter;
    List<String> cL;
    public static OpenChatFrag newInstance() {
        OpenChatFrag fragment = new OpenChatFrag();
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_open_chat_frag, container, false);

        favouritesList = (ListView) view.findViewById(R.id.favourites_list);
        Set<String> favMAC = FavouriteUsers.getInstance().getFavs();
        cL = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, cL);
        favouritesList.setAdapter(adapter);
        updateContactsList();
        Button refresh = (Button) view.findViewById(R.id.refresh_fav);

        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
             updateContactsList();
            }
        });

        favouritesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceName = favouritesList.getItemAtPosition(position).toString();
                boolean connection = false;
                if (deviceName.contains("✓")) {
                    connection = true;
                    deviceName = deviceName.substring(2);//to account for possible checkmark
                }
                BluetoothDevice device = null;
                try {
                    device = AllContactsFrag.getInstance().getDeviceByName(deviceName);
                } catch (Exception ex) {
                    //device isnt connected yet/maybe a outer network device
                }
                String mac;
                if (device != null) {//for paired devices
                    mac = device.getAddress();
                    ConnectedThread t = ConnectionsList.getInstance().getConnectedThread((device));
                    if (t == null) {//no connection available, try to connect
                        ConnectionsList.getInstance().makeConnectionTo(device);
                        Toast.makeText(getContext(), deviceName + " is a non connected paired device" +
                                "   Trying to connect..", Toast.LENGTH_LONG).show();
                        ConnectionsList.getInstance().closeConnection(device);
                        ConnectionsList.getInstance().makeConnectionTo(device);
                        return;
                    }
                    if (t.getSocket().isConnected() == false) {
                        Toast.makeText(getContext(), deviceName + " is a non connected paired device" +
                                "   Trying to connect..", Toast.LENGTH_LONG).show();
                        ConnectionsList.getInstance().closeConnection(device);
                        ConnectionsList.getInstance().makeConnectionTo(device);
                        return;
                    }
                } else {//this device is not paired, but maybe its in the network, check.
                    mac = ConnectionsList.getInstance().getMacFromName(deviceName);
                    if (ConnectionsList.getInstance().isDeviceInNetwork(mac) == false) {
                        Toast.makeText(getContext(), deviceName + " is currently not in the network", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (AllContactsFrag.getInstance().isMacBlocked(mac)) {
                    Toast.makeText(getContext(), deviceName + " is blocked. Can't communicate with them.", Toast.LENGTH_LONG).show();
                    return;
                }
                AllContactsFrag.getInstance().goToChat(getView(), mac);
            }
        });

        return view;
    }

    private void updateContactsList(){
        cL.clear();
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> d = bluetooth.getBondedDevices();
        Iterator<BluetoothDevice> i = d.iterator();
        boolean connected = false;
        while(i.hasNext()){
            BluetoothDevice device = i.next();
            String mac = device.getAddress();
            if(FavouriteUsers.getInstance().getFavs().contains(mac) == false){
                continue;
            }
            String device_name = device.getName();
            ConnectedThread t = ConnectionsList.getInstance().getConnectedThread(device);

            if(t == null){
                //Toast.makeText(getContext(), device.getName() + " -- trying to connect to it", Toast.LENGTH_SHORT).show();
                //ConnectionsList.getInstance().closeConnection(device);
                //ConnectionsList.getInstance().makeConnectionTo(device);
            }
            else{
                if(t.getSocket().isConnected() == false){
                    //ConnectionsList.getInstance().closeConnection(device);
                    //ConnectionsList.getInstance().makeConnectionTo(device);
                }
                else {
                    connected = true;
                    device_name = "✓ " + device_name;
                }
            }
            if(device_name.contains("✓") == false && ConnectionsList.getInstance().isDeviceInNetwork(device.getAddress())){//some other client has this client connected
                device_name = "✓ " + device_name;
            }
            if(ConnectionsList.getInstance().isDeviceInNetwork(device.getAddress())){

            }
            else {
                cL.add(device_name);
            }
        }
        // now add from network devices
        Iterator<String> di = ConnectionsList.getInstance().getNamesOfConnectedDevices().iterator();
        while(di.hasNext()) {
            String name = di.next();

            if(FavouriteUsers.getInstance().getFavs().contains(ConnectionsList.getInstance().getMacFromName(name)) == false){
                continue;
            }

            if (cL.contains(name) || cL.contains("✓ " + name)) {
                continue;
            }
            cL.add("✓ " + name);
        }
        favouritesList.setAdapter(adapter);
    }
}