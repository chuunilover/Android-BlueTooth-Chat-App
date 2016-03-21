package ca.toronto.csc301.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 3/20/2016.
 */
public class ClientGraph {
    //Graph made with an adjacency list, and clients maps each list to a client.
    private List<String> clients;
    private List<List<String>> adjacency_list;
    private String self_mac;

    public ClientGraph(String self_mac){
        clients = new ArrayList<String>();
        adjacency_list = new ArrayList<List<String>>();
        this.self_mac = self_mac;
    }

    /**
     *
     */
    public void addClient(String new_client_mac, String network_attach_client){
        if (!clients.contains(new_client_mac)) {
            //Add new client to attach point client, add [network_attach_client] to graph
            adjacency_list.get(clients.indexOf(network_attach_client)).add(new_client_mac);
            List<String> new_client_list = new ArrayList<String>();
            new_client_list.add(network_attach_client);
            adjacency_list.add(new_client_list);
            clients.add(new_client_mac);
        }
    }

    public void removeClient(String client_to_remove){
        if (clients.contains(client_to_remove)) {
            int index_to_remove = clients.indexOf(client_to_remove);
            clients.remove(index_to_remove);
            adjacency_list.remove(index_to_remove);
            for (List<String> list:adjacency_list){
                if(list.contains(client_to_remove)){
                    list.remove(client_to_remove);
                }
            }
            List<Integer> ints_to_remove = new ArrayList<Integer>();
            for (String client:findDisconnectedClients()){
                ints_to_remove.add(clients.indexOf(client));
            }
            int[] clients_to_remove = new int[ints_to_remove.size()];
            for (int i = 0; i < ints_to_remove.size(); i++){
                clients_to_remove[i] = ints_to_remove.get(i);
            }
            java.util.Arrays.sort(clients_to_remove);
            for(int i = client_to_remove.length() - 1; i >= 0; i++){
                clients.remove(clients_to_remove[i]);
                adjacency_list.remove(clients_to_remove[i]);
            }
        }
    }

    public List<String> findDisconnectedClients(){
        List<String> visitedClients = new ArrayList<String>();
        List<String> visitQueue = new ArrayList<String>();
        visitedClients.add(self_mac);
        visitQueue.add(self_mac);
        while(!visitQueue.isEmpty()){
            String current_mac = visitQueue.get(0);
            visitQueue.remove(0);
            visitedClients.add(current_mac);
            for (String neighbour:adjacency_list.get(clients.indexOf(current_mac))){
                if (!visitedClients.contains(neighbour)){
                    visitQueue.add(neighbour);
                    visitedClients.add(neighbour);
                }
            }
        }
        List<String> not_in_network = new ArrayList<String>();
        for (String mac_in_graph:clients){
            if(!visitedClients.contains(mac_in_graph)){
                not_in_network.add(mac_in_graph);
            }
        }
        return not_in_network;
    }
}
