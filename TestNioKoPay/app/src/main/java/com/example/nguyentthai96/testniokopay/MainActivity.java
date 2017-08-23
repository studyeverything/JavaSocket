package com.example.nguyentthai96.testniokopay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.thlsoft.nioserver.receive.PosReceiveDataNio;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onClick(View view) {

        //Client ReceiveData
        PosReceiveDataNio posReceiveDataNio=new PosReceiveDataNio("tagId123123");
        new Thread(posReceiveDataNio).start();
        posReceiveDataNio.setResponseListener(new PosReceiveDataNio.ResponseListener() {
            @Override
            public void onSucces(boolean confirmCustomer) {
                System.out.println("MainClientReceive confirmCustomer = [" + confirmCustomer + "]");
                Toast.makeText(MainActivity.this, confirmCustomer+"", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String error) {

            }
        });
    }
}
