package com.jcs;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.jcs.utils.IOUtils.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * Created by Jcs on 14/8/2016.
 */
public class Texture {
    public int id;
    public int width;
    public int height;

    public Texture(String path) {
        ByteBuffer image;
        ByteBuffer imageBuffer;

        try {
            imageBuffer = ioResourceToByteBuffer(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        IntBuffer wb = BufferUtils.createIntBuffer(1);
        IntBuffer hb = BufferUtils.createIntBuffer(1);
        IntBuffer comb = BufferUtils.createIntBuffer(1);

        if (!stbi_info_from_memory(imageBuffer, wb, hb, comb))
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

        image = stbi_load_from_memory(imageBuffer, wb, hb, comb, 0);
        if (image == null)
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

        width = wb.get(0);
        height = hb.get(0);
        int comp = comb.get(0);

        id = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, id);

        if (comp == 3) {
            if ((width & 3) != 0)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width & 1));
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, 0);

    }

    public static int getTexture(String path) {
        ByteBuffer image;
        ByteBuffer imageBuffer;

        try {
            imageBuffer = ioResourceToByteBuffer(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        IntBuffer wb = BufferUtils.createIntBuffer(1);
        IntBuffer hb = BufferUtils.createIntBuffer(1);
        IntBuffer comb = BufferUtils.createIntBuffer(1);

        if (!stbi_info_from_memory(imageBuffer, wb, hb, comb))
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

        image = stbi_load_from_memory(imageBuffer, wb, hb, comb, 0);
        if (image == null)
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

        int width = wb.get(0);
        int height = hb.get(0);
        int comp = comb.get(0);

        int idTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, idTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);    // Set texture wrapping to GL_REPEAT
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        // Set texture filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        if (comp == 3) {
            if ((width & 3) != 0)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width & 1));
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);
        return idTexture;
    }

    public void activeTexture(int sampler) {
        if (sampler >= 0 && sampler <= 31) {
            glActiveTexture(GL_TEXTURE0 + sampler);
        } else {
            throw new RuntimeException("can not activate the GL_TEXTURE" + sampler);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
