package sample;

import sample.ClientChanel;
import sample.ServerNio;

import java.util.Scanner;

public class ReciveDataNio {
    public static void main(String[] args) {

        //Client
        new Thread(new ClientChanel()).start();
    }
}
