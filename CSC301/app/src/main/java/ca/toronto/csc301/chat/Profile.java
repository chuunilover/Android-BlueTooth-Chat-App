package ca.toronto.csc301.chat;

/**
 * Created by akshay on 22/11/15.
 */
public class Profile {

    static Profile instance;

    private Profile(){
        name = " ";
    }

    public static Profile getInstance(){
        if(instance == null){
            instance = new Profile();
        }
        return instance;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}
