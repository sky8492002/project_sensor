package com.choi.sensorproject.opngl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10


class Phone {
    private var bitmapImage: Bitmap? = null

    private val COORDS_PER_VERTEX = 3

    private var matrixModel: FloatArray = arrayOf(
        -0.1f, -0.0f, -0.1f,
        0.5f, -0.5f, -0.5f,
        0.1f, 0.0f, -0.1f,
        -0.5f, 0.5f, -0.5f,
        -0.1f, -0.0f, 0.1f,
        0.5f, -0.5f, 0.5f,
        0.1f, 0.0f, 0.1f,
        -0.5f, 0.5f, 0.5f
    ).toFloatArray()

    var uvs = floatArrayOf(
        0f, 1f,
        1f, 1f,
        1f, 0f,
        0f, 0f
    )

    val order = arrayOf(
        0, 1, 3, 3, 1, 2, // Front face.
        0, 1, 4, 4, 5, 1, // Bottom face.
        1, 2, 5, 5, 6, 2, // Right face.
        2, 3, 6, 6, 7, 3, // Top face.
        3, 7, 4, 4, 3, 0, // Left face.
        4, 5, 7, 7, 6, 5, // Rear face.
    ).toIntArray() //삼각형 그리는 순서 (012),(023)

    val orderBuffer = ByteBuffer.allocateDirect(order.size * 4).let { //
        it.order(ByteOrder.nativeOrder())
        it.asIntBuffer()
    }.apply {
        put(order)
        position(0)
    } //드로잉 순서 버퍼를 만듭니다.

    private val color : FloatArray = arrayOf(
        0.0f, 1.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f).toFloatArray()

    private var vertexBuffer: FloatBuffer
    private var uvBuffer: FloatBuffer

    val VERTEX_SHADER_CODE = "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}"


    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "attribute vec2 v_texCoord;" +
            "uniform sampler2D s_texture;"+
            "void main(){" +
            "gl_FragColor = texture2D(s_texture, v_texCoord);" +
            //"gl_FragColor = vColor;" +
            "}"

    private var mProgram : Int = -1;

    init {

        // 모양의 좌표를 위해 byteBuffer을 초기화시킴
        // 좌표 갯수 * 4 (float 하나당 4byte)
        var byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(matrixModel.size * 4)
        var uvByteBuffer : ByteBuffer = ByteBuffer.allocateDirect(uvs.size * 4)

        // 디바이스 하드웨어의 native byte 순서로 바꿈.
        byteBuffer.order(ByteOrder.nativeOrder())

        // 생성한 바이트 버퍼를 float버퍼로 치환하여, vertexBuffer 멤버변수에 floatBuffer을 할당함.
        vertexBuffer = byteBuffer.asFloatBuffer()
        uvBuffer = uvByteBuffer.asFloatBuffer()

        // vertexBuffer에 traingle 좌표를 대입함.
        vertexBuffer.put(matrixModel)
        uvBuffer.put(uvs)

        // 버퍼의 읽기 포인트를 맨 처음으로 옮김
        vertexBuffer.position(0)
        uvBuffer.position(0)

        val vertexShader : Int = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader : Int = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        mProgram = GLES20.glCreateProgram();

        //  vertexShader, fragmentShader을 프로그램에 장착
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        // 실행
        GLES20.glLinkProgram(mProgram)
    }

    fun loadShader(type : Int, shaderCode : String) : Int {

        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    fun setTexture(bitmap: Bitmap){
        bitmapImage = bitmap
    }

    private fun bindTexture(textureHandle: Int){
        //val textures = IntArray(1)
        //GLES20.glGenTextures(1, textures, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapImage, 0)
    }

    private fun getImageHandle(): Int {
        val textureNames = IntArray(1)
        GLES20.glGenTextures(1, textureNames, 0) // 텍스처를 생성한다.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0) // 텍스처를 활성화한다.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[0]) // 텍스처를 바인딩한다.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapImage, 0) // 텍스처를 연동한다.
        return textureNames[0]
    }

    private var positionHandle : Int = -1
    private var colorHandle: Int = -1
    private var vPMatrixHandle: Int = 0
    private var uvHandle: Int = 0
    private var textureHandle: Int = 0

    fun draw(mvpMatrix: FloatArray) {

        // OpenGL ES 환경에 프로그램을 추가함
        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        uvHandle = GLES20.glGetAttribLocation(mProgram, "v_texCoord")
        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, uvBuffer)

        textureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture" )


        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // 색상을 채움.
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        bindTexture(textureHandle)

        // 그리기
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCoords.size / COORDS_PER_VERTEX);
        GLES20.glDrawElements(GL10.GL_TRIANGLES, order.size, GLES20.GL_UNSIGNED_INT, orderBuffer)


        // vertex handle 해제
        GLES20.glDisableVertexAttribArray(positionHandle)

        // color handle 해제
        GLES20.glDisableVertexAttribArray(colorHandle)

        GLES20.glDisableVertexAttribArray(uvHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
    }
}