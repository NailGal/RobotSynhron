package ru.netology;

import java.util.*;

public class RobotDelivery {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(() -> {
                String route = generateRoute("RLRFR", 100);
                int countR = countR(route);
                //System.out.println(countR);

                synchronized (sizeToFreq) {
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Ожидаем завершения всех потоков:
        for (Thread thread : threads) {
            thread.join();
        }
        printStatictics();
    }

    public static void printStatictics() {
        if (sizeToFreq.isEmpty()) {
            System.out.println("Нет данных для статистики");
            return;
        }

        Map.Entry<Integer, Integer> maxEntry = sizeToFreq.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElse(null);
        System.out.println("Самое частое число повторений " + maxEntry.getKey() + " (встретилось " + maxEntry.getValue() + " раз)");
        System.out.println("Другие размеры: ");
        sizeToFreq.entrySet().stream().filter(e -> !e.getKey().equals(maxEntry.getKey())).sorted(Map.Entry.comparingByKey()).forEach(e -> System.out.println("- " + e.getKey() + " (" + e.getValue() + " раз)"));
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