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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.toronto.csc301.chat.BlockedUsers;
import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.FavouriteUsers;

public class SettingsFrag extends Fragment {

    Button update;
    Button favourite;
    Button blacklist;
    EditText profileNameTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_settings_frag, container, false);

        update = (Button) view.findViewById(R.id.updateProfile);
        profileNameTextView = (EditText) view.findViewById(R.id.profile_name);
        favourite = (Button) view.findViewById(R.id.setting_favourits);
        favourite.setText("Add/Remove Favourite Users");
        blacklist = (Button) view.findViewById(R.id.setting_blacklist);
        blacklist.setText("Block/Unlock Users");


        if(profileNameTextView != null) {
            profileNameTextView.setText(BluetoothAdapter.getDefaultAdapter().getName());
        }

        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (profileNameTextView != null) {
                    if (profileNameTextView.getText().toString() != null) {
                        BluetoothAdapter.getDefaultAdapter().setName(profileNameTextView.getText().toString());
                    }
                }
            }
        });


        favourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Select User");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.select_dialog_singlechoice);

                //arrayAdapter.addAll(ConnectionsList.getInstance().getConnectedMacs());
                arrayAdapter.addAll(ConnectionsList.getInstance().getNamesOfConnectedDevices());
                alert.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alert.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                //GroupController.getInstance().addToGroupChat(groupChat, strName);
                                String mac = ConnectionsList.getInstance().getMacFromName(strName);
                                if(FavouriteUsers.getInstance().getFavs().contains(mac)) {
                                    FavouriteUsers.getInstance().removeFav(mac);
                                    Toast.makeText(getContext(), "Removed " + strName + " as a favourite", Toast.LENGTH_LONG).show();
                                }else{
                                    FavouriteUsers.getInstance().addFav(mac);
                                    Toast.makeText(getContext(), "Added " + strName + " as a favourite", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                alert.show();

            }
        });

        blacklist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Select User");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.select_dialog_singlechoice);

                //arrayAdapter.addAll(ConnectionsList.getInstance().getConnectedMacs());
                arrayAdapter.addAll(ConnectionsList.getInstance().getNamesOfConnectedDevices());
                alert.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alert.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                //GroupController.getInstance().addToGroupChat(groupChat, strName);
                                //BlockedUsers.getInstance().addUserToBlackList(ConnectionsList.getInstance().getMacFromName(strName));
                                String mac = ConnectionsList.getInstance().getMacFromName(strName);
                                if(AllContactsFrag.isMacBlocked(mac)){
                                    AllContactsFrag.unblockMac(mac);
                                    Toast.makeText(getContext(), "Unblocked " + strName + ". They can now communicate with you", Toast.LENGTH_LONG).show();
                                }else{
                                    AllContactsFrag.blockMac(mac);
                                    Toast.makeText(getContext(), "Blocked " + strName + ". They can no longer communicate with you", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                alert.show();

            }
        });

        return view;
    }

    public static SettingsFrag newInstance(){
        return new SettingsFrag();
    }

}
