package com.example.siddharthgautam.csc301;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.toronto.csc301.chat.ConnectedThread;
import ca.toronto.csc301.chat.ConnectionsList;
import ca.toronto.csc301.chat.GroupChat;
import ca.toronto.csc301.chat.GroupController;

public class GroupFrag extends Fragment {

    private BluetoothAdapter bluetooth;
    private ListView groupList;
    private ArrayList gL = new ArrayList();
    private ArrayAdapter<GroupChat> adapter;
    String[] items;
    ArrayList<String> listItems=new ArrayList<String>();
    EditText editText;
    Button addButton;
    ListView listView;
    ListView lv;
    private Thread refreshThread;
    private Handler refreshHandler;


    public static GroupFrag newInstance() {
        GroupFrag fragment = new GroupFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_group_frag, container, false);

        listView = (ListView) view.findViewById(R.id.group_list);

        lv = (ListView)view.findViewById(R.id.group_list);

        //listItems = new ArrayList<String>();
        //if (adapter == null) {
        //LinearLayout rl = (LinearLayout) view.findViewById(R.id.myID);
        Button bt = (Button)view.findViewById(R.id.Button01);
        //final Button refresh = (Button) view.findViewById(R.id.grp_frag_refresh);

        bt.setText("Add a Group");
        bt.setBackgroundColor(getResources().getColor(R.color.lightblue));
        bt.setTextColor(getResources().getColor(R.color.white));
/*
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                refreshView(view);
            }

        });
*/

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Enter a Group Name");
                final EditText input = new EditText(getContext());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int button) {
                        ArrayList<String> myStringArray1 = new ArrayList<String>();
                        String value = input.getText().toString();
                        Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();
                        // add the input value to the group controller listli
                        GroupController controller = GroupController.getInstance();
                        controller.createNewGroupChat(value);
                        List<GroupChat> data = controller.getGroupChats();
                        //refreshView(view);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int button) {
                    }
                });

                alert.show();
                // read data into adapter
                GroupController controller = GroupController.getInstance();
                List<GroupChat> data = controller.getGroupChats();
                adapter = new ArrayAdapter<GroupChat>(getActivity(), android.R.layout.simple_list_item_1, data);
                adapter.notifyDataSetChanged();
            }

        });
        //populateListView(view);
        //rl.addView(bt);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newGroupChatActivityIntent = new Intent(getActivity(), GroupChatActivity.class);
                newGroupChatActivityIntent.putExtra("GroupChat", (GroupChat)listView.getItemAtPosition(position));
                ConnectionsList.getInstance().setGroupChat((GroupChat)listView.getItemAtPosition(position));
                startActivity(newGroupChatActivityIntent);
            }
        });

        //ArrayList<String> listItems=new ArrayList<String>();
        //listItems.add("hello");
        //listItems.add("goodbye");
        ListView lv = (ListView)view.findViewById(R.id.group_list);
        adapter = new ArrayAdapter<GroupChat>(getActivity(), android.R.layout.simple_list_item_1, GroupController.getInstance().getGroupChats());
        //adapter.add("hello");
        //adapter.add("goodbye");
        lv.setAdapter(adapter);

        GroupController controller = GroupController.getInstance();

        //populateListView(view);
        refreshView(view);

        refreshHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                refreshView(GroupFrag.this.getView());
            }
        };


        refreshThread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(10000, 0);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    refreshHandler.sendMessage(new Message());
                }
                this.run();
            }
        };
        refreshThread.start();

        return view;
    }

    public void refreshView(View view) {
        GroupController controller = GroupController.getInstance();
        List<GroupChat> data = controller.getGroupChats();
        //Toast.makeText(getContext(), "Refreshing group chats...", Toast.LENGTH_LONG).show();
        //LinearLayout rl = (LinearLayout) view.findViewById(R.id.myID);

        adapter = new ArrayAdapter<GroupChat>(getActivity(), android.R.layout.simple_list_item_1, GroupController.getInstance().getGroupChats());
        //adapter.add("hello");
        //adapter.add("goodbye");
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //rl.addView(lv);
    }


}