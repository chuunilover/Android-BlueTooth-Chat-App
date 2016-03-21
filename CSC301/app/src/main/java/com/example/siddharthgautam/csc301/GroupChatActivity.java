package com.example.siddharthgautam.csc301;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.Event;
import ca.toronto.csc301.chat.GroupChat;
import ca.toronto.csc301.chat.GroupController;

public class GroupChatActivity extends AppCompatActivity {

    public static int MAX_MSGS_ON_SCREEN = 30;
    static GroupChatActivity instance;
    private static final UUID uuid = UUID.fromString("63183176-0f7c-4673-b120-ac4116843e65");
    private Button sendButton;
    private TextView messageTextView;
    private ListView messageView;
    private ArrayAdapter<String> stringArrayAdapter;
    private ArrayList<String> stringList;
    GroupChat groupChat;
    private Button addUser;
    private Button getGroupMembersButton;



    private String mac;
    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        Bundle b = getIntent().getExtras();
//        groupChat = b.getParcelable("GroupChat");
        groupChat = ConnectionsList.getInstance().getGroupChat();
        if(groupChat == null){
           //Toast.makeText(getApplicationContext(), "Group chat is null", Toast.LENGTH_LONG).show();

        } else {
            //Toast.makeText(getApplicationContext(), groupChat.getName(), Toast.LENGTH_LONG).show();
            setTitle("Group: " + groupChat.getName());
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendButton = (Button) findViewById(R.id.group_send);
        addUser = (Button) findViewById(R.id.addToGroupChat);
        messageTextView = (TextView) findViewById(R.id.group_new_message);
        messageView = (ListView) findViewById(R.id.group_message_list);

        stringList = new ArrayList<String>();
        stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringList);
        messageView.setAdapter(stringArrayAdapter);

        appContext = getApplicationContext();
        instance = this;

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                messageTextView.setText("");
            }
        });

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(GroupChatActivity.this);
                alert.setTitle("Select User");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupChatActivity.this,
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
                                GroupController.getInstance().addToGroupChat(groupChat,
                                        ConnectionsList.getInstance().getMacFromName(strName));
                            }
                        });
                alert.show();
            }
        });

        getGroupMembersButton = (Button)findViewById(R.id.viewGroupMembers);
        getGroupMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(GroupChatActivity.this);
                String s = "";
                for(String userMac: groupChat.getmembers()){
                    s += ConnectionsList.getInstance().getNameFromMac(userMac) + "\n";
                }
                dlgAlert.setMessage(s);
                dlgAlert.setTitle("Group Members:");
                dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int button) {

                    }
                });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });
        loadMessages(getApplicationContext().getFilesDir().getAbsoluteFile(), groupChat.getName());
    }

    public static GroupChatActivity getInstance() {
        if(instance != null) {
            return instance;
        } else {
            GroupChatActivity groupChatActivity = new GroupChatActivity();
            instance = groupChatActivity;
            return  instance;
        }
    }

    private void sendMessage(){
        String message = messageTextView.getText().toString();

        stringArrayAdapter.add("You: " + message); //Todo: replace with message
        stringArrayAdapter.notifyDataSetChanged();

        Event e = new Event();
        e.setType(7);
        e.setSender(BluetoothAdapter.getDefaultAdapter().getAddress());
        e.setSenderName(BluetoothAdapter.getDefaultAdapter().getName());
        e.addAllowedClientsFromSet(groupChat.getmembers());
        e.setGroupChat(groupChat);
        e.setMessage(message);

        if(true){//fix after
            //t.sendMessage(message);
            ConnectionsList.getInstance().sendEvent(e);
            //Toast.makeText(appContext, "Broadcast a group msg", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(appContext, "No connection available right now", Toast.LENGTH_LONG).show();
        }
        saveMessages(getApplicationContext().getFilesDir().getAbsoluteFile(), groupChat.getName());
    }
    public void recieveMessage(String message, String senderMac, String groupName){
        String senderName = ConnectionsList.getInstance().getNameFromMac(senderMac);
        //Toast.makeText(appContext, "Got a group msg", Toast.LENGTH_LONG).show();
        //if(ConnectionsList.getInstance().getGroupChat().getName().equals(groupName) == false){//the sending group chat isnt open rightnow
          //  Toast.makeText(getApplicationContext(), ">> Saving msg for group " + groupName, Toast.LENGTH_LONG).show();
                try {
                    //Opens file and writes to it
                    FileOutputStream fos = this.openFileOutput(groupName + ".txt", Context.MODE_APPEND);
                    fos.write((senderName + ": " + message + "\n").getBytes());
                    fos.close();
                }catch(FileNotFoundException ex){
                    try{
                        FileOutputStream fos = this.openFileOutput(groupName + ".txt", Context.MODE_PRIVATE);
                        fos.write((senderName + ": " + message + "\n").getBytes());
                        fos.close();
                        return;
                    }catch(Exception exc){
                        return;
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        //}
        //stringArrayAdapter.add(senderName + ": " + message);
        //If this chat is not open

        loadMessages(getApplicationContext().getFilesDir().getAbsoluteFile(), ConnectionsList.getInstance().getGroupChat().getName());
    }

    public void loadMessages(File dir, String username){
        Toast.makeText(getApplicationContext(), "Loading msg for group " + username, Toast.LENGTH_LONG).show();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(this.openFileInput(
                    username + ".txt")));
            String message;
            stringArrayAdapter.clear();
            while((message = inputReader.readLine()) != null){
                stringArrayAdapter.add(message);
                if(stringArrayAdapter.getCount() > MAX_MSGS_ON_SCREEN){
                    stringArrayAdapter.remove(stringArrayAdapter.getItem(0));
                }
            }
            inputReader.close();
        }
        catch (Exception e){

        }

    }


    public void saveMessages(File dir, String username){

        //File saveFile = new File(dir, username + ".txt");
        try {
            FileOutputStream fos = openFileOutput(username + ".txt", Context.MODE_PRIVATE);
            for(int i = 0; i < messageView.getCount(); i++){
                String m = stringArrayAdapter.getItem(i) + '\n';
                fos.write(m.getBytes());
            }
            fos.close();
        }
        catch (Exception e){
            //do later
        }
    }

}
