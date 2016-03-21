package ca.toronto.csc301.chat;

import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by akshay on 21/11/15.
 */
public class GroupController {

    private static GroupController instance;
    List<GroupChat> groupChats;
    BluetoothAdapter bluetooth;


    private GroupController(){
        groupChats = new ArrayList<>();
        bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    public static GroupController getInstance(){
        if(instance == null){
            instance = new GroupController();
            return instance;
        }
        return instance;
    }

    public GroupChat createNewGroupChat(String name){

        GroupChat groupChat = new GroupChat(name, bluetooth.getAddress());
        groupChats.add(groupChat);
        return groupChat;
    }

    public GroupChat getGroupChatByName(String name){
        Iterator<GroupChat> groupChatIterator = groupChats.iterator();
        while(groupChatIterator.hasNext()){
            GroupChat groupChat = groupChatIterator.next();
            if(groupChat.getName().equals(name)){
                return groupChat;
            }
        }
        return null;
    }

    public void addToGroupChat(GroupChat groupChat, String member){
        groupChat.addMember(member);
        Event event = new Event();
        event.addAllowedClientsFromSet(groupChat.getMembersExceptSelf());
        event.addExcludedTarget(bluetooth.getAddress());
        event.setType(5);
        event.setGroupChat(groupChat);
        ConnectionsList.getInstance().sendEvent(event);


    }

    public void addGroupChat(GroupChat groupChat){
        Iterator<GroupChat> groupChatIterator = groupChats.iterator();
        while(groupChatIterator.hasNext()){
            GroupChat chat = groupChatIterator.next();
            if(chat.equals(groupChat)){
                chat.setMembers(groupChat.getmembers());
                return;
            }
        }
        groupChats.add(groupChat);
    }

    public List<GroupChat> getGroupChats(){
        return groupChats;
    }
}
