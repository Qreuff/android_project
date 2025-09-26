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
