package sorting;

/**
 * Punto de entrada del proyecto.
 * Modos:
 *   single  -> ordena un arreglo y valida
 *   bench   -> corre benchmark completo con warm-up y promedios
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String mode = args[0];

        if (mode.equals("single")) {
            runSingle(args);
        } else if (mode.equals("bench")) {
            runBench(args);
        } else {
            System.out.println("Modo desconocido: " + mode);
            printUsage();
        }
    }

    private static void runSingle(String[] args) {
        if (args.length < 4) {
            System.out.println("Uso: single <tamaño> <hilos> <umbral>");
            return;
        }
        int size = Integer.parseInt(args[1]);
        int threads = Integer.parseInt(args[2]);
        int threshold = Integer.parseInt(args[3]);

        int[] data = Benchmark.generateArray(size, 42L);
        System.out.println("Tamaño del arreglo: " + size);
        System.out.println("Hilos: " + threads + ", umbral: " + threshold);

        long tSeq = Benchmark.runSequential(data);
        System.out.println("Tiempo secuencial: " + tSeq + " ms");

        long tPar = Benchmark.runParallel(data, threads, threshold);
        System.out.println("Tiempo paralelo:   " + tPar + " ms");
    }

    private static void runBench(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: bench <tamaño> <umbral> <hilos1,hilos2,...>");
            System.out.println("Ejemplo: bench 1000000 10000 1,2,4,8");
            return;
        }
        int size = Integer.parseInt(args[1]);
        int threshold = Integer.parseInt(args[2]);
        String[] parts = args[3].split(",");
        int[] threadList = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            threadList[i] = Integer.parseInt(parts[i].trim());
        }

        System.out.println("=== BENCHMARK MERGE SORT PARALELO ===");
        System.out.println("Tamaño: " + size + " | Umbral: " + threshold);
        System.out.println();

        // Warm-up antes de medir
        Benchmark.warmup(Math.min(size, 200_000));

        int[] data = Benchmark.generateArray(size, 42L);

        double avgSeq = Benchmark.averageSequential(data, 5);
        System.out.println("Secuencial: " + String.format("%.2f", avgSeq) + " ms (promedio de 5)");
        System.out.println();
        System.out.println("Hilos\tTiempo(ms)\tSpeedup\tValidez");
        System.out.println("-----\t----------\t-------\t-------");

        for (int i = 0; i < threadList.length; i++) {
            int threads = threadList[i];
            double avgPar = Benchmark.averageParallel(data, threads, threshold, 5);

            // Validar correctitud con una corrida adicional
            int[] testData = Benchmark.generateArray(size, 99L);
            int[] testResult = copyAndSort(testData, threads, threshold);
            boolean ok = Benchmark.validate(testData, testResult);

            double speedup = avgSeq / avgPar;
            System.out.println(
                threads + "\t" +
                String.format("%.2f", avgPar) + "\t\t" +
                String.format("%.2f", speedup) + "x\t" +
                (ok ? "OK" : "FALLA")
            );
        }
    }

    private static int[] copyAndSort(int[] source, int threads, int threshold) {
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool(threads);
        try {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    MergeSortParallel.sort(copy, threshold);
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
        return copy;
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java sorting.Main single <tamaño> <hilos> <umbral>");
        System.out.println("  java sorting.Main bench  <tamaño> <umbral> <hilos1,hilos2,...>");
    }
}