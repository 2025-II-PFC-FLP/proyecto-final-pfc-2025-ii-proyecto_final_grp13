# Informe de Paralelización – Programación de Riego

## Introducción

Se paralelizaron las partes de cálculo intensivo de la programación de riego: el costo por tablón y el costo de movilidad sobre una programación dada, así como la evaluación del costo total sobre un conjunto de programaciones muestreadas. Se usaron colecciones paralelas de Scala (`.par`) para lograr paralelización de datos, sin estado compartido.

---

## Estrategia de Paralelización

- **Tipo**: Paralelización de datos (data parallelism).
- **Herramienta**: `scala-parallel-collections` + `CollectionConverters._`.
- **Funciones paralelizadas**:
    - `costoRiegoFincaPar`: `f.indices.par.map(...)`
    - `costoMovilidadPar`: `pi.sliding(2).toVector.par.map(...)`
    - Evaluación de costos sobre un conjunto de programaciones muestreadas: `programas.par.map(...)`.

### Justificación

- Cada tablón y cada arco de movilidad se evalúan de forma independiente.
- Las funciones son puras; no hay estado compartido ni efectos laterales.
- La sobrecarga de paralelizar se amortiza con suficiente trabajo por elemento.

---

## Ley de Amdahl (marco teórico)

Sea $P$ la fracción paralelizable y $N$ el número de núcleos:

$$
\text{Speedup} = \frac{1}{(1-P) + \frac{P}{N}}
$$

- Overhead de hilos y conversión `.par`/`.seq` afecta entradas pequeñas.
- Para cargas reducidas, la fracción secuencial domina y puede producir desaceleración.

---

## Benchmark (muestra aleatoria de K permutaciones)

Medición con el runner `BenchmarkRiego` (K=200 permutaciones aleatorias, 3 corridas promedio, n pequeño para evitar factorial):

| Tamaño | Perms | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
|-------:|------:|----------------:|--------------:|----------------:|
| 8      | 200   | 12.71           | 44.25         | -71.27          |
| 10     | 200   | 3.04            | 12.08         | -74.86          |
| 12     | 200   | 2.78            | 5.78          | -51.83          |

**Interpretación**: Para n y K pequeños, el overhead de paralelización supera el trabajo útil → desaceleración. Este resultado es coherente con Amdahl: la fracción secuencial y el costo de orquestación dominan para instancias pequeñas.

---

## Cuándo paralelizar

- ✅ Útil cuando el trabajo por tarea es alto: más permutaciones muestreadas (K grande) o cálculos más costosos por tablón.
- ❌ No recomendable para instancias pequeñas: pocas permutaciones o n reducido (como en la tabla).

En este contexto, si se quisiera observar speedup, se debería aumentar K (más permutaciones muestreadas) y/o n, cuidando de no explotar factorial ni agotar memoria.

---

## Equivalencia semántica

- Las versiones paralelas producen el mismo costo que las secuenciales para un mismo conjunto de programaciones.
- El criterio de mínimo no cambia; solo cambia el orden de evaluación. Con datos puros, el resultado es determinista.

---

## Conclusiones

- La paralelización aplicada es correcta y libre de efectos colaterales.
- Para cargas pequeñas, el overhead domina y no hay speedup (incluso hay desaceleración).
- Para cargas mayores (no medidas aquí por límite factorial), se espera que la fracción paralelizable aumente el rendimiento hasta el límite impuesto por Amdahl y el hardware disponible.
- El muestreo de permutaciones es una técnica práctica para evitar explosión combinatoria y OOM; mantiene comparable el costo seq/par.