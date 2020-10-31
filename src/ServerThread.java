

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    Map<IPAddress,Integer> clientInterfaces;
    RouterStateChanger routerStateChanger;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice, Map<IPAddress,Integer> clientInterfaces,RouterStateChanger routerStateChanger) {

        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        this.clientInterfaces = clientInterfaces;
        this.routerStateChanger = routerStateChanger;

        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;

//        sendMessageToClient();

        new Thread(this).start();
    }

    public void sendMessageToClient(){

        ArrayList sendToClient = new ArrayList<>();
        sendToClient.add(this.endDevice);
        sendToClient.add(this.clientInterfaces);

        this.networkUtility.write(sendToClient);
    }

    @Override
    public void run() {
        sendMessageToClient();
        /**
         * Synchronize actions with client.
         */
        
        /*

        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
//        if (this.networkUtility.read()!=null) readFromClient();
        Packet p;
        boolean delivered;
        try{
            for (int i=0;i<10;i++){
               p =  readFromClient();
               if (p!=null){
                   delivered = deliverPacket(p);
                   if (delivered){
                       this.networkUtility.write("Packer Delivered");
                   }
                   else {
                       this.networkUtility.write("Packet Not Delivered");
                   }
               }

            }
        } catch (Exception e){
            System.out.println("Recipient not found!");
        }

//        readFromClient();

    }

    public Packet readFromClient(){

        String message = null;
        IPAddress recipientIp=null;

        Map<String,IPAddress> receiveMessage = (Map)this.networkUtility.read();

        for (Map.Entry<String,IPAddress> entry:receiveMessage.entrySet()){
            message = entry.getKey();
            recipientIp = entry.getValue();
        }
//        System.out.println("Message: "+message);
//        System.out.println("Recipient :"+recipientIp);
        if (message.equals(null)) return null;
//        System.out.println("Recipient IP address: "+recipientIp);


        Packet packet = new Packet(message,"SHOW_ROUTE",endDevice.getGateway(),(IPAddress)recipientIp);
        return packet;
    }

    public Boolean deliverPacket(Packet p) {


//        System.out.println(source+" "+destination);

        
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        */
        int source = Integer.parseInt(p.getSourceIP().toString().split("\\.")[2]);

        /*
        2. Find the router d which has an interface

                such that the interface and destination end device have same network address.
        */
        int destination = Integer.parseInt(p.getDestinationIP().toString().split("\\.")[2]);
        System.out.println("Distance from source to Destination: "+NetworkLayerServer.routerMap.get(source).getRoutingTable().get(destination-1).getDistance());
        /*
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.

                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */
//        RouterStateChanger.islocked = true;
        boolean delivered = true;
        int currentRouter = source;
        double hop = 0;
//        boolean checkPathAvailable = true;
        ArrayList<Integer> routerList = new ArrayList<>();

        while (currentRouter != destination){
            System.out.println("Current Router : "+currentRouter);
            double distanceSourceFromDestination = NetworkLayerServer.routerMap.get(currentRouter).getRoutingTable().get(destination-1).getDistance();
            if (distanceSourceFromDestination == Constants.INFINITY) {
                System.out.println("path not available");
                delivered = false;
                break;
            }

            int gatewayRouterId = NetworkLayerServer.routerMap.get(currentRouter).getRoutingTable().get(destination-1).getGatewayRouterId();
            boolean gateWayRouterState = NetworkLayerServer.routerMap.get(gatewayRouterId).getState();

            if (!gateWayRouterState){
                System.out.println("Packet Dropped!");
                NetworkLayerServer.routerMap.get(currentRouter).getRoutingTable().get(gatewayRouterId-1).setDistance(Constants.INFINITY);
                RouterStateChanger.islocked = true;
                NetworkLayerServer.DVR(gatewayRouterId);
                RouterStateChanger.islocked = false;
                delivered = false;
                break;
            }

            hop += NetworkLayerServer.routerMap.get(currentRouter).getRoutingTable().get(gatewayRouterId-1).getDistance();

            routerList.add(currentRouter);

            if (!NetworkLayerServer.routerMap.get(currentRouter).getState()){
                NetworkLayerServer.routerMap.get(gatewayRouterId).getRoutingTable().get(currentRouter-1).setDistance(1);
                RouterStateChanger.islocked = true;
                NetworkLayerServer.DVR(gatewayRouterId);
                RouterStateChanger.islocked = false;
            }
            NetworkLayerServer.routerMap.get(currentRouter).printRoutingTable();
            currentRouter = gatewayRouterId;


        }
//        findDistance(source,destination);
//        NetworkLayerServer.printRoutingTable();
        System.out.println("Total hop :"+ hop);
        p.setHopcount((int) hop);

        return delivered;
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
