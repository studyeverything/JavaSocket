package sample;

import java.util.Scanner;

/**
 * Created by nguye on 7/20/2017.
 */
public class Main {
    public static void main(String[] args) {

        String storeId="123";
        Scanner scanner=new Scanner(System.in);

        while(true){
            int i=scanner.nextInt();
            if (i!=0){
                new ServerNio().sendOrderData2NioServer(i,storeId);
                System.out.println("storeId = [" + storeId + "]");
            }
        }
    }
}
