package com.jcs;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
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
    ShaderProgram lightingShader;
    ShaderProgram lampShader;
    //int texture1;
    //int texture2;

    int VBO, VAO;
    int lightVAO;

    Matrix4f model;
    Matrix4f view;
    Matrix4f projection;

    boolean[] keys = new boolean[GLFW_KEY_LAST];

    public Camera camera = new Camera();

    Vector3f lightPos = new Vector3f(1.2f, 1.0f, 2.0f);

    double lastX = WIDTH / 2.0;
    double lastY = HEIGHT / 2.0;

    /*Vector3f[] cubePositions = new Vector3f[]{
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(2.0f, 5.0f, -15.0f),
            new Vector3f(-1.5f, -2.2f, -2.5f),
            new Vector3f(-3.8f, -2.0f, -12.3f),
            new Vector3f(2.4f, -0.4f, -3.5f),
            new Vector3f(-1.7f, 3.0f, -7.5f),
            new Vector3f(1.3f, -2.0f, -2.5f),
            new Vector3f(1.5f, 2.0f, -2.5f),
            new Vector3f(1.5f, 0.2f, -1.5f),
            new Vector3f(-1.3f, 1.0f, -1.5f),
    };


    Random r = new Random();
    Vector3f[] cubeRotations = new Vector3f[]{
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
            new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
    };*/

    private void init() throws Exception {
        glEnable(GL_DEPTH_TEST);

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("shaders/vertex.vs");
        shaderProgram.createFragmentShader("shaders/fragment.fs");
        shaderProgram.link();

        lightingShader = new ShaderProgram();
        lightingShader.createVertexShader("shaders/lighting.vs");
        lightingShader.createFragmentShader("shaders/lighting.fs");
        lightingShader.link();

        lampShader = new ShaderProgram();
        lampShader.createVertexShader("shaders/lamp.vs");
        lampShader.createFragmentShader("shaders/lamp.fs");
        lampShader.link();


        float[] vertices = new float[]{
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,

                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,

                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,

                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f
        };

        /*int[] indices = new int[]{  // Note that we start from 0!
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
                1.0f, 1.0f,  // Lower-left corner
                1.0f, 0.0f,  // Lower-right corner
                0.0f, 0.0f,  // Top-center corner
                0.0f, 1.0f,  // Top-center corner
        };*/

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length);
        fb.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        glBindVertexArray(VAO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        /*int byteOffset = floatByteSize * positionFloatCount;
        glVertexAttribPointer(2, 2, GL_FLOAT, false, vertexFloatSizeInBytes, byteOffset);
        glEnableVertexAttribArray(2);*/


        lightVAO = glGenVertexArrays();
        glBindVertexArray(lightVAO);
        // We only need to bind to the VBO (to link it with glVertexAttribPointer), no need to fill it; the VBO's data already contains all we need.
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //texture1 = Texture.getTexture("container.jpg");
        //texture2 = Texture.getTexture("awesomeFace.png");

        model = new Matrix4f();
        view = new Matrix4f();
        projection = new Matrix4f();


        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    private void update(float deltaTime) {
        movement(deltaTime);
    }

    private void render() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        lightingShader.bind();
        int objectColorLoc = glGetUniformLocation(lightingShader.programId, "objectColor");
        int lightColorLoc = glGetUniformLocation(lightingShader.programId, "lightColor");
        glUniform3f(objectColorLoc, 1.0f, 0.5f, 0.31f);
        glUniform3f(lightColorLoc, 1.0f, 0.5f, 1.0f);

        float[] data = new float[16];

        view = camera.getViewMatrix();
        projection.identity().perspective(camera.Zoom, (float) WIDTH / (float) HEIGHT, 0.1f, 100.0f);

        int modelLoc = glGetUniformLocation(lightingShader.programId, "model");
        int viewLoc = glGetUniformLocation(lightingShader.programId, "view");
        int projLoc = glGetUniformLocation(lightingShader.programId, "projection");

        glUniformMatrix4fv(projLoc, false, projection.get(data));
        glUniformMatrix4fv(viewLoc, false, view.get(data));

        glBindVertexArray(VAO);
        model.identity();
        glUniformMatrix4fv(modelLoc, false, model.get(data));
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        lampShader.bind();
        modelLoc = glGetUniformLocation(lightingShader.programId, "model");
        viewLoc = glGetUniformLocation(lightingShader.programId, "view");
        projLoc = glGetUniformLocation(lightingShader.programId, "projection");

        glUniformMatrix4fv(projLoc, false, projection.get(data));
        glUniformMatrix4fv(viewLoc, false, view.get(data));

        model.identity().translate(lightPos).scale(0.2f);
        glUniformMatrix4fv(modelLoc, false, model.get(data));
        glBindVertexArray(lightVAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);


        /*glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture1);
        glUniform1i(glGetUniformLocation(shaderProgram.programId, "ourTexture1"), 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texture2);
        glUniform1i(glGetUniformLocation(shaderProgram.programId, "ourTexture2"), 1);*/

        shaderProgram.bind();


        // Note: currently we set the projection matrix each frame, but since the projection matrix rarely changes it's often best practice to set it outside the main loop only once.



        glBindVertexArray(VAO);
        /*Quaternionf q = new Quaternionf();
        for (int i = 0; i < cubePositions.length; i++) {
            Vector3f v = cubePositions[i];
            q.identity().rotate(cubeRotations[i].x, cubeRotations[i].y, cubeRotations[i].z);
            model.identity().translate(v).rotate(q);

            glUniformMatrix4fv(modelLoc, false, model.get(data));
            glDrawArrays(GL_TRIANGLES, 0, 36);
        }*/

        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

    }

    private boolean firstMouse = true;

    public void mouseCallbac(long window, double xpos, double ypos) {
        if (firstMouse) {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        float xoffset = (float) (xpos - lastX);
        float yoffset = (float) (lastY - ypos);
        lastX = xpos;
        lastY = ypos;

        camera.ProcessMouseMovement(xoffset, yoffset);
    }

    public void scrollCallback(long window, double xoffset, double yoffset) {
        camera.ProcessMouseScroll((float) yoffset);
    }

    public void movement(float deltaTime) {
        if (keys[GLFW_KEY_W])
            camera.ProcessKeyboard(Camera.Camera_Movement.FORWARD, deltaTime);
        if (keys[GLFW_KEY_S])
            camera.ProcessKeyboard(Camera.Camera_Movement.BACKWARD, deltaTime);
        if (keys[GLFW_KEY_A])
            camera.ProcessKeyboard(Camera.Camera_Movement.LEFT, deltaTime);
        if (keys[GLFW_KEY_D])
            camera.ProcessKeyboard(Camera.Camera_Movement.RIGHT, deltaTime);
    }

    private void initCallbacks() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop

            keys[key] = action != GLFW_RELEASE;
        });

        glfwSetCursorPosCallback(window, this::mouseCallbac);
        glfwSetScrollCallback(window, this::scrollCallback);

        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });
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
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
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
        double lastTimer = glfwGetTime();
        while (running) {

            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            ups++;
            update(deltaTime);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            fps++;
            render();
            glfwSwapBuffers(window); // swap the color buffers


            if (glfwGetTime() - lastTimer > 1) {
                oneSecond(ups, fps);
                ups = fps = 0;
                lastTimer = glfwGetTime();
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
