package ru.netology;

import java.util.*;

public class RobotDelivery {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        // поток для выывода текущего лидера:
        Thread statsThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    try {
                        lock.wait();

                        synchronized (sizeToFreq) {
                            Map.Entry<Integer, Integer> maxEntry = null;
                            for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                                if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                                    maxEntry = entry;
                                }
                            }
                            if (maxEntry != null) {
                                System.out.println("Текущий лидер: " + maxEntry.getKey() + " (" + maxEntry.getValue() + " раз)");
                            }
                        }
                    } catch (InterruptedException e) {
                        // Прерывание для выхода из цикла
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        statsThread.start();

        List<Thread> threads = new ArrayList<>();


        // Поток, который создает строки
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(() -> {
                String route = generateRoute("RLRFR", 100);
                int countR = countR(route);

                //System.out.println(countR);

                // Поставлен монитор, который запрещает вносить данные
                synchronized (sizeToFreq) {
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                }

                synchronized (lock) {
                    lock.notify();
                }
            });
            threads.add(thread); // Список потоков
            thread.start(); // запуск потока по созданию строк
        }

        // Ожидаем завершения всех потоков:
        for (Thread thread : threads) {
            thread.join();
        }

        //Проверяем поток статистики
        statsThread.interrupt();
        statsThread.join();


        printStatictics();
    }

    public static void printStatictics() {
        System.out.println("\nФинальная статистика");
        synchronized (sizeToFreq) {
            if (sizeToFreq.isEmpty()) {
                System.out.println("Нет данных для статистики");
                return;
            }

            Map.Entry<Integer, Integer> maxEntry = sizeToFreq.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .orElse(null);
            System.out.println("Самое частое число повторений " + maxEntry.getKey() + " (встретилось " + maxEntry.getValue() + " раз)");
            System.out.println("Другие размеры: ");
            sizeToFreq.entrySet().stream()
                    .filter(e -> !e.getKey().equals(maxEntry.getKey()))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.println("- " + e.getKey() + " (" + e.getValue() + " раз)"));
        }
    }

    public static int countR(String route) {
        int count = 0;
        for (char c : route.toCharArray()) {
            if (c == 'R') {
                count++;
            }
        }
        return count;
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}