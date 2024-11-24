import org.w3c.dom.HTMLCanvasElement
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import kotlin.js.Date
import kotlin.math.cos
import kotlin.math.sin
import org.khronos.webgl.WebGLRenderingContext as GL

class Scene (
  val gl : WebGL2RenderingContext)  : UniformProvider("scene") {

  val vsTextured = Shader(gl, GL.VERTEX_SHADER, "textured-vs.glsl")
  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")
  val fsBackground = Shader(gl, GL.FRAGMENT_SHADER, "background-fs.glsl")
  val fsEnvmapped = Shader(gl, GL.FRAGMENT_SHADER, "envmapped-fs.glsl")

  val texturedProgram = Program(gl, vsTextured, fsTextured)
  val backgroundProgram = Program(gl, vsQuad, fsBackground)
  val envmappedProgram = Program(gl, vsTextured, fsEnvmapped)

  val texturedQuadGeometry = TexturedQuadGeometry(gl)

  val gameObjects = ArrayList<GameObject>()

  val envTexture = TextureCube(gl,
    "media/posx512.jpg", "media/negx512.jpg",
    "media/posy512.jpg", "media/negy512.jpg",
    "media/posz512.jpg", "media/negz512.jpg"
  )  

  val jsonLoader = JsonLoader()
  val slowpokeMeshes = jsonLoader.loadMeshes(gl,
    "media/slowpoke/slowpoke.json",
    Material(texturedProgram).apply{
      this["colorTexture"]?.set(
          Texture2D(gl, "media/slowpoke/YadonDh.png"))
    },
    Material(texturedProgram).apply{
      this["colorTexture"]?.set(
          Texture2D(gl, "media/slowpoke/YadonEyeDh.png"))
    }
  )

  val envmappedSlowpokeMeshes = jsonLoader.loadMeshes(gl,
    "media/slowpoke/slowpoke.json",
    Material(envmappedProgram).apply{
      this["envTexture"]?.set(envTexture)
    },
    Material(envmappedProgram).apply{
      this["envTexture"]?.set(envTexture)
    }
  )

  val backgroundMaterial = Material(backgroundProgram)
  val backgroundMesh = Mesh(backgroundMaterial, texturedQuadGeometry)

  val slowpokeObject = GameObject(*slowpokeMeshes)
  val slowpokeObject2 = GameObject(*slowpokeMeshes)
  val envMappedSlowpokeObject = GameObject(*envmappedSlowpokeMeshes)

  init{
    backgroundMaterial["envTexture"]?.set( this.envTexture )

    gameObjects += slowpokeObject
    slowpokeObject.position.set(5.0f, 10.0f, 5.0f)
    gameObjects += slowpokeObject2
    slowpokeObject2.position.set(5.0f, -10.0f, 5.0f)
    gameObjects += envMappedSlowpokeObject
    gameObjects += GameObject(backgroundMesh)
  }

  val lights = Array<Light>(8) { Light(it) }
  init{
    lights[0].position.set(1.0f, 1.0f, 1.0f, 0.0f).normalize()
    lights[0].powerDensity.set(1.0f, 1.0f, 1.0f)
    lights[1].position.set(0.0f, 0.0f, 0.0f, 1.0f)
    lights[1].powerDensity.set(0f, 100f)
    lights[2] = Headlight(2, slowpokeObject, Vec3(0f, 0f, 10f))
    lights[2].powerDensity.set(0f, 0f, 100f)
  }


  // LABTODO: replace with 3D camera
  val camera = PerspectiveCamera()

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat()/canvas.height)
  }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  init{
    //LABTODO: enable depth test
    gl.enable(GL.DEPTH_TEST)
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f
    timeAtLastFrame = timeAtThisFrame

    slowpokeObject.roll += 2 * dt
    slowpokeObject.position += Vec3(0.25f * sin(t), 0f)

    //LABTODO: move camera
    camera.move(dt, keysPressed)
    camera.update()
    lights[1].position.set(sin(t), 0f, cos(t), 1f)
    
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    gl.enable(GL.BLEND)
    gl.blendFunc(
      GL.SRC_ALPHA,
      GL.ONE_MINUS_SRC_ALPHA)

    gameObjects.forEach{ it.move(dt, t, keysPressed, gameObjects) }
    gameObjects.forEach{ it.update() }

    lights.forEach { if (it is Headlight) it.update() }

    gameObjects.forEach{ it.draw(this, camera, *lights) }
  }
}
