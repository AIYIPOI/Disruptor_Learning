package pipeline;

import java.util.concurrent.Executors;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class Example {
	public static int buffersize = 4;
	
	public Disruptor<IntegerEvent> disruptor;
	public RingBuffer<IntegerEvent> ringBuffer;
	
	public static void main(String[] args) {
		Example example = new Example();
		example.startDisruptor();
		example.addEvent(1);
	}
	
	/** 启动disruptor */
	public void startDisruptor() {
		// 创建disruptor对象
		disruptor = new Disruptor<>(
				IntegerEvent::new, 					// 创建事件工厂，disruptor会预先创建对象填充ringbuffer
				buffersize, 						// 队列长度
				Executors.defaultThreadFactory(), 	// 执行器，可以理解成消费线程
				ProducerType.MULTI, 				// 生产者模式
				new YieldingWaitStrategy());		// 等待策略，生产速度和消费速度不匹配时等待的方式
		
		// 绑定消费者
		disruptor.handleEventsWith(this::add1).then(this::add2).then(this::add3);
		
		// 启动disruptor，本质上是启动消费者监听线程
		ringBuffer = disruptor.start();
	}
	
	/** 处理事件，把结果加1 */
	public void add1(IntegerEvent event, long sequence, boolean endOfBath) {
		System.out.println("add1 执行前：" + event.data);
		event.data ++;
		System.out.println("add1 执行后：" + event.data);
	}
	
	/** 处理事件，把结果加1 */
	public void add2(IntegerEvent event, long sequence, boolean endOfBath) {
		System.out.println("add2 执行前：" + event.data);
		event.data ++;
		System.out.println("add2 执行后：" + event.data);
	}
	
	/** 处理事件，把结果加1 */
	public void add3(IntegerEvent event, long sequence, boolean endOfBath) {
		System.out.println("add3 执行前：" + event.data);
		event.data ++;
		System.out.println("add3 执行后：" + event.data);
	}
	
	/** 发布事件 */
	public void addEvent(int data) {
		long sequence = -1;
		try {
			sequence = ringBuffer.next();
			IntegerEvent event = ringBuffer.get(sequence);
			event.data = data;
			System.out.println("生产：" + data);
		} finally {
			ringBuffer.publish(sequence);
		}
	}
	
	class IntegerEvent {
		public int data;
	}
}
