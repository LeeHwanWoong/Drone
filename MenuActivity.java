package com.example.leehwanwoong.droneappcontroller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.FragmentManager;
/**
 * Created by LeeHwanWoong on 2017. 6. 1..
 */

public class MenuActivity extends AppCompatActivity implements OnMapReadyCallback {
    double latitude = 37.56, longitude = 126.97;
    String IP;
    int PORT,curCounter = 1;
    Handler mHandler = null;
    DataOutputStream dos;
    Socket socket;
    GoogleMap gMap;
    MarkerOptions markerOptions;
    LatLng current = new LatLng(latitude,longitude);
    boolean isON,soonON;
    String[] soon;

    protected  void onStop(){
        super.onStop();
        try{
            dos.close();
            socket.close();
        }  catch (IOException e){}
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView lati = (TextView) findViewById(R.id.textView);
        final TextView longi = (TextView) findViewById(R.id.textView2);
        Button send = (Button) findViewById(R.id.button3);
        Button location = (Button) findViewById(R.id.button2);
        Button video = (Button)findViewById(R.id.video);
        Button land = (Button) findViewById(R.id.button5);
        ToggleButton follow = (ToggleButton) findViewById(R.id.toggleButton);
        ToggleButton sunchal = (ToggleButton)findViewById(R.id.toggleButton2);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intet = getIntent();
        String tmp = (String)intet.getSerializableExtra("temp");
        String[] temp = tmp.split(" ");
        IP = temp[0];
        PORT = Integer.parseInt(temp[1]);

        soon = new String[4];
        soon[0] = "37.600698 126.866228";
        soon[1] = "37.600649 126.866431";
        soon[2] = "37.600823 126.866584";
        soon[3] = "37.600905 126.866305";

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                current = new LatLng(latitude,longitude);
                markerOptions.position(current);
                gMap.addMarker(markerOptions);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);

        mHandler = new Handler();

        setSocket.start();

        video.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                startActivity(intent);
            }

        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lati.setText("위도 : " + Double.toString(latitude));
                longi.setText("경도 : " + Double.toString(longitude));
                current = new LatLng(latitude,longitude);
                gMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                gMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }
        });

        follow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    isON = true;
                    if(!soonON){
                        autoSend autotemp = new autoSend();
                        autotemp.start();
                    }
                    else{
                    }
                }
                else{
                    isON = false;
                }
            }
        });

        sunchal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    soonON = true;
                    soonchal tmp = new soonchal();
                    tmp.start();
                }
                else{
                    soonON = false;
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(!soonON){
                    checkUpdate tmp = new checkUpdate();
                    tmp.start();
                }
                else{
                    Toast.makeText(MenuActivity.this,"순찰중입니다..",Toast.LENGTH_SHORT).show();
                }

            }
        });

        land.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                landing tmp = new landing();
                tmp.start();
                Toast.makeText(MenuActivity.this,"착륙시작",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Thread setSocket = new Thread(){
        public void run(){
            try{
                socket = new Socket(IP,PORT);
                OutputStream output = socket.getOutputStream();
                dos = new DataOutputStream(output);
                mHandler.post(connetSucces);
            }catch (IOException e1){
                finish();
                e1.printStackTrace();
                finish();
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        markerOptions = new MarkerOptions();
        markerOptions.position(current);
        markerOptions.title("현재 위치 - "+curCounter);
        curCounter++;
        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        gMap = googleMap;
        gMap.addMarker(markerOptions);
    }

    public class checkUpdate extends Thread{
        public void run(){
            try{
                dos.writeUTF(Double.toString(latitude) + " " + Double.toString(longitude));
                mHandler.post(showUpdate);
            } catch(Exception e){}
        }
    }

    public class autoSend extends Thread{
        public void run(){
            while(isON) {
                try {
                    dos.writeUTF(Double.toString(latitude) + " " + Double.toString(longitude));
                } catch (Exception e) {}
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e){}
            }
        }
    }

    public class soonchal extends Thread{
        public void run(){
            int i = 0;
            while(soonON == true){
                try {
                    dos.writeUTF(soon[i%4]);
                    i++;
                }catch(Exception e){}
                try{
                    Thread.sleep(3000);
                }catch (InterruptedException e){}
            }
        }
    }

    public class landing extends Thread{
        public void run(){
            try {
                dos.writeUTF("q q");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable connetSucces = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MenuActivity.this,"connect succes",Toast.LENGTH_LONG).show();
        }
    };

    private Runnable showUpdate = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MenuActivity.this,"sending...",Toast.LENGTH_SHORT).show();
        }
    };
}


