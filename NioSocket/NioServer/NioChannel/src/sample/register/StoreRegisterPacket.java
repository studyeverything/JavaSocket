package sample.register;

import sample.reponse.BasePacket;
import sample.reponse.StoreBasePacket;

/**
 * Created by nguye on 7/20/2017.
 */
public class StoreRegisterPacket extends StoreBasePacket {


    /**
     *
     */
    private static final long serialVersionUID = 6265423944609054765L;

    public StoreRegisterPacket(String store_id){
        super(store_id);
        super.pkt_type = BasePacket.PacketType.STORE_REGISTER;
    }

    @Override
    public StoreRegisterPacket clone() throws CloneNotSupportedException {
        StoreRegisterPacket data = (StoreRegisterPacket) super.clone();
        return data;
    }
}
