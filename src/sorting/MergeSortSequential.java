
package sorting;

/**
 * Implementación secuencial de Merge Sort con buffer auxiliar compartido.
 * Sirve como referencia base para comparar contra la versión paralela.
 */
public class MergeSortSequential {

    /**
     * Ordena el arreglo completo de forma secuencial.
     */
    public static void sort(int[] array) {
        if (array == null || array.length < 2) {
            return;
        }
        // Buffer auxiliar del mismo tamaño para evitar asignaciones por nivel
        int[] temp = new int[array.length];
        mergeSort(array, temp, 0, array.length);
    }

    /**
     * Merge Sort recursivo sobre el rango [lo, hi).
     */
    private static void mergeSort(int[] array, int[] temp, int lo, int hi) {
        // Caso base: un solo elemento ya está ordenado
        if (hi - lo < 2) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(array, temp, lo, mid);
        mergeSort(array, temp, mid, hi);
        merge(array, temp, lo, mid, hi);
    }

    /**
     * Fusiona dos mitades ordenadas [lo, mid) y [mid, hi) usando temp como
     * buffer intermedio. Al final copia el resultado de vuelta al arreglo.
     * Los rangos de cada merge son disjuntos a un mismo nivel, por lo que
     * no hay condición de carrera sobre temp.
     */
    static void merge(int[] array, int[] temp, int lo, int mid, int hi) {
        int i = lo;
        int j = mid;
        int k = lo;

        // Fusión clásica de dos listas ordenadas
        while (i < mid && j < hi) {
            if (array[i] <= array[j]) {
                temp[k] = array[i];
                i++;
            } else {
                temp[k] = array[j];
                j++;
            }
            k++;
        }

        // Copiar sobrantes de la mitad izquierda
        while (i < mid) {
            temp[k] = array[i];
            i++;
            k++;
        }

        // Copiar sobrantes de la mitad derecha
        while (j < hi) {
            temp[k] = array[j];
            j++;
            k++;
        }

        // Copiar el resultado fusionado de vuelta al arreglo original
        for (int p = lo; p < hi; p++) {
            array[p] = temp[p];
        }
    }

    /**
     * Ordena el rango [lo, hi) de forma secuencial usando el buffer temp.
     * Se usa como caso base de la versión paralela.
     */
    public static void sortRange(int[] array, int[] temp, int lo, int hi) {
        if (hi - lo < 2) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sortRange(array, temp, lo, mid);
        sortRange(array, temp, mid, hi);
        merge(array, temp, lo, mid, hi);
    }
}