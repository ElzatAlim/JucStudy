import java.util.concurrent.*;

/**
 * @author 艾力
 * @date 2023/3/6 19:53
 * CompletableFuture 类 Future类的强化版本
 *
 **/
public class B_CompletableFutureBuildDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //默认使用自己的线程池ForkJoinPool
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
        },threadPool);
        System.out.println(completableFuture.get());
        System.out.println("-----------------------------------------");

        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
            return "我们就是你想要的结果";
        }, threadPool);

        System.out.println(stringCompletableFuture.get());
        threadPool.shutdown();

    }


}
