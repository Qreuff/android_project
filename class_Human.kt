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
