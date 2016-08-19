package com.jcs;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private boolean running = false;

    int WIDTH = 800;
    int HEIGHT = 600;
    // The window handle
    private long window;

    ShaderProgram shaderProgram;

    int texture1;
    int texture2;

    int COUNT, VBO, VAO, EBO, TBO, CBO;

    private void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("shaders/vertex.vs");
        shaderProgram.createFragmentShader("shaders/fragment.fs");
        shaderProgram.link();


        float[] vertices = new float[]{
                0.5f, 0.5f, 0.0f,  // Top Right
                0.5f, -0.5f, 0.0f,  // Bottom Right
                -0.5f, -0.5f, 0.0f,  // Bottom Left
                -0.5f, 0.5f, 0.0f   // Top Left
        };

        int[] indices = new int[]{  // Note that we start from 0!
                0, 1, 3,   // First Triangle
                1, 2, 3    // Second Triangle
        };

        float[] colors = new float[]{
                1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f,
        };

        float[] texCoords = new float[]{
                0.0f, 0.0f,  // Lower-left corner
                1.0f, 0.0f,  // Lower-right corner
                1.0f, 1.0f,  // Top-center corner
                0.0f, 1.0f,  // Top-center corner
        };

        COUNT = indices.length;

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length);
        fb.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        CBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, CBO);
        fb = BufferUtils.createFloatBuffer(colors.length);
        fb.put(colors).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        TBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, TBO);
        fb = BufferUtils.createFloatBuffer(texCoords.length);
        fb.put(texCoords).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);

        EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length);
        ib.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);


        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        texture1 = Texture.getTexture("container.jpg");
        texture2 = Texture.getTexture("awesomeface.png");

        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    private void update() {
        /*double timeValue = glfwGetTime();
        float greenValue = (float) (Math.sin(timeValue) / 2f) + 0.5f;
        int vertexColorLocation = glGetUniformLocation(shaderProgram.programId, "ourColor");
        glUniform4f(vertexColorLocation, 0.0f, greenValue, 0.0f, 1.0f);*/
        Matrix4f trans = new Matrix4f();

        trans.translate(new Vector3f((float) Math.sin(glfwGetTime()), 0f, 0.0f));
        trans.rotate((float) glfwGetTime(), new Vector3f(0.0f, 0.0f, 1.0f));

        float[] data = new float[16];
        int transformLoc = glGetUniformLocation(shaderProgram.programId, "transform");
        glUniformMatrix4fv(transformLoc, false, trans.get(data));


    }

    private void render() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        shaderProgram.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture1);
        glUniform1i(glGetUniformLocation(shaderProgram.programId, "ourTexture1"), 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texture2);
        glUniform1i(glGetUniformLocation(shaderProgram.programId, "ourTexture2"), 1);
        glBindVertexArray(VAO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glDrawElements(GL_TRIANGLES, COUNT, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);


    }

    private void oneSecond(int ups, int fps) {
        System.out.println("ups: " + ups + ", " + fps);
    }

    private void destroy() {
        shaderProgram.cleanup();
    }

    private void initGLFW() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync <- ups and fps = 60 / SwapInterval->
        glfwSwapInterval(2);

        // Make the window visible
        glfwShowWindow(window);
    }


    private void initCallbacks() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
        });
    }

    private void loop() throws Exception {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        init();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        int ups, fps;
        ups = fps = 0;

        glfwSetTime(0);
        double lastTime = glfwGetTime();
        while (running) {

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            ups++;
            update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            fps++;
            render();
            glfwSwapBuffers(window); // swap the color buffers


            if (glfwGetTime() - lastTime > 1) {
                oneSecond(ups, fps);
                ups = fps = 0;
                lastTime = glfwGetTime();
            }

            if (glfwWindowShouldClose(window)) {
                running = false;
            }
        }

        destroy();
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try {

            initGLFW();
            initCallbacks();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void start() {
        running = true;
        run();
    }

    public static void main(String[] args) {
        new Main().start();
    }
}
