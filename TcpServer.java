import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Date;

class Deg2UTM
{
    double Easting;
    double Northing;
    int Zone;
    char Letter;

    void  Deg2UTM(double Lat,double Lon)
    {
        Zone= (int) Math.floor(Lon/6+31);
        if (Lat<-72)
            Letter='C';
        else if (Lat<-64)
            Letter='D';
        else if (Lat<-56)
            Letter='E';
        else if (Lat<-48)
            Letter='F';
        else if (Lat<-40)
            Letter='G';
        else if (Lat<-32)
            Letter='H';
        else if (Lat<-24)
            Letter='J';
        else if (Lat<-16)
            Letter='K';
        else if (Lat<-8)
            Letter='L';
        else if (Lat<0)
            Letter='M';
        else if (Lat<8)
            Letter='N';
        else if (Lat<16)
            Letter='P';
        else if (Lat<24)
            Letter='Q';
        else if (Lat<32)
            Letter='R';
        else if (Lat<40)
            Letter='S';
        else if (Lat<48)
            Letter='T';
        else if (Lat<56)
            Letter='U';
        else if (Lat<64)
            Letter='V';
        else if (Lat<72)
            Letter='W';
        else
            Letter='X';
        Easting=0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(Lat*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2)/3)+500000;
        Easting=Math.round(Easting*100)*0.01;
        Northing = (Math.atan(Math.tan(Lat*Math.PI/180)/Math.cos((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))-Lat*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(Lat*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))/(1-Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))+0.9996*6399593.625*(Lat*Math.PI/180-0.005054622556*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+4.258201531e-05*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))/3);
        if(Letter < 'M')
            Northing = Northing + 10000000;
        Northing=Math.round(Northing*100)*0.01;
    }
}

public class TcpServer implements Runnable{
	String latitude = "0";
	String longitude = "0";
	Socket socket_android,socket_ros;
	DataInputStream dis_android,dis_ros;
	DataOutputStream dos_android,dos_ros;
	Deg2UTM utm = new Deg2UTM();
	public void run(){
		try{
			ServerSocket serverSocket_android = new ServerSocket(5000);
			ServerSocket serverSocket_ros = new ServerSocket(7000);
			System.out.println("receiving....");
			while(true){
				System.out.println("android connecting....");
				socket_android = serverSocket_android.accept();
				try{
					OutputStream out2 = socket_android.getOutputStream();
					dos_android = new DataOutputStream(out2);
					InputStream in2 = socket_android.getInputStream();
					dis_android = new DataInputStream(in2);
				} catch(Exception e){}
				if(socket_android.isConnected()){
					System.out.println("안드로이드 연결 완료");
					break;
				}
			}
			while(true){
				System.out.println("ros connecting");
				socket_ros = serverSocket_ros.accept();
				try{
					OutputStream out = socket_ros.getOutputStream();
					dos_ros = new DataOutputStream(out);
					InputStream in = socket_ros.getInputStream();
					dis_ros = new DataInputStream(in);
				}catch(Exception e){}
				if(socket_ros.isConnected()){
					System.out.println("로스 연결 완료");
					break;
				}
			}
		}catch(Exception e){}
		while(true){
			System.out.println("수신 준비 완료");
			String asd = "a";
			try{
				String tmp = dis_android.readUTF();
				String[] temp = tmp.split(" ");
       				latitude = temp[0];
        				longitude = temp[1];

        				utm.Deg2UTM(Double.valueOf(latitude),Double.valueOf(longitude));

        				asd = Double.toString(utm.Easting) + "+" + Double.toString(utm.Northing) + "!";

        				dos_ros.writeUTF(asd);

			} catch(Exception e){
				latitude = "0";
        				longitude = "0";
			}

			System.out.println(asd);
		}
	}

	public static void main(String[] args){
		Thread asd = new Thread(new TcpServer());
		asd.start();
	}

	static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}
}