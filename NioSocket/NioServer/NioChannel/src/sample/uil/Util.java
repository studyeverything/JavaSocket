package sample.uil;

import sample.reponse.BasePacket;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Created by nguye on 7/20/2017.
 */
public class Util {
    public static synchronized byte[] serializeObject(BasePacket data) throws IOException {
        System.out.println("Util serializeObject(BasePacket data)");
        byte[] bytes = null;

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bout);) {

            oos.writeObject(data);
            oos.flush();
//			oos.reset();

            bytes = bout.toByteArray();
            bout.close();
            oos.close();
        }
        return bytes;
    }

    public static synchronized BasePacket deserializeObject(byte[] data) throws Exception {

        BasePacket receiveData = null;

        if (data.length > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));) {
                receiveData = (BasePacket) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
//				e.printStackTrace();
                System.err.println("Util data : " + String.valueOf(data));
                System.err.println("Util length throw e; : " + data.length);
                throw e;
            }
        }

        return receiveData;
    }

    public static String getProcessID() {
        ManagementFactory.getRuntimeMXBean();
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        return rt.getName();
    }
}

