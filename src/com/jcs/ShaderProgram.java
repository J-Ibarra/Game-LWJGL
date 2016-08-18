package com.jcs;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.jcs.utils.IOUtils.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Jcs on 18/8/2016.
 */
public class ShaderProgram {
    private final int programId;
    private int vsId;
    private int fsId;

    public ShaderProgram() throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
    }

    public void createVertexShader(String path) throws Exception {
        vsId = createShader(path, GL_VERTEX_SHADER);
        glAttachShader(programId, vsId);
    }

    public void createFragmentShader(String path) throws Exception {
        fsId = createShader(path, GL_FRAGMENT_SHADER);
        glAttachShader(programId, fsId);
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking shader, log: " + glGetShaderInfoLog(programId));
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Warning validating shader, log: " + glGetShaderInfoLog(programId));
        }

    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            if (vsId != 0) {
                glDetachShader(programId, vsId);
                glDeleteShader(vsId);
            }
            if (fsId != 0) {
                glDetachShader(programId, fsId);
                glDeleteShader(fsId);
            }
            glDeleteProgram(programId);
        }
    }

    protected int createShader(String path, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader, log: " + glGetShaderInfoLog(shaderId));
        }

        ByteBuffer source = ioResourceToByteBuffer(path);
        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer lengths = BufferUtils.createIntBuffer(1);

        strings.put(0, source);
        lengths.put(0, source.remaining());

        glShaderSource(shaderId, strings, lengths);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }
}
