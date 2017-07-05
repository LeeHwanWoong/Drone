#include<iostream>

#include <ros/ros.h>
#include <std_msgs/String.h>
#include <geometry_msgs/PoseStamped.h>
#include <mavros_msgs/CommandBool.h>
#include <mavros_msgs/SetMode.h>
#include <mavros_msgs/State.h>
#include <sensor_msgs/NavSatFix.h>
mavros_msgs::State              g_current_state;
geometry_msgs::PoseStamped     g_pose;
sensor_msgs::NavSatFix Na;
void cbState(const mavros_msgs::State::ConstPtr& msg)
{
    g_current_state = *msg;

    std::cout << "\n[MANSOO] state_cb(), -----------";
    std::cout << "\n          g_current_state.connected = " << ((g_current_state.connected) ? "OK!" : "Not yet!");
    std::cout << "\n          g_current_state.armed = " << ((g_current_state.armed ) ? "OK!" : "Not yet!");
    std::cout << "\n          g_current_state.guided = " << ((g_current_state.guided) ? "OK!" : "Not yet!");
    std::cout << "\n          g_current_state.mode = " << g_current_state.mode;
    std::cout << "\n[MANSOO] ------------------------\n";
}
void cbgps(const sensor_msgs::NavSatFix::ConstPtr& msg){
   Na = *msg;
    std::cout << "\n           latitude= " << Na.latitude;
    std::cout << "\n          longitude = " << Na.longitude;
    std::cout << "\n           altitude= " << Na.altitude;
}

void cbMoveX(const std_msgs::String::ConstPtr& msg)
{
  std::string::size_type sz;   // alias of size_t

  int pos = std::stoi (msg->data, &sz);
  std::cout << "x : " <<pos << std::endl;
  g_pose.pose.position.x = pos;
}

void cbMoveY(const std_msgs::String::ConstPtr& msg)
{
  std::string::size_type sz;   // alias of size_t

  int pos = std::stoi (msg->data, &sz);
  std::cout << "y: " <<pos << std::endl<<std::endl;
  g_pose.pose.position.y = pos;
}

void cbMoveZ(const std_msgs::String::ConstPtr& msg)
{
  std::string::size_type sz;   // alias of size_t

  int pos = std::stoi (msg->data, &sz);

  g_pose.pose.position.z = pos;
}

int main(int argc, char **argv)
{
    ros::init(argc, argv, "mansoo_offboard_node");
    ros::NodeHandle nh;




    ros::ServiceClient  set_mode_client = nh.serviceClient<mavros_msgs::SetMode>("mavros/set_mode");

    ros::ServiceClient  arming_client   = nh.serviceClient<mavros_msgs::CommandBool>("mavros/cmd/arming");

   ros::Subscriber     state_sub_gps        = nh.subscribe<sensor_msgs::NavSatFix>("mavros/global_position/global", 10, cbgps);
   ros::Subscriber     state_sub        = nh.subscribe<mavros_msgs::State>("mavros/state", 10, cbState);
   ros::Publisher      local_pos_pub    = nh.advertise<geometry_msgs::PoseStamped>("mavros/setpoint_position/local", 10);
    ros::Subscriber     move_x_sub        = nh.subscribe("mansoo_drone/x_pos", 10, cbMoveX);
    ros::Subscriber     move_y_sub        = nh.subscribe("mansoo_drone/y_pos", 10, cbMoveY);
    ros::Subscriber     move_z_sub        = nh.subscribe("mansoo_drone/z_pos", 10, cbMoveZ);

    //the setpoint publishing rate MUST be faster than 2Hz
    ros::Rate rate(20.0);

    // wait for FCU connection
    while(ros::ok() && g_current_state.connected){
        ros::spinOnce();
        rate.sleep();
    }


    //send a few setpoints before starting
    for(int i = 100; ros::ok() && i > 0; --i)
    {
        local_pos_pub.publish(g_pose);
        ros::spinOnce();
        rate.sleep();
    }

    mavros_msgs::SetMode offb_set_mode;
    offb_set_mode.request.custom_mode = "OFFBOARD";

    mavros_msgs::CommandBool arm_cmd;
    arm_cmd.request.value = true;

    ros::Time last_request = ros::Time::now();

    while(ros::ok())
    {


        ros::spinOnce();
        rate.sleep();
    }

    return 0;
}
