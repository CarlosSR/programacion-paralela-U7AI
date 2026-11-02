# Comandos para ejecutar
## Compilar:

```
javac -d out src/sorting/*.java
```

## Modo prueba rápida:

```
java -cp out sorting.Main single 1000000 8 10000
```

## Benchmark en M1 (hilos 1,2,4,8):

```
java -Xmx2g -cp out sorting.Main bench 1000000 10000 1,2,4,8
```

## Benchmark en i7-8700T (hilos 1,2,4,6,12):

```
java -Xmx2g -cp out sorting.Main bench 1000000 10000 1,2,4,6,12
```

## Sweep de umbral:

### i7
```
java -Xmx2g -cp out sorting.Main bench 1000000 1000 12
java -Xmx2g -cp out sorting.Main bench 1000000 5000 12
java -Xmx2g -cp out sorting.Main bench 1000000 10000 12
java -Xmx2g -cp out sorting.Main bench 1000000 50000 12
java -Xmx2g -cp out sorting.Main bench 1000000 100000 12
```

### M1
```
java -Xmx2g -cp out sorting.Main bench 1000000 1000 8
java -Xmx2g -cp out sorting.Main bench 1000000 5000 8
java -Xmx2g -cp out sorting.Main bench 1000000 10000 8
java -Xmx2g -cp out sorting.Main bench 1000000 50000 8
java -Xmx2g -cp out sorting.Main bench 1000000 100000 8
```

## Para generar las gráficas
Es necesario tener instalado matplotlib

```
pip3 install matplotlib
```

El script de automatización está pensando para las dos arquitecturas del experimento, pero se puede usar i7 en cualquier otra
```
python3 benchmark_runner.py m1

python3 benchmark_runner.py i7
```

Finalmente, se ejecuta el siguiente comando que va a leer los archivos en la carpeta results/

```
python3 plot.py
``

