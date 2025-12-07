package taller

import scala.annotation.tailrec
import scala.util.Random
import scala.collection.parallel.CollectionConverters._
object Riego {

  // Tipos de datos
  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  // 2.1 Generación de entradas aleatorias
  val random: Random = new Random()

  def fincaAlAzar(long: Int): Finca = {
    // ts entre [1, long*2], tr entre [1, long], prioridad entre [1,4]
    Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1,
        random.nextInt(4) + 1)
    )
  }

  def distanciaAlAzar(long: Int): Distancia = {
    // distancias entre [1, long*3], diagonal en 0
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long) { (i, j) =>
      if (i == j) 0 else v(i)(j)
    }
  }

  // 2.2 Exploración de entradas
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  // 2.3 Calculando el tiempo de inicio de riego
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length
    @tailrec
    def loop(rest: List[Int], current: Int, acc: Vector[Int]): Vector[Int] = rest match {
      case Nil => acc
      case h :: t =>
        val updated = acc.updated(h, current)
        loop(t, current + treg(f, h), updated)
    }
    loop(pi.toList, 0, Vector.fill(n)(0))
  }
  // 2.4 Calculando costos
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val t = tIR(f, pi)
    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p  = prio(f, i)
    val start = t(i)
    val finish = start + tr
    if (ts - tr >= start) {
      // dentro del margen de supervivencia
      math.max(0, ts - finish)
    } else {
      // fuera de margen: penaliza por prioridad
      p * (finish - ts)
    }
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int =
    f.indices.map(i => costoRiegoTablon(i, f, pi)).sum

  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    if (pi.length <= 1) 0
    else pi.sliding(2).map { case Vector(a, b) => d(a)(b) }.sum
  }

  // 2.5 Generando programaciones de riego
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    Vector.range(0, n).permutations.map(_.toVector).toVector
  }

  // 2.6 Programación de riego óptima (secuencial)
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val programas = generarProgramacionesRiego(f)
    programas
      .map { pi =>
        val cr = costoRiegoFinca(f, pi)
        val cm = costoMovilidad(f, pi, d)
        val total = cr + cm
        (pi, total)
      }
      .minBy(_._2)
  }

  // 3.1/3.2/3.3 Versiones paralelas

  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
    val t = tIR(f, pi)
    f.indices.par.map { i =>
      val ts = tsup(f, i)
      val tr = treg(f, i)
      val p  = prio(f, i)
      val start = t(i)
      val finish = start + tr
      if (ts - tr >= start) math.max(0, ts - finish)
      else p * (finish - ts)
    }.sum
  }
}