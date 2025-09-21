import kotlin.math.*
import kotlin.random.Random

open class Human(
    var fullName: String,
    var age: Int,
    var speed: Double,
    var x: Double = 0.0,
    var y: Double = 0.0
) : Runnable {

    var simulationTime: Int = 10   // время симуляции по умолчанию (секунд)
    var dt: Double = 1.0           // шаг времени (секунда)

    fun setPosition(nx: Double, ny: Double) {
        x = nx; y = ny
    }

    open fun move(dt: Double = 1.0): Pair<Double, Double> {
        if (dt <= 0.0) return x to y
        val theta = Random.nextDouble(0.0, 2 * Math.PI)
        val distance = speed * dt
        val dx = distance * cos(theta)
        val dy = distance * sin(theta)
        x += dx
        y += dy
        return x to y
    }

    override fun run() {
        var t = 0.0
        while (t < simulationTime) {
            t += dt
            move(dt)
            println("$fullName: pos=(${String.format("%.3f", x)}, ${String.format("%.3f", y)}) at t=${t.toInt()}s")
            Thread.sleep((dt * 1000).toLong())
        }
    }

    override fun toString(): String {
        return "Human(name='$fullName', age=$age, speed=${"%.2f".format(speed)}, x=${"%.3f".format(x)}, y=${"%.3f".format(y)})"
    }
}

class Driver(
    fullName: String,
    age: Int,
    speed: Double,
    x: Double = 0.0,
    y: Double = 0.0,
    val licenseNumber: String
) : Human(fullName, age, speed, x, y) {

    override fun move(dt: Double): Pair<Double, Double> {
        if (dt <= 0.0) return x to y
        val distance = speed * dt
        x += distance // прямолинейное движение вдоль оси X
        return x to y
    }

    override fun run() {
        var t = 0.0
        while (t < simulationTime) {
            t += dt
            move(dt)
            println("$fullName (Driver): pos=(${String.format("%.3f", x)}, ${String.format("%.3f", y)}) at t=${t.toInt()}s")
            Thread.sleep((dt * 1000).toLong())
        }
    }
}

fun main() {
    val numHumans = 3
    val simulationTime = 10
    val dt = 1.0

    val humans = Array(numHumans) { i ->
        val speed = Random.nextDouble(0.5, 2.5)
        Human(
            fullName = "Human ${i + 1}",
            age = 18 + i,
            speed = speed
        )
    }.toList()

    val driver = Driver("Driver 1", 35, 2.0, licenseNumber = "AB1234")

    // задаём одинаковые параметры симуляции
    (humans + driver).forEach {
        it.simulationTime = simulationTime
        it.dt = dt
    }

    val threads = humans.map { Thread(it) } + Thread(driver)

    println("Запуск симуляции на $simulationTime секунд...\n")
    threads.forEach { it.start() }
    threads.forEach { it.join() }
    println("\nИтоговые состояния:")
    (humans + driver).forEach { println(it) }
}
