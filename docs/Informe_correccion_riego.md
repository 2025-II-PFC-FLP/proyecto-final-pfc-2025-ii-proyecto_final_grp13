# Informe de Corrección – Programación de Riego

## Teorema Principal

Las funciones implementadas en `Riego.scala` calculan correctamente:
1. Los tiempos de inicio de riego según una permutación `pi`.
2. El costo de riego por tablón y total de la finca.
3. El costo de movilidad entre tablones según la programación.
4. La búsqueda del programa óptimo (mínimo costo) entre todas las permutaciones (versión secuencial) y su contraparte paralela con el mismo criterio.
   Todas las funciones terminan en tiempo finito para entradas finitas y preservan los invariantes de la especificación.

---

## Definiciones y Semántica

Sea una finca $f$ con $n$ tablones. Para cada tablón $i$:
- $\text{ts}(i)$: tiempo de supervivencia.
- $\text{tr}(i)$: tiempo de riego.
- $\text{p}(i)$: prioridad.

Para una permutación $\pi$ de $\{0,\dots,n-1\}$:
- Tiempo de inicio:
  $$
  tIR(f,\pi)[\pi(k)] = \sum_{j=0}^{k-1} tr(\pi(j))
  $$

- Costo de riego por tablón $i$:
  $$
  \text{start} = tIR(f,\pi)[i], \quad \text{finish} = \text{start} + tr(i)
  $$
  $$
  \text{costoRiego}(i) =
  \begin{cases}
  \max(0, ts(i) - \text{finish}), & \text{si } ts(i) - tr(i) \ge \text{start} \\
  p(i) \cdot (\text{finish} - ts(i)), & \text{otro caso}
  \end{cases}
  $$

- Costo de movilidad:
  $$
  \text{costoMov}(\pi,d) = \sum_{k=0}^{n-2} d(\pi(k), \pi(k+1))
  $$

- Costo total:
  $$
  \text{costoTotal}(f,\pi,d) = \sum_i \text{costoRiego}(i) + \text{costoMov}(\pi,d)
  $$

---

## Casos Base y Cobertura (pattern matching y recursión)

1) `tIR`
- Caso base: lista vacía → acumulador devuelto.
- Caso recursivo: toma cabeza `h`, actualiza `acc(h)=current`, avanza con `current + tr(h)`.
- Terminación: cada llamada consume un elemento de la lista `pi`.

2) `costoRiegoTablon`
- Depende de `tIR`, sin recursión propia; usa condicional estructural.

3) `costoRiegoFinca`
- Mapea todos los índices y suma; termina porque el tamaño es finito.

4) `costoMovilidad`
- Si longitud ≤ 1 → 0; si no, recorre pares adyacentes con `sliding(2)`.

5) `generarProgramacionesRiego`
- Usa `permutations` de una colección finita; termina pero de orden factorial.

6) `ProgramacionRiegoOptimo`
- Aplica `minBy` sobre todas las permutaciones (finita). Correctitud: examina exhaustivamente el espacio de búsqueda y toma el mínimo costo.

7) Versiones paralelas
- Misma semántica que las secuenciales; cambian `map`/`sum`/`sliding` a paralelos (`.par`).

---

## Invariantes

- Invariante de índices: toda programación es una permutación de 0..n-1 (sin repetición, sin omisión).
- Invariante de tiempos: `tIR` asigna a cada tablón el acumulado de riegos previos en la secuencia.
- Invariante de corrección del costo: para cada tablón, se aplica exactamente una de las dos ramas (dentro de margen o fuera, penalizada por prioridad).
- Pureza funcional: no hay estado mutable ni efectos colaterales; las funciones dependen solo de sus argumentos.

---

## Terminación

- `tIR`: recursión de cola, consume la lista de `pi` (longitud finita).
- `costo*`: usan map/sum sobre colecciones finitas.
- `ProgramacionRiegoOptimo`: opera sobre un número finito de permutaciones (factorial). Para n moderado termina; para benchmarking se usa muestreo de K permutaciones para evitar explosión combinatoria.
- Versiones paralelas: mismas colecciones finitas; terminan bajo el mismo dominio.

---

## Demostración de Corrección (esbozo)

1) **tIR** por inducción sobre la lista `pi`:
    - Base: lista vacía → vector inicial de ceros, correcto.
    - Paso inductivo: al fijar cabeza `h`, se asigna el acumulado actual a `h` y se incrementa el acumulado por `tr(h)`. Por hipótesis inductiva, el resto queda bien asignado.

2) **costoRiegoTablon**:
    - Usa el `start` correcto de `tIR`.
    - Distingue si aún está en margen (`ts - tr ≥ start`) o si se atrasó; aplica la fórmula requerida.

3) **costoMovilidad**:
    - Recorre cada par consecutivo de la permutación; coincide con la definición de suma de distancias entre visitas.

4) **ProgramacionRiegoOptimo**:
    - Evalúa todas las permutaciones y toma el mínimo costo → coincide con la definición de óptimo global en un dominio finito.

5) **Paralelo vs Secuencial**:
    - Las versiones paralelas preservan la misma función de costo. La operación de mínimo sobre el mismo conjunto garantiza igualdad de resultado (asumiendo determinismo de `.par` sobre datos puros).

---

## Casos de prueba documentados (≥5 por función)

### tIR
1. 1 tablón: tr=(2), pi=[0] → tIR=[0].
2. 2 tablones: tr=(2,3), pi=[1,0] → tIR(1)=0, tIR(0)=3.
3. 3 tablones: tr=(1,1,1), pi=[0,1,2] → tIR=[0,1,2].
4. 3 tablones: tr=(1,4,2), pi=[2,0,1] → tIR(2)=0, tIR(0)=2, tIR(1)=3.
5. 4 tablones: tr=(2,2,2,2), pi=[3,2,1,0] → tIR(3)=0, tIR(2)=2, tIR(1)=4, tIR(0)=6.

### costoRiegoTablon
(Usa tIR previo)
1. ts=10,tr=2,p=1,start=0 → finish=2 ≤ ts → costo=10-2=8.
2. ts=5,tr=2,p=3,start=3 → finish=5=ts → costo=0.
3. ts=6,tr=4,p=2,start=5 → finish=9>ts → costo=2*(9-6)=6.
4. ts=4,tr=1,p=5,start=0 → finish=1 ≤ ts → costo=3.
5. ts=7,tr=3,p=4,start=2 → finish=5 ≤ ts → costo=2.

### costoRiegoFinca
1. Un tablón → suma de ese costo.
2. Dos tablones con costos 8 y 0 → total 8.
3. Tres tablones con costos 3,6,2 → total 11.
4. Cuatro tablones todos en margen → suma de sobrantes.
5. Mixto: dos en margen, dos atrasados → suma ponderada por prioridad.

### costoMovilidad
1. pi longitud 1 → 0.
2. pi=[0,1], d(0,1)=5 → costo=5.
3. pi=[1,2,0], d(1,2)=3, d(2,0)=4 → costo=7.
4. pi=[2,1,3,0], suma de arcos consecutivos.
5. pi=[0,3,1,2], suma d(0,3)+d(3,1)+d(1,2).

### ProgramacionRiegoOptimo / ProgramacionRiegoOptimoPar
(Comparan permutaciones y eligen mínimo)
1. n=1 → única permutación → óptimo.
2. n=2 → evalúa [0,1] y [1,0]; elige menor costo.
3. n=3 pequeña (distancias simétricas, tr uniformes) → óptimo coincide en seq y par.
4. Caso rúbrica F1: Prog 2 mejora a total 38 vs 45.
5. Caso rúbrica F2: Prog 1 mejor (36 vs 41).

(Para versiones paralelas, el resultado debe coincidir con la secuencial en el mismo dominio de permutaciones.)

---

## Conclusión

Las funciones cumplen la especificación:
- Calculan tiempos de inicio, costos y mínimo global correctamente.
- Terminan para entradas finitas (exploración exhaustiva o muestreo controlado).
- Mantienen invariantes de permutación y pureza funcional.
- Las versiones paralelas son semánticamente equivalentes a las secuenciales.