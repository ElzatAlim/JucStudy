import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 艾力
 * @date 2023/3/6 20:21
 * CompletableFuture类的使用
 *
 * 成功执行完以后会调我们下一步方法，报错也会会调报错处理方法
 *
 *
 **/
public class C_CompletableFutureUseDemo {
    public static void main(String[] args) {
        ExecutorService myThreadPool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture.supplyAsync(()->{
                System.out.println("    我进来啦！");
                try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
                return "GoGoGo!";
            }).whenComplete((v,e)->{
                System.out.println("    调用远程服务进行Update操作:"+v);
            }).exceptionally((e)->{
                e.printStackTrace();
                return "    执行报错啦！";
            });
            System.out.println(Thread.currentThread().getName()+"我忙去了");
            try {TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) {throw new RuntimeException(e);}
            System.out.println("结束");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            myThreadPool.shutdown();
         }


    }
}
