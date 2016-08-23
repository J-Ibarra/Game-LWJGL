package com.jcs;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.util.Random;

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

    ShaderProgram lightingShader;
    ShaderProgram lampShader;

    int VBO, VAO;
    int lightVAO;

    Matrix4f model;
    Matrix4f view;
    Matrix4f projection;

    boolean[] keys = new boolean[GLFW_KEY_LAST];

    public Camera camera = new Camera();

    double lastX = WIDTH / 2.0;
    double lastY = HEIGHT / 2.0;

    int textureDiffuse, textureSpecular, textureMatrix;

    Vector3f[] cubePositions = new Vector3f[]{
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
    };

    Vector3f pointLightPositions[] = new Vector3f[]{
            new Vector3f(0.7f, 0.2f, 2.0f),
            new Vector3f(2.3f, -3.3f, -4.0f),
            new Vector3f(-4.0f, 2.0f, -12.0f),
            new Vector3f(0.0f, 0.0f, -3.0f)
    };

    private void init() throws Exception {
        glEnable(GL_DEPTH_TEST);

        lightingShader = new ShaderProgram();
        lightingShader.createVertexShader("shaders/lighting.vs");
        lightingShader.createFragmentShader("shaders/lighting.fs");
        lightingShader.link();

        lampShader = new ShaderProgram();
        lampShader.createVertexShader("shaders/lamp.vs");
        lampShader.createFragmentShader("shaders/lamp.fs");
        lampShader.link();


        float[] vertices = new float[]{
                // Positions          // Normals           // Texture Coords
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,

                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
        };

        int floatByteSize = 4;
        int positionFloatCount = 3;
        int normalFloatCount = 3;
        int textureFloatCunt = 2;
        int floatsPerVertex = positionFloatCount + normalFloatCount + textureFloatCunt;
        int vertexFloatSizeInBytes = floatByteSize * floatsPerVertex;

        /**
         * Cube
         * */
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        VBO = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length);
        fb.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexFloatSizeInBytes, 0);
        glEnableVertexAttribArray(0);
        int byteOffset = floatByteSize * positionFloatCount;
        glVertexAttribPointer(1, 3, GL_FLOAT, false, vertexFloatSizeInBytes, byteOffset);
        glEnableVertexAttribArray(1);
        byteOffset = floatByteSize * (positionFloatCount + normalFloatCount);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, vertexFloatSizeInBytes, byteOffset);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);


        /**
         * Light
         */

        lightVAO = glGenVertexArrays();
        glBindVertexArray(lightVAO);
        // We only need to bind to the VBO (to link it with glVertexAttribPointer), no need to fill it; the VBO's data already contains all we need.
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexFloatSizeInBytes, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        model = new Matrix4f();
        view = new Matrix4f();
        projection = new Matrix4f();


        textureDiffuse = Texture.getTexture("container2.png");
        textureSpecular = Texture.getTexture("container2_specular.png");
        textureMatrix = Texture.getTexture("matrix.jpg");
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    private void update(float deltaTime) {
        movement(deltaTime);
    }

    private void render() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);


        /**
         * Render Scene
         */
        lightingShader.bind();

        int viewPosLoc = glGetUniformLocation(lightingShader.programId, "viewPos");
        glUniform3f(viewPosLoc, camera.Position.x, camera.Position.y, camera.Position.z);

        int matDiffuseLoc = glGetUniformLocation(lightingShader.programId, "material.diffuse");
        int matSpecularLoc = glGetUniformLocation(lightingShader.programId, "material.specular");
        int matEmissionLoc = glGetUniformLocation(lightingShader.programId, "material.emission");
        int matShineLoc = glGetUniformLocation(lightingShader.programId, "material.shininess");

        glUniform1i(matDiffuseLoc, 0);
        glUniform1i(matSpecularLoc, 1);
        glUniform1i(matEmissionLoc, 2);
        glUniform1f(matShineLoc, 32.0f);

        int dirLightDirLoc = glGetUniformLocation(lightingShader.programId, "dirLight.direction");
        int dirLightAmbLoc = glGetUniformLocation(lightingShader.programId, "dirLight.ambient");
        int dirLightDifLoc = glGetUniformLocation(lightingShader.programId, "dirLight.diffuse");
        int dirLightSpeLoc = glGetUniformLocation(lightingShader.programId, "dirLight.specular");

        glUniform3f(dirLightDirLoc, -0.2f, -1.0f, -0.3f);
        glUniform3f(dirLightAmbLoc, 0.05f, 0.05f, 0.05f);
        glUniform3f(dirLightDifLoc, 0.4f, 0.4f, 0.4f);
        glUniform3f(dirLightSpeLoc, 0.5f, 0.5f, 0.5f);

        for (int i = 0; i < pointLightPositions.length; i++) {
            int pointPosLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].position");
            int pointAmbLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].ambient");
            int pointDifLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].diffuse");
            int pointSpeLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].specular");
            int pointConLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].constant");
            int pointLinLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].linear");
            int pointQuaLoc = glGetUniformLocation(lightingShader.programId, "pointLights[" + i + "].quadratic");


            glUniform3f(pointPosLoc, pointLightPositions[i].x, pointLightPositions[i].y, pointLightPositions[i].z);
            glUniform3f(pointAmbLoc, 0.05f, 0.05f, 0.05f);
            glUniform3f(pointDifLoc, 0.8f, 0.8f, 0.8f);
            glUniform3f(pointSpeLoc, 1.0f, 1.0f, 1.0f);
            glUniform1f(pointConLoc, 1.0f);
            glUniform1f(pointLinLoc, 0.09f);
            glUniform1f(pointQuaLoc, 0.032f);
        }

        float[] data = new float[16];

        view = camera.getViewMatrix();
        projection.identity().perspective(camera.Zoom, (float) WIDTH / (float) HEIGHT, 0.1f, 100.0f);

        int modelLoc = glGetUniformLocation(lightingShader.programId, "model");
        int viewLoc = glGetUniformLocation(lightingShader.programId, "view");
        int projLoc = glGetUniformLocation(lightingShader.programId, "projection");

        glUniformMatrix4fv(projLoc, false, projection.get(data));
        glUniformMatrix4fv(viewLoc, false, view.get(data));

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureDiffuse);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureSpecular);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, textureMatrix);


        glBindVertexArray(VAO);
        Quaternionf q = new Quaternionf();
        float angle = (float) glfwGetTime();
        for (int i = 0; i < cubePositions.length; i++) {
            Vector3f v = cubePositions[i];
            q.identity().rotateAxis(angle, cubeRotations[i]);
            model.identity().translate(v).rotate(q);

            glUniformMatrix4fv(modelLoc, false, model.get(data));
            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
        glBindVertexArray(0);


        /**
         * Render Lamp
         */
        lampShader.bind();
        modelLoc = glGetUniformLocation(lampShader.programId, "model");
        viewLoc = glGetUniformLocation(lampShader.programId, "view");
        projLoc = glGetUniformLocation(lampShader.programId, "projection");

        glUniformMatrix4fv(projLoc, false, projection.get(data));
        glUniformMatrix4fv(viewLoc, false, view.get(data));

        glBindVertexArray(lightVAO);
        for (int i = 0; i < pointLightPositions.length; i++) {
            Vector3f v = pointLightPositions[i];
            model.identity().translate(v).scale(0.1f);
            glUniformMatrix4fv(modelLoc, false, model.get(data));
            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
        glBindVertexArray(0);

    }

    private boolean firstMouse = true;

    public void mouseCallback(long window, double xpos, double ypos) {
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
            if (key == GLFW_KEY_UNKNOWN)
                return;

            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop

            keys[key] = action != GLFW_RELEASE;
        });

        glfwSetCursorPosCallback(window, this::mouseCallback);
        glfwSetScrollCallback(window, this::scrollCallback);

        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });
    }

    private void oneSecond(int ups, int fps) {
        System.out.println("ups: " + ups + ", " + fps);
    }

    private void destroy() {

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
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidMode.width() - WIDTH) / 2,
                (vidMode.height() - HEIGHT) / 2
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
