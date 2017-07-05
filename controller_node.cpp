#include <iostream>
#include <string>
#include <chrono>
#include <thread>
#include <signal.h>
#include "ros/ros.h"
#include "std_msgs/String.h"

//==================================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <errno.h>
#include <netinet/in.h>
#include <unistd.h>

//=================================================================================

// Ctrl+C를 누르면 이 함수가 호출 됨
void cbSigintHandler(int sig)
{
  // node의 ros를 종료 함
  ros::shutdown();
}

std::vector<std::string> token_split(const std::string& s, char c)
{
    auto end = s.cend();
    auto start = end;

    std::vector<std::string> v;
    for( auto it = s.cbegin(); it != end; ++it ) {
        if( *it != c ) {
            if( start == end )
                start = it;
            continue;
        }
        if( start != end ) {
            v.emplace_back(start, it);
            start = end;
        }
    }
    if( start != end )
        v.emplace_back(start, end);
    return v;
}

int main(int argc, char **argv)
{
  std::string my_name = "controller_node";

  // All node must call this function to communicate with the roscore.
  ros::init(argc, argv, my_name);

  // ctrl+C를 눌렀을 때 프로그램을 종료하도록 이벤트 처리
  signal(SIGINT, cbSigintHandler);

  // 'chatter' 토픽에 대해 subscribe하고, publish 할 수 있도록 설정
  ros::NodeHandle n;

  ros::Publisher  pub_moveX = n.advertise<std_msgs::String>("woong_ros/x_pos", 100);
  ros::Publisher  pub_moveY = n.advertise<std_msgs::String>("woong_ros/y_pos", 100);
  ros::Publisher  pub_moveZ = n.advertise<std_msgs::String>("woong_ros/z_pos", 100);

  std_msgs::String pub_msg; 
 
  // 콘솔 입력할 때 ros로부터 수신되는 이벤트 처리가 안되기 때문에 ros의 이벤트 확인은 thread 처리로 변경
  std::thread t([]() {
     ros::AsyncSpinner spinner(1); // Use 4 threads
     spinner.start();
     ros::waitForShutdown();
  });

  //=============================================================================
    char* CLIENT_IP;
    int CLIENT_PORT;      

    CLIENT_IP = *(argv+1);
    CLIENT_PORT = atoi(*(argv+2));
   
     int client_socket;
          struct sockaddr_in server_address;
          unsigned char Buff[250];
          int check = 3500;


          client_socket = socket(PF_INET,SOCK_STREAM,0);

          if (client_socket == -1)
            {
              printf("Client Socket ERROR");
              exit(0);
            }
          bzero((char *)&server_address, sizeof(server_address));

          server_address.sin_family = AF_INET;
            server_address.sin_port = htons(CLIENT_PORT);
            server_address.sin_addr.s_addr = inet_addr(CLIENT_IP);



          if(connect(client_socket, (struct sockaddr *)&server_address, sizeof(server_address)) == -1)
            {
              printf("Connect ERROR");
              exit(0);

            }

          printf("\nclient socket = [%d]\n\n",client_socket);

//==================================================================================

std::string tmp,tmp2;

while (ros::ok()){
    memset(Buff,0,250);
    char temp[20];
    memset(temp,0,20);
    while(check>3000){
              check = read(client_socket,Buff,sizeof(Buff));
    }

    int i;

    std::cout<<Buff<<std::endl;

    for(i = 0;Buff[i+2] != '+';i++){
        temp[i] = Buff[i+2];
    }
    tmp = temp;
    memset(temp,0,20);
    i++;
    for(int j = 0;Buff[i+2] != '!';i++,j++){
        temp[j] = Buff[i+2];
    }
    tmp2 = temp;

        if (client_socket == -1)
            {
              printf("Disconnection Check\n");
              close(client_socket);
              break;
            }

    std::cout<<"Drone Pos(X Y Z) : ("<<tmp<<" , "<<tmp2<<" , 1.3)"<<std::endl;
     
    pub_msg.data = tmp;
      pub_moveX.publish(pub_msg);
      pub_msg.data = tmp2;
      pub_moveY.publish(pub_msg);
      pub_msg.data ="1.3";
      pub_moveZ.publish(pub_msg);

      check = 3500;
    }

    close(client_socket);

  ros::shutdown();

  t.join();

  return 0;
}
