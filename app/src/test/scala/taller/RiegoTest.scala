package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoTest extends AnyFunSuite {
  import Riego._


  private val F1: Finca = Vector(
    (10, 3, 4),
    (5, 3, 3),
    (2, 2, 1),
    (8, 1, 1),
    (6, 4, 2)
  )
  private val DF1: Distancia = Vector(
    Vector(0, 2, 2, 4, 4),
    Vector(2, 0, 4, 2, 6),
    Vector(2, 4, 0, 2, 2),
    Vector(4, 2, 2, 0, 4),
    Vector(4, 6, 2, 4, 0)
  )

  private val F2: Finca = Vector(
    (9, 3, 4),
    (5, 3, 3),
    (2, 2, 1),
    (8, 1, 1),
    (6, 4, 2)
  )
  private val DF2: Distancia = Vector(
    Vector(0, 2, 2, 6, 8),
    Vector(2, 0, 2, 2, 2),
    Vector(2, 2, 0, 2, 2),
    Vector(6, 2, 2, 0, 2),
    Vector(8, 2, 2, 2, 0)
  )

  test("tIR calcula los tiempos de inicio (Ejemplo 1, Prog 1)") {
    val pi = Vector(0, 1, 4, 2, 3)
    val tiempos = tIR(F1, pi)
    assert(tiempos === Vector(0, 3, 10, 12, 6))
  }

  test("Costo de riego Programación 1 de F1 (CR=33)") {
    val pi = Vector(0, 1, 4, 2, 3)
    assert(costoRiegoFinca(F1, pi) === 33)
  }

  test("Costo de movilidad Programación 1 de F1 (CM=12)") {
    val pi = Vector(0, 1, 4, 2, 3)
    assert(costoMovilidad(F1, pi, DF1) === 12)
  }

  test("Costo total Programación 1 de F1 (45)") {
    val pi = Vector(0, 1, 4, 2, 3)
    val total = costoRiegoFinca(F1, pi) + costoMovilidad(F1, pi, DF1)
    assert(total === 45)
  }

  test("Programación 2 de F1 mejora a Programación 1 (38 < 45)") {
    val pi1 = Vector(0, 1, 4, 2, 3)
    val pi2 = Vector(2, 1, 4, 3, 0)
    val total1 = costoRiegoFinca(F1, pi1) + costoMovilidad(F1, pi1, DF1)
    val total2 = costoRiegoFinca(F1, pi2) + costoMovilidad(F1, pi2, DF1)
    assert(total1 === 45)
    assert(total2 === 38)
    assert(total2 < total1)
  }

  test("Ejemplo 2: Programación 1 vs 2 (36 < 41)") {
    val pi1 = Vector(2, 1, 4, 3, 0)
    val pi2 = Vector(2, 1, 4, 0, 3)
    val total1 = costoRiegoFinca(F2, pi1) + costoMovilidad(F2, pi1, DF2)
    val total2 = costoRiegoFinca(F2, pi2) + costoMovilidad(F2, pi2, DF2)
    assert(total1 === 36)
    assert(total2 === 41)
    assert(total1 < total2)
  }
}