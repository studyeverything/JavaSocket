package sample.server;

import sample.reponse.StoreBasePacket;
import sample.uil.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nguye on 7/20/2017.
 */
public class DataQueue {

    private LinkedList<StoreBasePacket> dataQueue = new LinkedList<StoreBasePacket>();

    /**
     * key: 가맹점 식별자
     * value: List of OrderData
     */
    private ConcurrentHashMap<String, LinkedList<StoreBasePacket>> pendingDataMap = new
            ConcurrentHashMap<String, LinkedList<StoreBasePacket>>();


    private StoreKeyMap keymap;

    AtomicInteger cnt = new AtomicInteger(1);


    public DataQueue(StoreKeyMap map){
        System.out.println("public DataQueue(StoreKeyMap map){");
        this.keymap = map;
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
//		ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new PoolWorker());
    }

    public void addData(String storeName, StoreBasePacket data){
        synchronized(dataQueue){
            System.out.println("DataQueue addData(String storeName="+storeName+", StoreBasePacket data){" +data.toString());
            dataQueue.add(data);
            dataQueue.notify();
        }
    }


    public void addPendingData(StoreBasePacket msg){
        System.out.println("DataQueue.addPendingData");
        LinkedList<StoreBasePacket> list = pendingDataMap.get(msg.getStore_id());
        if (list == null){
            list = new LinkedList<StoreBasePacket>();
        }

        if ( list.size() >= SettingsNioServer.MAX_SIZE_QUEUE) {
            list.removeFirst();
            System.err.println("pending size is overflow (" + SettingsNioServer.MAX_SIZE_QUEUE +") so removefirst.");
        }

        list.add(msg);
        pendingDataMap.put(msg.getStore_id(), list);

        System.err.println(
                String.format("channel is not connected... pendingDataMap(%d)",
                        list.size())
        );
    }


    /**
     * pendingQueue에 있는 데이터를 전송한다.
     * 단, 다수의 데이터를 일괄 전송 시, 전송 실패가 발생하므로, jitter(0.5)를 걸어준다.
     * @param storename
     * @param channel
     * @throws IOException
     */
    public void sendPendingData(String storename, SocketChannel channel) throws IOException {

        System.out.println("DataQueue storename = [" + storename + "], channel = [" + channel + "]");

        LinkedList<StoreBasePacket> list = pendingDataMap.get(storename);
        if (list == null) return;

        while(!list.isEmpty()){
            StoreBasePacket data = list.removeFirst();
            System.out.println("pending : " + data);
            ByteBuffer buf2 = ByteBuffer.wrap(Util.serializeObject(data));
            channel.write(buf2);
            try{
                Thread.sleep( (new Random()).nextInt(500) );//jitter
            }catch(InterruptedException e){
                System.out.println("DataQueue e = " + e);
            }
        }
    }


    private class PoolWorker extends Thread {

        public void run() {
            while(true){
                System.out.println("whiile");
                synchronized(dataQueue){
                    while(dataQueue.isEmpty()){
                        try{
                            System.out.println("DataQueue dDataQueue = " + dataQueue);
                            dataQueue.wait();
                        }catch(InterruptedException e){
                            System.out.println("DataQueue error"+e);
                        }
                    }

                    StoreBasePacket order = (StoreBasePacket)dataQueue.removeFirst();

                    //SelectionKey key = keymap.getKey(order.getStoreId());
                    System.out.println("DataQueue order.getStoreId()"+   order.getStore_id());
                    List<SelectionKey> keys = keymap.getKey(order.getStore_id());
                    if (keys != null && keys.size() > 0){ // key != null
//						SocketChannel clientChannel = (SocketChannel)key.channel();
//						ByteBuffer buf2;
//						try {
//							if (clientChannel.isConnected()){
//								//pendingData를 우선 전송한다.
//								sendPendingData(order.getStoreId(), clientChannel);
//
//								buf2 = ByteBuffer.wrap(Util.serializeObject(order));
//								clientChannel.write(buf2);
//							} else {
//								//channle이 연결된 상태가 아니라면..일시적 통신 장애일 수 있으므로,
//								//pendingDataQueue에 저장함.
//								addPendingData(order);
//							}
//						} catch (IOException e) {
//							System.err.println(String.format("channel is opened : %s", clientChannel.isOpen()));
//							System.err.println(String.format("channel is connected : %s", clientChannel.isConnected()));
//							e.printStackTrace();
//
//							keymap.removeKey(order.getStoreId());
//						}
                        for(SelectionKey key: keys){
                            if(key != null){
                                SocketChannel clientChannel = (SocketChannel)key.channel();
                                ByteBuffer buf2;
                                try {
                                    if(clientChannel.isOpen()){
                                        if (clientChannel.isConnected()){
                                            //pendingData를 우선 전송한다.
                                            sendPendingData(order.getStore_id(), clientChannel);

                                            buf2 = ByteBuffer.wrap(Util.serializeObject(order));
                                            clientChannel.write(buf2);
                                        } else {
                                            //channle이 연결된 상태가 아니라면..일시적 통신 장애일 수 있으므로,
                                            //pendingDataQueue에 저장함.
                                            addPendingData(order);
                                        }
                                    }
                                    else{
                                        System.out.println("DataQueue 167 order.getStoreId()"+order.getStore_id());
                                        Iterator<SelectionKey> it = keymap.getKey(order.getStore_id()).iterator();
                                        while(it.hasNext()){
                                            SelectionKey value = it.next();
                                            if(value.equals(key)){
                                                System.out.println("DataQueue order.getStoreId()"+order.getStore_id());
                                                keymap.getKey(order.getStore_id()).remove(value);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println(String.format("loi : %s", e.getMessage()));
                                    System.err.println(String.format("channel is opened : %s", clientChannel.isOpen()));
                                    System.err.println(String.format("channel is connected : %s", clientChannel.isConnected()));


                                    keymap.removeKey(order.getStore_id());
                                }
                            }
                        }


                    } else {
                        //NOTE: client가 접속해 있지 않은 상태에서, 주문데이터를 전송받으면,
                        //pendingDataQueue에 저장함.
                        System.err.println(
                                String.format("could not find key from keymap (%s) ",
                                        order.getStore_id(),
                                        keymap.print()
                                ));
                        addPendingData(order);
                    }
                }
            }
        }
    }
}

