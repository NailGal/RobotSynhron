package ru.netology;

import java.util.*;

public class RobotDelivery {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object();
    private static volatile boolean isRunning = true;

    public static void main(String[] args) throws InterruptedException {
        // Поток для вывода текущего лидера
        Thread statsThread = new Thread(() -> {
            while (isRunning) {
                synchronized (lock) {
                    try {
                        lock.wait();
                        if (!isRunning) break;

                        synchronized (sizeToFreq) {
                            Map.Entry<Integer, Integer> maxEntry = null;
                            for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                                if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                                    maxEntry = entry;
                                }
                            }
                            if (maxEntry != null) {
                                System.out.println("Текущий лидер: " + maxEntry.getKey()
                                        + " (" + maxEntry.getValue() + " раз)");
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        statsThread.start();

        List<Thread> threads = new ArrayList<>();

        // Создаем рабочие потоки
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(() -> {
                String route = generateRoute("RLRFR", 100);
                int countR = countR(route);

                synchronized (sizeToFreq) {
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                }

                synchronized (lock) {
                    lock.notify();
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Ожидаем завершения всех рабочих потоков
        for (Thread thread : threads) {
            thread.join();
        }

        // Останавливаем поток статистики
        synchronized (lock) {
            isRunning = false;
            lock.notify();
        }
        statsThread.join();

        printStatistics();
    }

    public static void printStatistics() {
        System.out.println("\nФинальная статистика:");
        synchronized (sizeToFreq) {
            if (sizeToFreq.isEmpty()) {
                System.out.println("Нет данных для статистики");
                return;
            }

            Map.Entry<Integer, Integer> maxEntry = Collections.max(
                    sizeToFreq.entrySet(),
                    Map.Entry.comparingByValue()
            );

            System.out.println("Самое частое число повторений " + maxEntry.getKey() +
                    " (встретилось " + maxEntry.getValue() + " раз)");
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