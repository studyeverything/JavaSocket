package sample.server;

/**
 * Created by nguye on 7/20/2017.
 */
public class SettingsNioServer {
    public static final int DEFAULT_NIO_SERVER_PORT = 9756;
    public  static String DEFAULT_NIO_IP="172.16.0.113";

    /** 최대 대기 시간 10초 */
    public  static final int DEFAULT_TIMEOUT = 10000;

    public  static final int MAX_SIZE_QUEUE = 100;

    public  static final int MAX_BUFFER_SIZE = 8192;
    public  static int MAX_RETRY_CNT = 100;
}
