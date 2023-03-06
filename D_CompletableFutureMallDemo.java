import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author 艾力
 * @date 2023/3/6 20:21
 * CompletableFuture类的使用-电商项目实战case
 * 获取某个书在多个平台中的价格，查询价格耗时1S
 *
 **/
public class D_CompletableFutureMallDemo {

    static List<NetMall> list = Arrays.asList(new NetMall("京东"),new NetMall("淘宝"),new NetMall("拼多多"));

    /**
     * 老方法      List<NetMall> ----->   List<String>
     */
    public static List<String>  getPrice(List<NetMall> list,String productName){
        return list.stream().map(item->{
            return String.format(productName+" in %s \t price is %.2f", item.getNetMallName(),item.calcPrice(productName));
        }).collect(Collectors.toList());
    }


    /**
     * 新方法      List<NetMall> ----->  ?   ----->  List<String>
     * 注意：join方法跟gei方法一样，只是不用抛出一场
     */
    public static List<String>  getPriceByCompletableFuture(List<NetMall> list,String productName){
        return list.stream().map(item->CompletableFuture.supplyAsync(()->
                String.format(productName + " in %s \t price is %.2f", item.getNetMallName(), item.calcPrice(productName))))
                .collect(Collectors.toList())
                .stream()
                .map(item -> item.join())
                .collect(Collectors.toList());
    }




    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<String> mysql = getPrice(list, "Mysql");
        for (String s : mysql) {
            System.out.println(s);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("总共耗时："+(endTime-startTime)+" 毫秒");
        System.out.println("-------------------------------");
        long startTime1 = System.currentTimeMillis();
        List<String> mysql1 = getPriceByCompletableFuture(list, "Mysql");
        for (String s : mysql1) {
            System.out.println(s);
        }
        long endTime1 = System.currentTimeMillis();
        System.out.println("总共耗时："+(endTime1-startTime1)+" 毫秒");

    }



}

class NetMall {
    public NetMall(String netMallName) {
        this.netMallName = netMallName;
    }

    public String getNetMallName() {
        return netMallName;
    }

    public void setNetMallName(String netMallName) {
        this.netMallName = netMallName;
    }

    private String netMallName;

    public double calcPrice(String productName) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ThreadLocalRandom.current().nextDouble()*2+productName.charAt(0);
    }
}
