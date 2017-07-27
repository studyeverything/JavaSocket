package sample.reponse;

/**
 * Created by nguye on 7/20/2017.
 */
public class StoreBasePacket extends  BasePacket{


    private String store_id;


    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public StoreBasePacket(String store_id) {
        this.store_id = store_id;
    }



    @Override
    public StoreBasePacket clone() throws CloneNotSupportedException {
        StoreBasePacket data = (StoreBasePacket) super.clone();
        return data;
    }

    @Override
    public String toString(){
        return String.format("%s store_id:%s", this.getClass().getSimpleName(), store_id);
    }
}