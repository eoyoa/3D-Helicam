import vision.gears.webglmath.Mat4
import vision.gears.webglmath.Vec3

class Headlight(id: Int, var trackedObject: GameObject, var offset: Vec3) : Light(id) {
    fun update() {
        val transformation = Mat4()
        transformation.set()
            .rotate(trackedObject.roll)
            .translate(trackedObject.position)
        val newPos = transformation * offset.xyz1

        console.log("newPos: ${newPos.x}, ${newPos.y}, ${newPos.z}, ${newPos.w}")

        position.set(newPos)
    }
}