package taller

import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner
import org.junit.runner.RunWith
import scala.util.Random
import scala.collection.parallel.CollectionConverters._
import Riego._

@RunWith(classOf[JUnitRunner])
class BenchmarkRiego extends AnyFunSuite {

  private def genFinca(n: Int, seed: Int): (Finca, Distancia) = {
    val rnd = new Random(seed)
    val f = Vector.fill(n)(
      (rnd.nextInt(n * 2) + 1, rnd.nextInt(n) + 1, rnd.nextInt(4) + 1)
    )
    val dRaw = Vector.fill(n, n)(rnd.nextInt(n * 3) + 1)
    val d = Vector.tabulate(n, n)((i, j) => if (i == j) 0 else dRaw(i)(j))
    (f, d)
  }

  // Genera K permutaciones aleatorias reproducibles
  private def genProgramas(n: Int, k: Int, seed: Int): Vector[ProgRiego] = {
    val rnd = new Random(seed)
    Vector.fill(k) {
      rnd.shuffle((0 until n).toVector)
    }
  }

  private def time[A](runs: Int)(block: => A): Double = {
    val times = Vector.fill(runs) {
      val t0 = System.nanoTime()
      val _ = block
      (System.nanoTime() - t0).toDouble / 1e6
    }
    times.sum / runs
  }

  test("benchmark seq vs par (aprox, sin factorial)") {
    val sizes = List(8, 10, 12)
    val runs  = 3
    val kPerm = 200
    val seedBase = 1234

    println("Tamaño | Perms | Secuencial (ms) | Paralelo (ms) | Aceleración (%)")
    sizes.foreach { n =>
      val (f, d) = genFinca(n, seedBase + n)
      val programas = genProgramas(n, kPerm, seedBase + n * 10)
      val ts = time(runs) {
        programas.map { pi =>
          costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
        }.min
      }
      val tp = time(runs) {
        programas.par.map { pi =>
          costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)
        }.min
      }
      val accel = if (tp > 0) (ts / tp - 1.0) * 100.0 else 0.0
      println(f"$n%6d | $kPerm%5d | $ts%.2f | $tp%.2f | $accel%.2f")
    }
  }
}
