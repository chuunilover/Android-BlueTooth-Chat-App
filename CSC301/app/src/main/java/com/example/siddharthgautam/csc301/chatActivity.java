package com.example.siddharthgautam.csc301;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devpaul.filepickerlibrary.FilePickerActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import ca.toronto.csc301.chat.ConnectedThread;
import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.Event;


public class chatActivity extends AppCompatActivity {

    public static int MAX_MSGS_ON_SCREEN = 20;
    static chatActivity instance;
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> devices;
    private static final UUID uuid = UUID.fromString("63183176-0f7c-4673-b120-ac4116843e65");
    private Button sendButton;
    private TextView messageTextView;
    private ListView messageView;
    private ArrayAdapter<String> stringArrayAdapter;
    private ArrayList<String> stringList;
    private BluetoothDevice contactDevice;
    private String mac, persistMac;
    private Context appContext;
    private Button sendFileButton;
    //private final Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        mac = b.getString("mac");
        persistMac = mac;
        //setTitle("Chat: " + mac);
        setTitle("Chat: " + ConnectionsList.getInstance().getNameFromMac(mac));
        setContentView(R.layout.activity_chat2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sendButton = (Button) findViewById(R.id.sendMessageButton);
        setSendButtonFunction(sendButton);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageView = (ListView) findViewById(R.id.messageView);
        stringList = new ArrayList<String>();
        stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringList);
        messageView.setAdapter(stringArrayAdapter);
        appContext = getApplicationContext();
        instance = this;

        sendFileButton = (Button) findViewById(R.id.sendFile);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filePickerIntent = new Intent(getInstance(), FilePickerActivity.class);
                filePickerIntent.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_FILE);
                startActivityForResult(filePickerIntent, FilePickerActivity.REQUEST_FILE);
            }
        });
    }

    @Override
    /**
     * Credit: https://github.com/DeveloperPaul123/FilePickerLibrary
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == FilePickerActivity.REQUEST_FILE
                && resultCode == RESULT_OK) {

            String filePath = data.
                    getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            if(filePath != null) {
                Toast.makeText(getInstance().appContext, "You selected file " + filePath, Toast.LENGTH_SHORT).show();
                File f = new File(filePath);
                byte[] bytes = new byte[(int) f.length()];
                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
                    bis.read(bytes, 0, bytes.length);
                }
                catch(Exception ex){

                }
                File sourceFile = new File(filePath);
                Intent intent = new Intent(); intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sourceFile));
                startActivity(Intent.createChooser(intent, "Send File"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static chatActivity getInstance() {
        if(instance != null) {
            return instance;
        } else {
            chatActivity inst = new chatActivity();
            instance = inst;
            return  instance;
        }
    }

    @Override
    protected void onStart() {
        //// TODO: 10/31/2015 replace "Test.txt" with unique identifier for chat.

        loadMessages(appContext.getFilesDir().getAbsoluteFile(), mac);
        this.mac = persistMac;
        super.onStart();
    }

    @Override
    protected void onStop() {
        //// TODO: 10/31/2015 replace "Test.txt" with unique identifier for chat.
        saveMessages(getApplicationContext().getFilesDir().getAbsoluteFile(), mac);
        this.mac = "";
        super.onStop();
    }

    /**
     * Set sendButton's onClickListener to sendMessage.
     * @param sendButton A button, which you want to use as the send message button.
     */
    private void setSendButtonFunction(Button sendButton){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                messageTextView.setText("");
            }
        });
    }

    /**
     * Called when the send button is pressed. Get the text from the messageTextView,
     * send it through a BlueTooth connection and write the message on your own message log.
     */
    private void sendMessage(){
        //Toast.makeText(this, messageTextView.getText(), Toast.LENGTH_SHORT).show();
        String message = messageTextView.getText().toString();
        stringArrayAdapter.add("You: " + message); //Todo: replace with message
        stringArrayAdapter.notifyDataSetChanged();

        Event e = new Event();
        e.setType(1);
        e.allowClient(mac);
        e.setMessage(message);
        e.setSenderName(BluetoothAdapter.getDefaultAdapter().getName());
        e.setSender(BluetoothAdapter.getDefaultAdapter().getAddress());

        if(true){//fix after
            //t.sendMessage(message);
            ConnectionsList.getInstance().sendEvent(e);
            //Toast.makeText(appContext, "Broadcast a msg", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(appContext, "No connection available right now", Toast.LENGTH_LONG).show();
        }
        saveMessages(appContext.getFilesDir().getAbsoluteFile(), mac);
    }

    public void recieveMessage(String message, String senderMac, Event e){
        //Toast.makeText(appContext, " got msg", Toast.LENGTH_SHORT).show();

        String senderName = ConnectionsList.getInstance().getNameFromMac(senderMac);
        //If this chat is not open
        if(senderMac.equals(mac) == false){//save messaged to be loaded later if the senders' chat is currently not open
            try {
                //Opens file and writes to it
                FileOutputStream fos = this.openFileOutput(senderMac + ".txt", Context.MODE_APPEND);
                fos.write(("Them: " + message + "\n").getBytes());
                fos.close();
            }catch(FileNotFoundException ex){
                try{
                    FileOutputStream fos = this.openFileOutput(senderMac + ".txt", Context.MODE_PRIVATE);
                    fos.write(("Them: " + message + "\n").getBytes());
                    fos.close();
                    return;
                }catch(Exception exc){
                    return;
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //Toast.makeText(appContext, "SAVED A MESSAGE! Saved " + message + " to" + senderMac+".txt", Toast.LENGTH_LONG);
            return;
        }
        stringArrayAdapter.add("Them: " + message);
        if(stringArrayAdapter.getCount() > MAX_MSGS_ON_SCREEN){
            stringArrayAdapter.remove(stringArrayAdapter.getItem(0));
        }
        stringArrayAdapter.notifyDataSetChanged();
        //BluetoothController.getInstance().sendMessage(new Message());
        //implement this!
    }

    /**public void receiveMessage(Message m){
        stringArrayAdapter.add(m.toString()); //Todo: replace with message
        if(stringArrayAdapter.getCount() > MAX_MSGS_ON_SCREEN){
            stringArrayAdapter.remove(stringArrayAdapter.getItem(0));
        }
        stringArrayAdapter.notifyDataSetChanged();
    }**/

    /**
     * Loads all the messages from a given message log file onto the UI.
     * @param dir A file containing the location of the app's resources.
     * @param username A unique identifier indicating which message log file you want to view.
     */
    public void loadMessages(File dir, String username){
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

    /**
     * Writes all the messages from the current message buffer array (on the ListView)
     * to a file in the app's resources folder.
     * @param dir A file of the app's resource folder.
     * @param username A unique identifier for this message log file. Must be able to identify
     *                 its name for later use.
     */
    public void saveMessages(File dir, String username){

        //File saveFile = new File(dir, username + ".txt");
        try {
            FileOutputStream fos = this.openFileOutput(username + ".txt", Context.MODE_PRIVATE);
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
