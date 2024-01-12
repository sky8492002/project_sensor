package com.choi.sensorproject.ui.opngl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle {
    private val COORDS_PER_VERTEX = 3;

    private var triangleCoords: FloatArray = arrayOf(
        0.0f, 0.5f, 0.0f, // 꼭대기
        -0.5f, -0.5f, 0.0f, // 바닥 왼쪽
        0.5f, -0.5f, 0.0f // 바닥 오른쪽
    ).toFloatArray()

    private val color : FloatArray = arrayOf(1.0f, 1.0f, 1.0f, 1.0f).toFloatArray()

    private var vertexBuffer: FloatBuffer

    val VERTEX_SHADER_CODE = "attribute vec4 vPosition;" +
            "void main(){" +
            "gl_Position = vPosition;" +
            "}";

    val FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){" +
            "gl_FragColor = vColor;" +
            "}"

    private var mProgram : Int = -1;

    init {
        // 모양의 좌표를 위해 byteBuffer을 초기화시킴
        // 좌표 갯수 * 4 (float 하나당 4byte)
        var byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4);

        // 디바이스 하드웨어의 native byte 순서로 바꿈.
        byteBuffer.order(ByteOrder.nativeOrder())

        // 생성한 바이트 버퍼를 float버퍼로 치환하여, vertexBuffer 멤버변수에 floatBuffer을 할당함.
        vertexBuffer = byteBuffer.asFloatBuffer();

        // vertexBuffer에 traingle 좌표를 대입함.
        vertexBuffer.put(triangleCoords)

        // 버퍼의 읽기 포인트를 맨 처음으로 옮김
        vertexBuffer.position(0)

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

    private var positionHandle : Int = -1;
    private var colorHandle: Int = -1


    fun draw() {

        // OpenGL ES 환경에 프로그램을 추가함
        GLES20.glUseProgram(mProgram);

        // vertex Shader의 vPosition memeber을 핸들링함.
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // 삼각형 정점에 대한 핸들링 활성화
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // 삼각형의 색상을 채움.
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // 삼각형 그리기
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCoords.size / COORDS_PER_VERTEX);


        // vertex handle 해제
        GLES20.glDisableVertexAttribArray(positionHandle);

        // color handle 해제
        GLES20.glDisableVertexAttribArray(colorHandle);

    }



}