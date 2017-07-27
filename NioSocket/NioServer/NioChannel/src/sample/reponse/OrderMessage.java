package sample.reponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nguye on 7/20/2017.
 */
public class OrderMessage extends StoreBasePacket {
    private static final long serialVersionUID = 367394951010526137L;

    private int tableId;

    private long orderId;

    private String storeId;

    private String tagId;


    public OrderMessage(String store_id) {
        super(store_id);
        super.pkt_type = PacketType.PREPAY_ORDER_DATA;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }


    @Override
    public String toString() {


        return String.format("%s orderId:%s,"
                        + "storeId: %s,"
                        + "tagId: %s,",
                this.getClass().getSimpleName(),
                orderId,
                storeId,
                tagId);
    }
}
