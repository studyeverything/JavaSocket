package sample.reponse;

import java.io.Serializable;

/**
 * Created by nguye on 7/20/2017.
 */
public class BasePacket implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -3686599855786847540L;


    protected PacketType pkt_type;

    public void setType(PacketType type) {
        this.pkt_type = type;
    }

    public PacketType getType() {
        return this.pkt_type;
    }

    @Override
    public BasePacket clone() throws CloneNotSupportedException {
        BasePacket data = (BasePacket) super.clone();
        return data;
    }

    public enum PacketType {
        REGISTER,
        REGISTER_ACK,
        ORDER_DATA,
        ORDER_DATA_FROM_REST,
        COMPLETE_PAYMENT,
        ORDER_CANCEL_DATA,
        CUSTOMER_REGISTER,
        CUSTOMER_UNREGISTER,
        COMPLETE_NUMBERING,
        PREPAY_ORDER_DATA,
        STORE_REGISTER
    }
}

