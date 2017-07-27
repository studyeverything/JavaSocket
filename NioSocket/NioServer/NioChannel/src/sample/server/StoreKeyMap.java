package sample.server;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nguye on 7/20/2017.
 */
public class StoreKeyMap {


    private ConcurrentHashMap<String, CopyOnWriteArrayList<SelectionKey>> map = new ConcurrentHashMap<String, CopyOnWriteArrayList<SelectionKey>>();

    public void addKey(String storename, SelectionKey key){
        System.out.println("StoreKeyMap storename = [" + storename + "], key = [" + key + "]");
        if(map.get(storename) == null){
            map.put(storename, new CopyOnWriteArrayList<SelectionKey>());
        }
        map.get(storename).add(key);

    }

    public void clearAll(){
        map.clear();
    }

    public void removeKey(String storename){
        map.remove(storename);
    }

    public List<SelectionKey> getKey(String storename){
        System.out.println("StoreKeyMap getKey storename = " + storename);
        return map.get(storename);
    }


    //	@Override
    public String print(){
        StringBuilder sb = new StringBuilder();

        for(String store : map.keySet()){
            sb.append(store).append(":").append(map.get(store));
        }
        return sb.toString();
    }

}
