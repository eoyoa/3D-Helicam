import vision.gears.webglmath.Mat4
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import kotlin.math.tan

class HelicamCamera(var trackedObject: GameObject) : UniformProvider("camera") {
    // uniform vars
    val viewProjMatrix by Mat4()
    val position by Vec3()

    val rayDirMatrix by Mat4()

    // helicam specific vars
    var distanceFromObject = 50f

    var ahead = Vec3()
    var right = Vec3()
    var up = Vec3()

    var viewMatrix = Mat4()

    var fov = 1.0f
    var aspect = 1.0f
    var nearPlane = 0.1f
    var farPlane = 1000.0f

    companion object {
        val worldUp = Vec3(0.0f, 1.0f, 0.0f)
        val worldForward = Vec3(1.0f, 0.0f, 0.0f)
    }

    fun update() {
        console.log("tracked pos: ${trackedObject.position.x}, ${trackedObject.position.y}, ${trackedObject.position.z}")
        position.set(trackedObject.position + (worldUp + worldForward).normalize() * distanceFromObject)
        console.log("helicam pos: ${position.x}, ${position.y}, ${position.z}")

        ahead = (trackedObject.position - position).normalize()
        console.log("ahead: ${ahead.x}, ${ahead.y}, ${ahead.z}")
        right = ahead.cross(worldUp).normalize()
        console.log("right: ${right.x}, ${right.y}, ${right.z}")
        up = right.cross(ahead)
        console.log("up: ${up.x}, ${up.y}, ${up.z}")

        viewMatrix = Mat4(
            right.x ,   right.y ,   right.z ,   0f,
            up.x    ,   up.y    ,   up.z    ,   0f,
            -ahead.x,   -ahead.y,   -ahead.z,   0f,
            0f      ,   0f      ,   0f      ,   1f
        )
            .translate(position)
            .invert()
        viewProjMatrix.set(viewMatrix)

        val yScale = 1.0f / tan(fov * 0.5f)
        val xScale = yScale / aspect
        val f = farPlane
        val n = nearPlane
        viewProjMatrix *= Mat4(
            xScale ,    0.0f ,         0.0f ,   0.0f,
            0.0f ,  yScale ,         0.0f ,   0.0f,
            0.0f ,    0.0f ,  (n+f)/(n-f) ,  -1.0f,
            0.0f ,    0.0f ,  2*n*f/(n-f) ,   0.0f)

        rayDirMatrix.set().translate(position)
        rayDirMatrix *= viewProjMatrix
        rayDirMatrix.invert()
    }

    fun setAspectRatio(ar : Float) {
        aspect = ar
        update()
    }
}