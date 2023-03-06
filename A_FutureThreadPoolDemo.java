import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author 艾力
 * @date ${DATE} ${TIME}
 **/
public class A_FutureThreadPoolDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        FutureTask<String> futureTask = new FutureTask<>(new MyThread());
        futureTask.run();
        System.out.println(futureTask.get());


    }



    static class MyThread implements Callable {


        @Override
        public String call() throws Exception {
            System.out.println("--GO  --");
            return "hello Callable";
        }
    }
}
