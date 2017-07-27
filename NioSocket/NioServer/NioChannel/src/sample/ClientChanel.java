package sample;

import sample.register.StoreRegisterPacket;
import sample.reponse.StoreBasePacket;
import sample.server.SettingsNioServer;
import sample.uil.Util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by nguye on 7/20/2017.
 */
public class ClientChanel implements Runnable {

    String TAG="ClientChanel ";

    private SocketChannel socketChannel;
    static final int MAX_BUFFER_SIZE = 8192;
    private ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
    private Selector socketSelector;
    private String remoteIP= SettingsNioServer.DEFAULT_NIO_IP;
    private int remotePORT= SettingsNioServer.DEFAULT_NIO_SERVER_PORT;
    String storeId="123";

    public ClientChanel() {
    }

    private int retryCnt = 0;
    static final int DEFAULT_TIMEOUT = 5000;

    private boolean tryConnect2Server() {
        try {

            while (sendRegisterPacket(initConnect()) == -1) {
                retryCnt++;
                Thread.sleep(retryCnt * DEFAULT_TIMEOUT);

                System.out.println(String.format("ClientChanel retry (%d)....", retryCnt));

                if (retryCnt > SettingsNioServer.MAX_RETRY_CNT) {
                    return false;
                }
            }

            System.out.println(TAG+"Success tryConnect2Server");
            retryCnt = 0;
            return true;

        } catch (InterruptedException e) {
            System.out.println(TAG+ e.getMessage());
        }

        return false;
    }

    private SocketChannel initConnect() {

//		if (socketChannel != null) socketChannel.close();
        System.out.println(TAG+ String.format("ClientChanel remote's ip : %s, remote's port:%d", remoteIP, remotePORT));

        socketChannel = null;

        try {
            socketSelector = Selector.open();


            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(remoteIP), remotePORT);
//            InetSocketAddress addr = new InetSocketAddress(remoteIP, remotePORT);

//            socketChannel = SocketChannel.open(addr);
            socketChannel = SocketChannel.open();
            int timeout = DEFAULT_TIMEOUT;
            if (retryCnt > 1) {
                timeout = (retryCnt - 1) * DEFAULT_TIMEOUT;
            }
            socketChannel.socket().connect(addr, timeout);

            socketChannel.configureBlocking(false);
            socketChannel.register(socketSelector, SelectionKey.OP_READ);

        } catch (ConnectException e) {
            //server socket에 열려있지 않은 경우
            System.out.println(TAG+ "ClientChanel could connect to the server");
            return (socketChannel = null);
        } catch (IOException e) {
            System.out.println(TAG+"ClientChanel could connect to the server IOException");
            return (socketChannel = null);
        }

        System.out.println(TAG+ "ClientChanel established successful.");

        return socketChannel;
    }


    private int sendRegisterPacket(SocketChannel channel) {

        if (channel == null) {
             System.out.println(TAG+ "channel is null...");
            return -1;
        }


        StoreRegisterPacket pkt = new StoreRegisterPacket(storeId);
        try {
            ByteBuffer buf = ByteBuffer.wrap(Util.serializeObject(pkt));
            if (channel.write(buf) == -1) {
                 System.out.println(TAG+ "failed to write..");
            }
        } catch (IOException e) {
             System.out.println(TAG+ e.getMessage());
            return -1;
        }
         System.out.println(TAG+ "sent register packet...");
        return 0;
    }

    @Override
    public void run() {
        tryConnect2Server();

        while (true) {
            try {
                System.out.println("NioClient runnable");
                socketSelector.select();
                Iterator<SelectionKey> selectedKeys = socketSelector.selectedKeys().iterator();
                System.out.println("NioClient selectedKeys");
                while (selectedKeys.hasNext()) {
                    System.out.println("NioClient selectedKeys.hasNext()");

                    SelectionKey key = selectedKeys.next();
                    if (key.isReadable()) {
                         System.out.println(TAG+ "read....");
                        this.read(key);
                    } else if (key.isWritable()) {
                         System.out.println(TAG+ "writable....");
                    } else {
                         System.out.println(TAG+ "other....");
                    }

                    selectedKeys.remove();
                }//while

            } catch (IOException e) {
                System.out.println(TAG+ e.getMessage());
            }
        }
    }


    private void read(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();

        int len = -1;
        buffer.clear();

        try {
            len = channel.read(buffer);
        } catch (IOException e) {
             System.err.println(TAG+ "server closed forcibly.."+e);
            key.cancel();
            try {
                channel.close();
            } catch (IOException e1) {
                 System.out.println(TAG+ e.getMessage());
            }
            return;
        }

        if (len == -1) {
             System.out.println(TAG+ "server shutdown and try to reconnect....");

            if (tryConnect2Server()) {
                socketSelector.wakeup();
            }
            return;
        }

        buffer.flip();
        StoreBasePacket recv_data = null;
        try {
            recv_data = (StoreBasePacket) Util.deserializeObject(buffer.array());
        } catch (IOException e) {
             System.out.println(TAG+ e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

         System.out.println(TAG+ "received from server...");
        if (recv_data != null) {

             System.out.println(TAG+ String.format("[%s] (%d) received data - %s ",
                    Thread.currentThread().getName(),
                    len, recv_data.toString()));
        }
    }
}
