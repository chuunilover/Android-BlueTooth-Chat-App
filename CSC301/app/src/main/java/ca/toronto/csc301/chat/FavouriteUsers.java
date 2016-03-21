package ca.toronto.csc301.chat;

import android.content.Context;

import com.example.siddharthgautam.csc301.AllContactsFrag;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by akshay on 22/11/15.
 */
public class FavouriteUsers {

    private Set<String> favMacAddrs;
    private static FavouriteUsers instance;

    private FavouriteUsers(){
        favMacAddrs = new HashSet<>();
        loadFavs();
    }

    public static FavouriteUsers getInstance(){
        if(instance == null){
            instance = new FavouriteUsers();
        }
        return instance;
    }

    public Set<String> getFavs(){
        return this.favMacAddrs;
    }

    private void saveFavs(){
        try {
            FileOutputStream fos = AllContactsFrag.getInstance().getContext().openFileOutput("favs.txt", Context.MODE_PRIVATE);
            Iterator<String> i = favMacAddrs.iterator();
            while (i.hasNext()) {
                fos.write(("" + i.next() + "\n").getBytes());
            }
            fos.close();
        }
        catch(Exception ex){

        }
    }

    private void loadFavs(){
        favMacAddrs.clear();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(AllContactsFrag.getInstance().getContext().openFileInput(
                    "favs.txt")));
            String message;
            while((message = inputReader.readLine()) != null){
                favMacAddrs.add(message);
            }
            inputReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addFav(String mac){
        favMacAddrs.add(mac);
        saveFavs();
    }

    public void removeFav(String mac){
        favMacAddrs.remove(mac);
        saveFavs();
    }
}
