package com.choi.sensorproject.opngl

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_UNSIGNED_INT
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10.GL_TRIANGLES

class Square {
    private val COORDS_PER_VERTEX = 3;
    private val VERTEX_STRIDE: Int = COORDS_PER_VERTEX * 4

//    private var triangleCoords: FloatArray = arrayOf(
//        -0.5f,  0.5f, //0번 정점
//        -0.5f, -0.5f, //1번 정점
//        0.5f, -0.5f,  //2번 정점
//        0.5f,  0.5f  //3번 정점
//    ).toFloatArray()

    private var vertices = floatArrayOf(
        -0.5f, -0.8f, 0.0f,
        0.5f, -0.8f, 0.0f,
        -0.5f, 0.8f, 0.0f,
        0.5f, 0.8f, 0.0f
    )

    private var texCoords = floatArrayOf(
        0f,1f, 0.0f,
        1f,1f, 0.0f,
        0f,0f, 0.0f,
        1f,0f, 0.0f,
    )

    val order = arrayOf(0,1,2,0,2,3).toIntArray() //삼각형 그리는 순서 (012),(023)

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
    private var texCoordBuffer: FloatBuffer

    val VERTEX_SHADER_CODE =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vTexCoord = aTexCoord;"+
            "}"


    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "varying vec2 vTexCoord;"+
            "uniform sampler2D sTexture;"+
            "void main() {" +
            "gl_FragColor = texture2D(sTexture, vTexCoord);" +
            "}"

//    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
//            "uniform vec4 vColor;" +
//            "void main(){" +
//            "gl_FragColor = vColor;" +
//            "}"

    private var mProgram : Int = -1;

    init {
        // 모양의 좌표를 위해 byteBuffer을 초기화시킴
        // 좌표 갯수 * 4 (float 하나당 4byte)
        val byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        val textCoordByteBuffer : ByteBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)

        // 디바이스 하드웨어의 native byte 순서로 바꿈.
        byteBuffer.order(ByteOrder.nativeOrder())
        textCoordByteBuffer.order(ByteOrder.nativeOrder()) // 이 줄이 없으면 텍스펴 표시 안됨

        // 생성한 바이트 버퍼를 float버퍼로 치환하여, vertexBuffer 멤버변수에 floatBuffer을 할당함.
        vertexBuffer = byteBuffer.asFloatBuffer();
        texCoordBuffer = textCoordByteBuffer.asFloatBuffer()

        // vertexBuffer에 traingle 좌표를 대입함.
        vertexBuffer.put(vertices)
        texCoordBuffer.put(texCoords)

        // 버퍼의 읽기 포인트를 맨 처음으로 옮김
        vertexBuffer.position(0)
        texCoordBuffer.position(0)
    }

    fun loadShader(type : Int, shaderCode : String) : Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private var positionHandle : Int = -1;
    private var colorHandle: Int = -1
    private var vPMatrixHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var textureHandle: Int = 0
    private val textureUnit = IntArray(1)

    fun readyToDraw(bitmapImage: Bitmap){
        val vertexShader : Int = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader : Int = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        mProgram = GLES20.glCreateProgram();

        //  vertexShader, fragmentShader을 프로그램에 장착
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        // 실행
        GLES20.glLinkProgram(mProgram)
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        texCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord")
        textureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture")

//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
//        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//
//        GLES20.glGenTextures(textureUnit.size, textureUnit, 0)
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureUnit[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapImage, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D) // 메모리 캐싱 문제 해결 기능

        bitmapImage.recycle()
    }

    fun draw(mvpMatrix: FloatArray) {

        // OpenGL ES 환경에 프로그램을 추가함
        GLES20.glUseProgram(mProgram)

        //GLES20.glUniform1i(textureHandle, 0)

        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
        //GLES20.glUniform4fv(colorHandle, 1, color, 0)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, texCoordBuffer)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glEnableVertexAttribArray(textureHandle)

        //GLES20.glDrawElements(GL_TRIANGLES, order.size, GL_UNSIGNED_INT, orderBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)

    }

}