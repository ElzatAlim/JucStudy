volatile
	解释：是个修饰符，能解决多线程内存不可见的问题， 对于一写多读，是可以解决变量同步问题，但是如果多写同样会有线程安全问题；
	防止指令重排：禁止编译器和cpu进行重排，通过屏障，告诉编译器屏障中间部分不能跟上下面的代码进行重排，需要跟我们源代码保持一致
	可见性：值被改变时，会通知其他线程，他们内存中的值过期了，需要重新从主内存中获取最新的值
	无法保证原子性：a++，a+1等操作会有问题：A，B线程读取了3，都自增了，存回去，这个时候A会把3改成4，B会把4改成4，就会有问题。下面cas来解决

CAS 
	解释：CAS的全称为Compare-And-Swap，直译就是对比交换。是一条CPU的原子指令，其作用是让CPU先进行比较两个值是否相等，然后原子地更新某个位置的值。其实现方式是基于硬件平台的汇编指令，就是说CAS是靠硬件实现的，
		JVM只是封装了汇编调用（Java.util.Concurrent.Atomic unsafe类native方法），那些AtomicInteger类便是使用了这些封装后的接口。
	解释：原子类，通过Java.util.Concurrent.Atomic类的API，更新值时compareAndSet方法通过unsafe类native方法，类似指针操作一样，在存储之前判断值内存地址中的值是否跟我们预期值一样，如果不一样说明被改过，会重新进行处理，再尝试；
	优点：就是避开Synchronized重量级的锁，通过底层unsafe方法，实现一个轻量级的加锁方式。
	缺点：do while循环长时间不成功，等待时间太长的话，会导致cpu开销过多；可以使用JDK1.8以后新增的的几个类LongAdder，减少乐观锁的重试次数，性能更好（如果无竞争的话跟AtomicLong一样，当出现竞争关系时，会新增一个cells数组，根据线程id进行hash的到hash值，在通过这个hash值对该下标的进行自增操作，最后将数组cells）
		会导致“ABA”问题，ABA：CAS只管结果，不管过程是否有改变：线程A读取，更新，再读取，又更新到跟之前一样的值； 解决办法就是加时间戳或者版本号，使用AtomicStampedReference，AtomicMarkableReference(有没有被改过简化为ture/false)

	可以通过AtomicRefrence来实现自旋锁，不需要Sync；原理就是两个方法：lock（如果是null，就改成当前对象），unlock（如果是当前对象，改成null）方法；
	可以通过AtomicRefrenceFiledUpdater字段级别的更新某个对象的属性，锁粒度细化了；需要更新的对象必须是public volatile修饰；

ThreadLocal
	解释：可以说是线程的局部变量，并不是解决线程间共享数据的问题，是用于变量在线程间隔离且在方法间共享的场景；底层原理就是Thread类里的ThreadLocal属性包含静态内部类ThreadLocalMap；实际上就是以threadLocal实例为key（使用的是弱引用，方法执行完以后顺利的被回收，减少内存泄漏的问题；
		下次调用get或者remove方法是会清掉key为null的键值对，但是无法百分百保证,所以执行完需要手动remove一下，也可以避免下一个线程获取到上个线程遗留下来的value），任意对象为value的Entry对象
	用法：给对象添加一个ThreadLocal<T> name = ThreadLocal.withinitial(()->0); 然后通过它get和set方法来修改；


内存泄漏：
	解释：有很多已经不需要的对象一直占着内存，没办法被gc清掉。
	强引用：默认； 	软引用(softReference)：内存不够时才会被回收； 
	弱引用(WeakReference)：不管内存够不够用，垃圾回收机制一运行就回收该对象占用的内存；
	虚引用(PhantomReference(T,队列))：就是个虚设，在任何时候会被垃圾回收机制回收掉。get方法总是返回null，主要是用来回收时被finalize以后会被放到队列中，做某些事情的通知机制；

java对象布局：对象头，实例数据，填充对齐；
	对象头（对象标记MarkWord 8k+类型指针8k）：对象标记：哈希吗，GC标记，GC次数(最大值：1111，到15次就到年老代)，同步锁标记，偏向锁持有者；默认大小是16K，开启压缩指针时12K但是还是会被填充对其到16；类型指针是存放些方法区的一些信息
	实例数据：存放类的属性信息，包括父类的属性信息；
	对齐填充：对象地址必须是8字节的倍数，如果不满8字节的倍数会自动填充；

高并发时应该要考虑锁的性能损耗，能用无锁数据结构，就不用锁；能锁区块，就不要锁整个方法体；能用对象锁，就不要用类锁； 

Synchronized
	解释：再早起版本中，是属于一个重量级锁，因为底层唤醒和阻塞一个java线程是通过操作系统切换cpu来完成的；

Monitor
	解释：JVM中的同步就是基于进入和退出管程(Monitor)对象来实现的，每个对象实例都会有一个Monitor，Monitor可以和对象一起创建，销毁；Monitor是由ObjectMonitor来实现，ObjectMonitor是由C++的ObjectMonitor.hpp文件实现.就是依赖于操作系统。所以Synchronized是重量级锁
	如果一个对象被某个线程锁住，则该对象的对象头MarkWord会有monitor的起始地址，Monitor的Owner字段会存放拥有该对象锁的线程ID

锁升级： 无锁-》偏向锁-》轻量级锁-》重量级锁			JDK1.6之前Synchronized使用的是重量级锁，JDK1.6之后引入了锁升级，而不是无论什么场景都是重量级锁
	偏向锁：当同一段代码被同一个线程多次访问，由于只有一个线程那么该线程在后续访问时就会自动获得锁；第一次被拥有的时候回记录下偏向线程ID，不需要再次加锁和释放锁，下次访问的时候直接检查锁的MarkWorld是不是放的自己的ID。性能极高；直到发生竞争才会释放锁，可能会升级为轻量级锁；java15被废弃了，偏向锁开关默认是关闭的
	轻量级锁：多线程竞争，但是任意时刻最多只有一个线程竞争，既不存在锁竞争太过激烈的情况，也就没有线程阻塞；都是按顺序来获取锁，cas自旋达到一定次数（java6之前：默认是10，jvm参数可以改，java6之后：自适应决定次数，上一次自旋的时间和次数）没有成功时升级为重量级锁；
		两者区别：偏向锁是有线程竞争的时候才会释放锁，轻量级锁每次执行完都会释放锁，所以效率比偏向锁低一点
	重量级锁：线程竞争不使用自旋，不会消耗CPU；Java中Synchronized的重量级锁，是基于进入和退出Monitor对象来实现的。在编译的时候同步块的开始位置插入monitor enter指令，结束位置插入 monitor exit指令。当线程执行到monitor enter指令时，会尝试获取对象锁对应monitor所有权，如果获取到了会在Monitor的owner中存放当前线程的id，这样它就处于锁		定状态，直到退出同步块；
	锁的使用场景：偏向锁：单线程适用的场景下使用；轻量级锁：在竞争不激烈的时候使用，采用的是自旋的方式，同步方法/代码块执行时间很短的话，虽然会占用cpu资源，但是相比重量级锁还是更高效；重量级锁：适用于竞争激烈的场景，如果同步方法/代码块执行时间很长的话，轻量级锁自旋带来的性能消耗比重量级锁更严重，这时候就需要使用重量级锁；


AQS
	解释：AbstractQueuedSynchronize字面意思是抽象队列同步器，是一个抽象类，可以说是JUC的基石，主要是解决在进行等待的时候，后续线程的通知，唤醒等机制，解决把锁分配给谁的问题；
		统一规范了锁的实现，将其抽象出来，屏蔽了同步状态管理，同步队列的管理和维护，阻塞线程的排队和通知，唤醒机制等，是一切锁和同步组件实现的---公共基础部分
	原理：双向队列来完成资源获取线程的派对工作(将把每个去抢占资源的线程封装成一个node节点)+int类型表示持有锁的状态；    AQS=Node{waitstate,Thread,prev,next}+head+tail+state

公平锁，非公平锁
	区别：多一个判断，公平锁讲究先来先到，线程获取锁时，先看一下这个锁的等待队列中是否有有效的节点，如果有的话，就会进入到等待队列中；非公平锁：排第一的线程不一定能抢到锁，不管等待队列中是否有线程，只要能获取锁，他就立刻占有锁对象，不讲武德；

ReentrantLock	可重入锁   
ReentrantReadWriteLock 可重入的读写锁，解决了读读可以共享，多线程并发可以访问，读多写少的时候更好；  比传统的Synchronized 快；
	解释：写的时候不能读，读的时候不能写，但是可以多个线程同时读；      读写互斥，读读共享，读没有完成时候其他线程写锁无法获取
	缺点：  1.写锁饥饿问题：读多写少时，因为读写互斥，读线程很久获取不到锁   
		 2.锁降级：写锁可以降为读锁：读锁结束，写锁有望；写锁独占，读写全堵；			为什么会有锁降级：写锁-》读锁-》读锁释放-》写锁释放  如果 写锁-》写锁释放（**这里锁可能会被抢，数据被改**）-》读锁-》读锁释放；
StampedLock	邮戳锁-不可重入锁    是jdk1.8中新增的一个读写锁，是对jdk1.5中读写锁ReentrantReadWriteLock的锁饥饿问题优化。
	解释：最典型的乐观锁；ReentrantReadWriteLock+读的过程中也允许获取写锁介入；
