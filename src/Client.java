import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        /**
         * Tasks
         */

        ArrayList receive = (ArrayList)networkUtility.read();
        
        /*
        1. Receive EndDevice configuration from server
        */
        EndDevice endDevice = (EndDevice) receive.get(0);
//        System.out.println(endDevice.getIpAddress());

        /*
        2. Receive active client list from server
        */

        Map<IPAddress,Integer> clientList = (Map)receive.get(1);
//        System.out.println(clientList);

        ArrayList<IPAddress> activeClientList = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<IPAddress, Integer> entry : clientList.entrySet()){

            if (entry.getKey() != endDevice.getGateway() && entry.getValue()>0){
                activeClientList.add(entry.getKey());
            }
        }
//        System.out.println(activeClientList);

        for (int i=0;i<10;i++){

            String message = "Packet number "+i;

            if (activeClientList.size()==0) {
                System.out.println("No active recipient");
                Map<String,IPAddress> sendMessage = new HashMap<>();
                sendMessage.put(null,null);
                networkUtility.write(sendMessage);
                break;
            }

            IPAddress recipientIp = activeClientList.get(random.nextInt(activeClientList.size()));

            System.out.println(message+" "+recipientIp);

            Map<String,IPAddress> sendMessage = new HashMap<>();
            sendMessage.put(message,recipientIp);
            networkUtility.write(sendMessage);

            Thread.sleep(200);
            System.out.println((String)networkUtility.read());
        }

        /*
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message

        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */


    }
}
