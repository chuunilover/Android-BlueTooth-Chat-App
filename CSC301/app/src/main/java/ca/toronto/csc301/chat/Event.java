package ca.toronto.csc301.chat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Priyen on 2015-11-19.
 */
public class Event implements Serializable{
    /**
     * type:
     *  int 1 = broadcast msg, visibility to only target clients
     *  int 2 = request copy of receivers' clients
     *  int 3 = recieving type 2 request, in data value
     *  int 4 = broadcast -- new device in the network (the sender.)
     *  int 5 = new member added to group chat
     *  int 6 = keep alive
     *  int 7 = recieving a group chat message
     */
    private int type;
    private byte[] fileBytes = null;
    private String message;
    private String receiver;
    private String sender;//sender mac
    private String senderName;//
    private HashMap<String, String> data = new HashMap<String, String>();
    //who can see the MESSAGE?
    private HashSet<String> allowedClients = new HashSet<String>();//mac addrs'
    //who has already seen/received this event?
    private HashSet<String> excludedTargets = new HashSet<String>();
    private GroupChat groupChat;
    private UUID UUID;
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public UUID getUUID(){
        return this.UUID;
    }

    public Event (){
        this.UUID = UUID.randomUUID();
    }

    public void setFileBytes(byte[] bytes){
        this.fileBytes = bytes;
    }

    public byte[] getFileBytes(){
        return this.fileBytes;
    }

    public void setGroupChat(GroupChat groupChat){
        this.groupChat = groupChat;
    }

    public GroupChat getGroupChat(){ return groupChat;}

    public void setSenderName(String s){
        this.senderName = s;
    }

    public String getSenderName(){
        return this.senderName;
    }

    public void setData(HashMap<String, String> s)
    {
        this.data = s;
    }

    public HashMap<String, String> getData(){
        return this.data;
    }

    public void setSender(String s){
        this.sender = s;
    }

    public String getSender(){
        return this.sender;
    }

    public void setType(int s){
        this.type = s;
    }

    public void setMessage(String s){
        this.message = s;
    }

    public void addExcludedTarget(String s){
        excludedTargets.add(s);
    }

    public void removeExcludedTarget(String s){
        excludedTargets.remove(s);
    }

    public Set<String> getExcludedTargets(){
        return new HashSet<String>(this.excludedTargets);
    }

    public int getType(){
        return this.type;
    }

    public String getMessage(){
        return this.message;
    }

    public boolean isClientAllowed(String mac){
        return allowedClients.contains(mac);
    }
    //lets this client see the msg
    public void allowClient(String s){
        allowedClients.add(s);
    }

    public void addAllowedClientsFromSet(HashSet<String> members){
        allowedClients.addAll(members);
    }

    public HashSet<String> getAllowedClients() {
        return allowedClients;
    }

    public void removeFronAllowedClients(String mac){
        allowedClients.remove(mac);
    }
}
