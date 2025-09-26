interface Movable{
    interface move_Human{
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
}
    interface move_Driver{
        override fun move(dt: Double): Pair<Double, Double> {
        if (dt <= 0.0) return x to y
        val distance = speed * dt
        x += distance // прямолинейное движение вдоль оси X
        return x to y
    }
}   
}    
