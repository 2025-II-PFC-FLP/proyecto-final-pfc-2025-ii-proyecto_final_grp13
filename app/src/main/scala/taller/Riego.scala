package taller

import scala.annotation.tailrec
import scala.util.Random
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

}