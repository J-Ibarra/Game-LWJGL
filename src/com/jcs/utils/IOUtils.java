package com.jcs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.newByteChannel;
import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * Created by Jcs on 18/8/2016.
 */
public class IOUtils {

    public static ByteBuffer ioResourceToByteBuffer(String resource) {
        try {
            ByteBuffer buffer;
            Path path = Paths.get(currentThread().getContextClassLoader().getResource(resource).toURI());
            if (isReadable(path)) {
                SeekableByteChannel fc = newByteChannel(path);
                buffer = createByteBuffer((int) fc.size() + 1);

                while (fc.read(buffer) != -1) ;

                buffer.flip();
                return buffer;
            } else
                return null;
        } catch (Exception e) {
            throw new RuntimeException("could not load resource: " + resource, e.getCause());
        }
    }

    public static String ioReadFileAsString(String resource) throws IOException {
        try {
            StringBuilder result = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(currentThread().getContextClassLoader().getResourceAsStream(resource)));
            String line;
            while ((line = br.readLine()) != null)
                result.append(line).append('\n');
            br.close();
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("could not load resource: " + resource, e.getCause());
        }
    }
}
