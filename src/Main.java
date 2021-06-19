import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

class Main { 
	static ArrayList<Thread> arrThread = new ArrayList<>();
	
	public static final int CARS_COUNT = 10;
	//public static Semaphore semaphoreMain  = new Semaphore(CARS_COUNT*2);
	 public static CountDownLatch countDownLatch = new CountDownLatch(CARS_COUNT);
	 public static Lock lock =new ReentrantLock();
	 public static AtomicInteger win = new AtomicInteger(0);
	 public static CyclicBarrier cyclicBarrier = new CyclicBarrier(CARS_COUNT);
	public static void main(String[] args) {
		System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
		Race race = new Race(new Road(60), new Tunnel(), new Road(40));
		Car[] cars = new Car[CARS_COUNT];
		for (int i = 0; i < cars.length; i++) {
			cars[i] = new Car(race, 20 + (int) (Math.random() * 10), countDownLatch, lock,  win);
		}
	
		for (int i = 0; i < cars.length; i++) {
			
			arrThread.add(new Thread(cars[i]) );
			arrThread.get(i).start();
			//new Thread(cars[i]).start();
		}   
		
		
		//semaphoreMain.release();
		try {
		countDownLatch.await();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
		
		
		for (Thread t:arrThread) {
			try {
			t.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	
		
		
		System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
	}
}
 class Car implements Runnable {
	 
	private static int CARS_COUNT; 
	private Race race;
	private int speed;
	private AtomicInteger win;
	private String name;
	private Lock lock;
	private CountDownLatch countDownLatch;
	public String getName() {
		return name;
	}
	public int getSpeed() {
		return speed;
	}
	public Car(Race race, int speed, CountDownLatch countDownLatch, Lock lock, AtomicInteger win) {
		this.countDownLatch=countDownLatch;
		this.lock= lock;
		this.race = race;
		this.win = win;
		this.speed = speed;
		CARS_COUNT++;
		this.name = "Участник #" + CARS_COUNT;
	}
	@Override
	public void run() {
		try {
			System.out.println(this.name + " готовится");
			Thread.sleep(500 + (int)(Math.random() * 800)); 
			countDownLatch.countDown();
			System.out.println(this.name + " готов"); 
			countDownLatch.await();
			//Main.semaphoreMain.acquire();
		} catch (Exception e) {
			e.printStackTrace();
		}
	      
		/*try {
			Main.cyclicBarrier.await();
		} catch (Exception e) {
			e.printStackTrace();
		} */
		for (int i = 0; i < race.getStages().size(); i++) {
			race.getStages().get(i).go(this);
		}
		int winn=0;
	   try {
		 winn= win.incrementAndGet();
		   lock.lock();
	   } finally { 
		   lock.unlock();
		   if (winn ==1 ) {
			   System.out.println( this.name +" ПОБЕДИТЕЛЬ");  
		   } else {
		   System.out.println( this.name +" занял "+ win +" место.");
		   }
	   }
		
	}
}
 abstract class Stage {
	protected int length;
	protected String description;
	public String getDescription() {
		return description;
	}
	public abstract void go(Car c);
}
 class Road extends Stage {
	public Road(int length) {
		this.length = length;
		this.description = "Дорога " + length + " метров";
	}
	@Override
	public void go(Car c) {
		try {
			System.out.println(c.getName() + " начал этап: " + description);
			Thread.sleep(length / c.getSpeed() * 1000);
			System.out.println(c.getName() + " закончил этап: " + description);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
 class Tunnel extends Stage {

 private Semaphore semaphore = new Semaphore(Main.CARS_COUNT/2);
	public Tunnel() {
		this.length = 80;
		this.description = "Тоннель " + length + " метров";
	}
	@Override
	public void go(Car c) {
		try {
			try {
				System.out.println(c.getName() + " " + c.getSpeed() + " готовится к этапу(ждет): " + description);
				semaphore.acquire();
				System.out.println(c.getName() + " " + c.getSpeed() + " начал этап: " + description);
				Thread.sleep(length / c.getSpeed() * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				System.out.println(c.getName() + " " + c.getSpeed()  + " закончил этап: " + description);
				semaphore.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
 class Race {
	private ArrayList<Stage> stages;
	public ArrayList<Stage> getStages() { return stages; }
	public Race(Stage... stages) {
		this.stages = new ArrayList<>(Arrays.asList(stages));
	}
}
