package Testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<Integer, Integer> test = new HashMap<>();
        ArrayList<Integer> testArray = new ArrayList<>();

        testArray.add(1);
        testArray.add(2);

        System.out.println(testArray.get(1));
//        test.put(1,2);
//        test.put(2,3);
//
////        for( val : test.keySet()):
//
////        System.out.println(test.get(1));
////        test.forEach((key,value) -> System.out.println(value));
//        for (Map.Entry entry : test.entrySet())
//        {
//            System.out.println("key: " + entry.getKey() + "; value: " + entry.getValue());
//        }
    }
}
