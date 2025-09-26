class Driver(
    fullName: String,
    age: Int,
    speed: Double,
    x: Double = 0.0,
    y: Double = 0.0,
    val licenseNumber: String
) : Human(fullName, age, speed, x, y) {