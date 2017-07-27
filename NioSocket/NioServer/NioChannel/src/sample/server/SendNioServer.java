package sample.server;


import sample.reponse.StoreBasePacket;
import sample.uil.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nguye on 7/20/2017.
 */
public class SendNioServer implements Runnable {


    private Selector socketSelector;
    private ServerSocketChannel serverSocketChannel;

    private ByteBuffer buffer = ByteBuffer.allocate(SettingsNioServer.MAX_BUFFER_SIZE);

    static AtomicInteger atomicInteger = new AtomicInteger(1);

    //가맹점주, key 저장
    private StoreKeyMap storeKeyMap;

    private DataQueue dataQueue;

    public SendNioServer(StoreKeyMap keymap, DataQueue queue) throws IOException {
        System.out.println("keymap = [" + keymap + "], queue = [" + queue + "]");
        this.storeKeyMap = keymap;
//		this.dataQueue = new DataQueue(keymap);
        this.dataQueue	= queue;


        this.socketSelector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        InetSocketAddress addr = new InetSocketAddress(SettingsNioServer.DEFAULT_NIO_SERVER_PORT);
        serverSocketChannel.socket().bind(addr);
        serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
    }

    private void accept(SelectionKey key){

        System.out.println(this.getClass().getSimpleName()+ " accept(SelectionKey key) key = " + key);
        // TODO NTT For an accept to be pending the channel must be a server socket channel.
        serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel;
        try {
            clientChannel = serverSocketChannel.accept();

            clientChannel.configureBlocking(false);
            clientChannel.register(socketSelector, SelectionKey.OP_READ);
            System.out.println("SendNioServer clientChannel.register(socketSelector, SelectionKey.OP_READ); accept...."+ clientChannel.socket().getRemoteSocketAddress());
        } catch (IOException e) {
            System.out.println("SendNioServer catch (IOException e)"+e.getMessage());
        }
    }

    private void read(SelectionKey key){
        System.out.println("SendNioServer read(SelectionKey key)" );
        buffer.clear();
        SocketChannel ch = (SocketChannel)key.channel();

        int len;

        try{
            len = ch.read(buffer);
            System.out.println("SendNioServer  len = ch.read(buffer); len:"+len );
        }catch(IOException e){
            //remote forcibly closed the connection,
            //cancel the selectionkey and close the channel
            System.err.println("SendNioServer remote closed forcibly");
            System.err.println("SendNioServer  Key closed keystring=" + key.toString()+" key "+key.toString());
            key.cancel();
            try {
                ch.close();
            } catch (IOException e1) {
                System.err.println( "SendNioServer  catch (IOException e1) "+e1.getMessage());
            }
            return;
        }

        if (len == -1){
            //client shut down the socket.
            //do the same from our end and cancel the channel
            System.err.println("SendNioServer  remote shutdown..");
            try {
                key.channel().close();
            } catch (IOException e) {
                System.err.println("SendNioServer  Loi cat -1-1-1 } catch (IOException e) {"+e.getMessage());
            }
            key.cancel();
//			continue;
            return;
        }
        System.out.println("SendNioServer  buffer.flip();" );
        buffer.flip();

        try{
            //가맹점 App으로부터 등록 packet를 수신하면,
            //storeMap에 가맹점명과 Selectionkey를 저장해서, 데이터가 있을 시, 전송할 수 있도록 한다.
            StoreBasePacket recv_pkt = (StoreBasePacket) Util.deserializeObject(buffer.array());
            System.out.println("SendNioServer received data: " + recv_pkt.toString() +" recv_pkt type:"+recv_pkt.getType());
            switch(recv_pkt.getType()){
                case STORE_REGISTER:
                    System.out.println("SendNioServer regiter");
                    System.out.println("SendNioServer Key regiter key.toString()=" +  key.toString() +" key" +key);
                    //client 등록 패킷 
                    storeKeyMap.addKey(recv_pkt.getStore_id(), key);
                    System.out.println("SendNioServer 119  storeKeyMap.addKey(recv_pkt.getStoreId(), key);");
                    //pendingdata가 있으면, 바로 전송 
                    dataQueue.sendPendingData(recv_pkt.getStore_id(), ch);
                    System.out.println("SendNioServer 122 dataQueue.sendPendingData(recv_pkt.getStoreId(), ch);");
                    break;

                //		case ORDER_DATA_FROM_REST:
                //			//REST로부터 주문데이터를 수신함.....
                //			//TODO: Queue로 push하고, thread에서  polling해서 전송 
                //			log("ORDER_DATA_FROM_REST 데이터 수신 ");
                //
                //			OrderDataFromREST src = (OrderDataFromREST)recv_pkt;
                //			
                //			OrderData orderdata = (OrderData)src.clone();
                //			orderdata.setPktType(PacketType.ORDER_DATA);
                //			orderdata.seqNo = atomicInteger.incrementAndGet();
                //
                //			dataQueue.addData(orderdata.getStoreName(), orderdata);
                //			break;

                //		case COMPLETE_PAYMENT:
                //			//REST로부터 결제 완료 메시지를 수신함.
                //			log("COMPLETE_PAYMENT 데이터 수신 ");
                //			
                //			dataQueue.addData(recv_pkt.getStoreName(), recv_pkt);
                //			break;


                case PREPAY_ORDER_DATA:
                    System.out.println("SendNioServer PREPAY_ORDER_DATA");
                    System.out.println("SendNioServer PREPAY_ORDER_DATA 데이터 수신" +recv_pkt.toString());
                    dataQueue.addData(recv_pkt.getStore_id(), recv_pkt);
                    break;

//                case REQUEST_CANCEL_ORDER:
//                    System.out.println("REQUEST_CANCEL_ORDER");
//                    System.out.printf("REQUEST_CANCEL_ORDER received message");
//                    dataQueue.addData(recv_pkt.getStoreId(), recv_pkt);
//                    break;
//                case REPLY_CANCEL_ORDER:
//                    System.out.println("REPLY_CANCEL_ORDER");
//                    System.out.printf("REPLY_CANCEL_ORDER received message");
//                    dataQueue.addData(recv_pkt.getStoreId(), recv_pkt);
//                    break;
                default:
                    System.out.println("SendNioServer Unknown Pakcet Type: " + recv_pkt);
                    break;
            }

            System.out.println("SendNioServer end swith case");
        }catch(Exception e){
            System.err.println("SendNioServer Loi roi ne xem di  }catch(Exception e){ " + e);
            return;
        }
    }



    public void run(){
        try{

            while(true){

                socketSelector.select();
                Iterator<SelectionKey> selectedKeys = socketSelector.selectedKeys().iterator();
                while(selectedKeys.hasNext()){
                    SelectionKey key = selectedKeys.next();

                    System.out.println("SendNioServer key ne key" + key.toString());
                    selectedKeys.remove();

                    if (key.isValid() == false) continue;

                    if (key.isAcceptable()){
                        System.out.println("SendNioServer key.isAcceptable() " + key);
                        this.accept(key);
                    }
                    else if (key.isReadable()){
                        this.read(key);
                    }
                }//while

            }
        }catch(IOException  e){
            System.err.println("SendNioServer }catch(IOException  e){ " +e);
        }
    }


//	private static void log(String msg){
//		System.out.println("[SendNioServer] - " + msg);
//	}
//	
//	private static void error(String msg){
//		System.err.println("[SendNioServer] - " + msg);
//	}

    public static void main(String[] args) throws IOException{
        System.out.printf("Main");
        StoreKeyMap keymap = new StoreKeyMap();
        DataQueue queue = new DataQueue(keymap);
        SendNioServer server = new SendNioServer(keymap, queue);
        new Thread(server).start();
    }


//	public static void main2(String[] args) throws IOException, InterruptedException{
//		
//		StoreKeyMap map = new StoreKeyMap();
//		
//		SendNioServer server = new SendNioServer(map);
//		new Thread(server).start();
//		
//		Thread.sleep(2000);
//		
//		while(map.getKey("QR1")==null){
//			System.out.println("등록패킷 대기 중..");
//			System.out.println(map.print());
//			Thread.sleep(1000);
//		}
//		
//		DataQueue queue = new DataQueue(map);
//		
//		
//		AtomicInteger cnt = new AtomicInteger(1);
//		
//		while(true){
//			
////			String msg = String.format("melong- %s", cnt.incrementAndGet());
//			OrderData data = new OrderData("QR1", "#1");
//			data.seqNo = cnt.incrementAndGet();
//			data.addOrderItem("김치찌개", 1, 7000);
//			data.addOrderItem("밥공기", 1, 1000);
//
////			if (cnt.incrementAndGet() %2 == 0) {
//				queue.addData("QR1", data);
////			} else {
////				data.setStoreName("QR2");
////				queue.addData("QR2", data);
////			}
//			
//			Thread.sleep( new Random().nextInt(2000)  );
//		}
//		
//	}

}
