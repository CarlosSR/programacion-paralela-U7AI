package sorting;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Benchmark comparativo entre Merge Sort secuencial y paralelo.
 * Realiza warm-up, varias repeticiones y valida la correctitud.
 */
public class Benchmark {

    private static final int WARMUP_RUNS = 2;
    private static final int MEASURE_RUNS = 5;

    /**
     * Genera un arreglo pseudoaleatorio reproducible con la semilla dada.
     */
    public static int[] generateArray(int size, long seed) {
        Random random = new Random(seed);
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt();
        }
        return array;
    }

    /**
     * Ejecuta una corrida del algoritmo secuencial y retorna el tiempo en ms.
     */
    public static long runSequential(int[] source) {
        int[] copy = copyArray(source);
        long start = System.nanoTime();
        MergeSortSequential.sort(copy);
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    /**
     * Ejecuta una corrida del algoritmo paralelo con el pool y umbral dados.
     */
    public static long runParallel(int[] source, int threads, int threshold) {
        int[] copy = copyArray(source);
        ForkJoinPool pool = new ForkJoinPool(threads);
        long start = System.nanoTime();
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
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }

    /**
     * Realiza warm-up para que el JIT compile las rutas críticas.
     */
    public static void warmup(int size) {
        System.out.println("[Warm-up] Ejecutando " + WARMUP_RUNS + " corridas de calentamiento...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            int[] data = generateArray(size, 12345L + i);
            runSequential(data);
            runParallel(data, 4, 10000);
        }
        System.out.println("[Warm-up] Listo.\n");
    }

    /**
     * Ejecuta la batería completa y retorna promedios de tiempos.
     */
    public static double averageSequential(int[] source, int runs) {
        long total = 0;
        for (int i = 0; i < runs; i++) {
            total += runSequential(source);
        }
        return (double) total / runs;
    }

    public static double averageParallel(int[] source, int threads, int threshold, int runs) {
        long total = 0;
        for (int i = 0; i < runs; i++) {
            total += runParallel(source, threads, threshold);
        }
        return (double) total / runs;
    }

    /**
     * Verifica que el arreglo esté ordenado ascendentemente.
     */
    public static boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida la correctitud comparando contra Arrays.sort.
     */
    public static boolean validate(int[] source, int[] result) {
        if (!isSorted(result)) {
            return false;
        }
        int[] reference = copyArray(source);
        java.util.Arrays.sort(reference);
        for (int i = 0; i < reference.length; i++) {
            if (reference[i] != result[i]) {
                return false;
            }
        }
        return true;
    }

    private static int[] copyArray(int[] source) {
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }
}