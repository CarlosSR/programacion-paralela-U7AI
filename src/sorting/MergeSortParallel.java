package sorting;

import java.util.concurrent.RecursiveAction;

/**
 * Merge Sort paralelo usando Fork/Join.
 * Extiende RecursiveAction porque ordena en el mismo arreglo (in-place)
 * usando un buffer auxiliar compartido.
 */
public class MergeSortParallel extends RecursiveAction {

    private static final long serialVersionUID = 1L;

    private final int[] array;
    private final int[] temp;
    private final int lo;
    private final int hi;
    private final int threshold;

    public MergeSortParallel(int[] array, int[] temp, int lo, int hi, int threshold) {
        this.array = array;
        this.temp = temp;
        this.lo = lo;
        this.hi = hi;
        this.threshold = threshold;
    }

    /**
     * Punto de entrada estático para lanzar la ordenación paralela.
     */
    public static void sort(int[] array, int threshold) {
        if (array == null || array.length < 2) {
            return;
        }
        int[] temp = new int[array.length];
        MergeSortParallel task = new MergeSortParallel(array, temp, 0, array.length, threshold);
        // invoke() ejecuta la tarea en el pool común y espera el resultado
        task.invoke();
    }

    @Override
    protected void compute() {
        int size = hi - lo;

        // Caso base: si el subproblema es pequeño, resolvemos secuencialmente
        // para evitar el overhead de crear tareas Fork/Join
        if (size <= threshold) {
            sequentialSort();
            return;
        }

        int mid = lo + (size / 2);

        // Dividir el problema en dos subtareas sobre mitades disjuntas
        MergeSortParallel leftTask = new MergeSortParallel(array, temp, lo, mid, threshold);
        MergeSortParallel rightTask = new MergeSortParallel(array, temp, mid, hi, threshold);

        // fork() ejecuta la tarea izquierda en otro hilo (o en cola)
        leftTask.fork();
        // La tarea derecha se ejecuta en el hilo actual
        rightTask.compute();
        // join() espera a que la izquierda termine
        leftTask.join();

        // Una vez ordenadas ambas mitades, fusionarlas
        MergeSortSequential.merge(array, temp, lo, mid, hi);
    }

    /**
     * Ordenación secuencial del rango actual. Reutiliza el merge del
     * secuencial pero con sus propios índices.
     */
    private void sequentialSort() {
        MergeSortSequential.sortRange(array, temp, lo, hi);
    }
}