class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Поток " + Thread.currentThread().getName() + " выполняется.");
    }
}


public class ThreadExample {
    public static void main(String[] args) throws InterruptedException {
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();
        System.out.println(thread1.getState());
        thread1.start(); // Запуск первого потока
        thread2.start(); // Запуск второго потока
        while(thread1.getState() != Thread.State.TERMINATED){
            System.out.println(thread1.getState());
            try {
                Thread.sleep(1000);

            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println(thread1.getState());

    }
}