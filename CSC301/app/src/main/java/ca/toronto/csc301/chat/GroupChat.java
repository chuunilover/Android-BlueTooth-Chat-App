package ca.toronto.csc301.chat;

import android.bluetooth.BluetoothAdapter;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by akshay on 21/11/15.
 */
public class GroupChat implements Serializable{
    private HashSet<String> members;
    private String name;

    public GroupChat(String name, String member){
        this.name = name;
        members = new HashSet<>();
        members.add(member);
    }

    public void addMember(String member){
        members.add(member);
    }

    public void setMembers(HashSet<String> members){this.members.addAll(members);}

    public HashSet<String> getmembers(){
        return members;
    }

    public HashSet<String> getMembersExceptSelf(){
        HashSet<String> membersExceptSelf = new HashSet<String>();
        membersExceptSelf.addAll(members);
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        membersExceptSelf.remove(bluetooth.getAddress());
        return membersExceptSelf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean checkMemberByMAC(String mac){
        return members.contains(mac);
    }

    @Override
    public boolean equals(Object o) {
        GroupChat temp = (GroupChat) o;
        return temp.getName().equals(name);
    }

    @Override
    public String toString(){
        return name;
    }
}
