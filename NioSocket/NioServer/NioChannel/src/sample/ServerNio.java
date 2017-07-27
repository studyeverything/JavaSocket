package sample;

import sample.reponse.BasePacket;
import sample.reponse.OrderMessage;
import sample.reponse.StoreBasePacket;
import sample.uil.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by nguye on 7/20/2017.
 */
public class ServerNio {

    private static InetSocketAddress addr = null;
    private static SocketChannel socketChannel = null;

    final int TIMEOUT = 5000;
    private String nioIp="172.16.0.113";
    private int nioPort=9756;

    private SocketChannel openSocket() {

        if (socketChannel != null) {
            return socketChannel;
        }

        try {
            addr = new InetSocketAddress(nioIp, nioPort);
            socketChannel = SocketChannel.open();
            socketChannel.socket().connect(addr, TIMEOUT);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("NioServer Connection failure");
        }
        return socketChannel;
    }

    public void closeSocket() throws IOException {
        socketChannel.finishConnect();
        socketChannel.close();
        socketChannel = null;
    }


//    private int send2NioServer(OrderData data) throws IOException, InterruptedException {
//
//        SocketChannel localSocket = openSocket();
//        ByteBuffer buf = null;
//        try {
//            buf = ByteBuffer.wrap(Util.serializeObject(data));
//            return localSocket.write(buf);
//        } catch (IOException e) {
//            System.err.println("write 실패.. 재시도 함..");
//            System.err.println(data.toString());
//            closeSocket();
//            Thread.sleep(100);
//            return send2NioServer(data);
//        }
//    }

    private InetSocketAddress getNioServerAddr() {
        return new InetSocketAddress(nioIp,
                nioPort);
    }

//    private int sendMsg2NioServer(CommonData data) {
//
//        try (SocketChannel socketChannel = SocketChannel.open(getNioServerAddr());) {
//            System.out.println("NioServerService nio addr: " + getNioServerAddr());
//            ByteBuffer buf = ByteBuffer.wrap(Util.serializeObject(data));
//            return socketChannel.write(buf);
//        } catch (IOException e) {
//            System.err.println("NioServerService Could not connect to Store Socket Server");
//        }
//
//        return -1;
//    }

    private int sendMsg2NioServer(StoreBasePacket data) {
        try (SocketChannel sockChannel = SocketChannel.open(getNioServerAddr());) {
            ByteBuffer buf = ByteBuffer.wrap(Util.serializeObject(data));
            return sockChannel.write(buf);
        } catch (IOException e) {
            System.err.println("NioServerService Could not connect to Store Socket Server"+e);
        }

        return -1;
    }


    public int sendOrderData2NioServer(int tableId, String storeId) {

        OrderMessage msg = new OrderMessage(storeId);
        msg.setTagId("taggggggggggggggg");
        System.out.println("ServerNio sendOrderData2NioServer  tableId = [" + tableId + "], storeId = [" + storeId + "]");

        return sendMsg2NioServer(msg);
    }
}
