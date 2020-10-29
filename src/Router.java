//Work needed
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Router {

    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    private Map<Integer, Router> routerMap;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();
        routerMap = new HashMap<>();
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = false;
        else state = true;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();
        routerMap = new HashMap<>();


        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = interfaceAddresses.size();
    }

    public void setRouterMap(Map routerMap){
        this.routerMap = routerMap;
//        System.out.println(routerMap);
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }



    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {


        for (Object i : routerMap.keySet()){
            int id = (Integer)i;
//            System.out.println(id);
            int routerId = id;
//            if(routerId != this.routerId){
            int distance = Constants.INFINITY;
            Router router = (Router)routerMap.get(i);
            if (routerId == this.routerId){
                distance = 0;
            }

            else if (neighborRouterIDs.contains(id) && router.getState() && routerId != this.routerId){
                distance = 1;
            }


            RoutingTableEntry routingTableEntry1 = new RoutingTableEntry(routerId,distance,routerId);
            routingTable.add(routingTableEntry1);


        }

    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        ArrayList<RoutingTableEntry> routingTable = new ArrayList<>();
        for (Object i : routerMap.keySet()){
//            int id = (Integer)i;
//            System.out.println(id);
            int routerId = (Integer)i;
//            if(routerId != this.routerId){
            int distance = Constants.INFINITY;
            Router router = (Router)routerMap.get(i);
            if (routerId == this.routerId){
                distance = 0;
            }
            else if(this.getNeighborRouterIDs().contains(routerId) && router.getState()){
                distance = 1;
            }

            RoutingTableEntry routingTableEntry = new RoutingTableEntry(routerId,distance,routerId);
            routingTable.add(routingTableEntry);


        }

        this.routingTable = routingTable;
//        this.routingTable = null;
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */

    /*TO:DO
        Without SplitHorizon and Force Update
     */

    public boolean updateRoutingTable(Router neighbor) {
        double finalDistance;
        boolean change = false;

        ArrayList<RoutingTableEntry> routingTable = neighbor.getRoutingTable();

        double distanceToNeighbour = this.routingTable.get(neighbor.getRouterId()-1).getDistance();

        for(RoutingTableEntry obj : routingTable){
            int routerId = obj.getRouterId();

            if (routerId == this.routerId) continue;
//            else if(!this.routerMap.get(routerId).getState()) continue;

            double distanceFromSource = this.routingTable.get(routerId-1).getDistance();
            double distanceFromNeighbour = obj.getDistance();

            if (distanceToNeighbour + distanceFromNeighbour < distanceFromSource ){
                finalDistance = distanceToNeighbour + distanceFromNeighbour;
                this.routingTable.get(routerId-1).setDistance(finalDistance);
                this.routingTable.get(routerId-1).setGatewayRouterId(neighbor.routerId);
                change = true;
            }
        }

        return change;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {

        double tempDistance;
        boolean change = false;

        int sourceId = this.routerId;
        int getWayId = neighbor.getRouterId();

        ArrayList<Integer> neighborIds = neighbor.getNeighborRouterIDs();
        ArrayList<RoutingTableEntry> gateWayRoutingTable = neighbor.getRoutingTable();

//        for(int desRouterId:neighborIds){
//            if (desRouterId == this.routerId) continue;
//            double sourceToDestination = this.routingTable.get(desRouterId-1).getDistance();
//            double gateWayToDestination = gateWayRoutingTable.get(desRouterId-1).getDistance();
//            double sourceToGateWay = this.routingTable.get(neighbor.getRouterId()-1).getDistance();
//
//            tempDistance = sourceToGateWay+gateWayToDestination;
//            System.out.println("temp distance: "+tempDistance);
//            System.out.println("sourceToDes: "+sourceToDestination);
//            if((this.routingTable.get(desRouterId-1).getGatewayRouterId() == getWayId) || (sourceToDestination> tempDistance && (sourceId != gateWayRoutingTable.get(desRouterId-1).getGatewayRouterId()))){
//                this.routingTable.get(desRouterId-1).setDistance(tempDistance);
//                this.routingTable.get(desRouterId-1).setGatewayRouterId(getWayId);
//                change = true;
//            }
//
//        }

        for (RoutingTableEntry destinationRouter : gateWayRoutingTable){

            int destinationRouterId = destinationRouter.getRouterId();
            if (destinationRouterId==this.routerId || destinationRouterId == neighbor.routerId) continue;
            else if(!this.routerMap.get(destinationRouterId).getState()) continue;

            double sourceToDestination = this.routingTable.get(destinationRouterId-1).getDistance();
            double sourceToGateway = this.routingTable.get(neighbor.getRouterId()-1).getDistance();
            double gatewayToDestination = destinationRouter.getDistance();

            tempDistance = sourceToGateway+gatewayToDestination;
//            System.out.println("temp distance: "+tempDistance);
//            System.out.println("sourceToDes: "+sourceToDestination);
            if((this.routingTable.get(destinationRouterId-1).getGatewayRouterId() == getWayId) || (sourceToDestination> tempDistance && (sourceId != gateWayRoutingTable.get(destinationRouterId-1).getGatewayRouterId()))){
                this.routingTable.get(destinationRouterId-1).setDistance(tempDistance);
                this.routingTable.get(destinationRouterId-1).setGatewayRouterId(getWayId);
                change = true;
            }

        }

        return change;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */

    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }

    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
