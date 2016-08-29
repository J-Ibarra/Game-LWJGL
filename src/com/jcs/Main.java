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
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private boolean running = false;

    int WIDTH = 800;
    int HEIGHT = 600;
    // The window handle
    private long window;

    ShaderProgram shader;
    ShaderProgram instanceShader;

    Matrix4f model;
    Matrix4f view;
    Matrix4f projection;

    boolean[] keys = new boolean[GLFW_KEY_LAST];

    public Camera camera = new Camera();

    double lastX = WIDTH / 2.0;
    double lastY = HEIGHT / 2.0;

    Mesh planet;
    Mesh rock;

    int texturePlanet = -1;
    int textureRock = -1;

    private void init() throws Exception {
        double initTime = glfwGetTime();
        glEnable(GL_DEPTH_TEST);

        shader = new ShaderProgram();
        shader.createVertexShader("shaders/vertex.vs");
        shader.createFragmentShader("shaders/fragment.fs");
        shader.link();

        instanceShader = new ShaderProgram();
        instanceShader.createVertexShader("shaders/instanceVertex.vs");
        instanceShader.createFragmentShader("shaders/instanceFragment.fs");
        instanceShader.link();

        planet = OBJLoader.loadMesh("planet.obj");
        rock = OBJLoader.loadMesh("rock.obj");

        texturePlanet = Texture.getTexture("planet_Quom1200.png");
        textureRock = Texture.getTexture("Rock-Texture-Surface.jpg");

        model = new Matrix4f();
        view = new Matrix4f();
        projection = new Matrix4f();


        random.setSeed(System.currentTimeMillis());
        Quaternionf q = new Quaternionf();

        for (int i = 0; i < amount; i++) {
            float angle = (float) i / (float) amount * 360.0f;
            float displacement = (random.nextInt() % (int) (2 * offset * 100)) / 100.0f - offset;

            float x = (float) (Math.sin(angle) * radius + displacement);
            displacement = (random.nextInt() % (int) (2 * offset * 100)) / 100.0f - offset;
            float y = displacement * 0.4f; // Keep height of asteroid field smaller compared to width of x and z
            displacement = (random.nextInt() % (int) (2 * offset * 100)) / 100.0f - offset;
            float z = (float) (Math.cos(angle) * radius + displacement);

            float scale = (random.nextInt() % 20) / 100.0f + 0.05f;

            float rotAngle = (random.nextInt() % 360);
            q.rotateAxis(rotAngle, new Vector3f(0.4f, 0.6f, 0.8f));

            modelMatrices[i] = new Matrix4f().translate(x, y, z).rotate(q).scale(scale);
        }

        glBindVertexArray(rock.getVaoId());
        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);

        FloatBuffer fb = BufferUtils.createFloatBuffer(modelMatrices.length * 16);
        float[] data = new float[16];
        for (int i = 0; i < modelMatrices.length; i++) {
            fb.put(modelMatrices[i].get(data));
        }
        fb.flip();

        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 4 * 16, 0);
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, 4 * 16, 4 * 4);
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, 4 * 16, 4 * 8);
        glEnableVertexAttribArray(6);
        glVertexAttribPointer(6, 4, GL_FLOAT, false, 4 * 16, 4 * 12);


        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glVertexAttribDivisor(5, 1);
        glVertexAttribDivisor(6, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        System.out.println(glfwGetTime() - initTime);
        glfwShowWindow(window);
    }

    Random random = new Random();
    int amount = 100000;
    Matrix4f[] modelMatrices = new Matrix4f[amount];
    float radius = 100.0f;
    float offset = 2.5f;

    private void update(float deltaTime) {
        movement(deltaTime);
    }

    private void render() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        shader.bind();

        int modelLoc = glGetUniformLocation(shader.programId, "model");
        int viewLoc = glGetUniformLocation(shader.programId, "view");
        int projLoc = glGetUniformLocation(shader.programId, "projection");
        int textLoc = glGetUniformLocation(shader.programId, "texture");

        float[] data = new float[16];

        projection.identity().perspective(camera.Zoom, (float) WIDTH / (float) HEIGHT, 0.1f, 1000.0f);
        glUniformMatrix4fv(projLoc, false, projection.get(data));

        view = camera.getViewMatrix();
        glUniformMatrix4fv(viewLoc, false, view.get(data));

        model.identity();
        glUniformMatrix4fv(modelLoc, false, model.get(data));

        glUniform1i(textLoc, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texturePlanet);

        glBindVertexArray(planet.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, planet.getVertexCount(), GL_UNSIGNED_INT, 0);


        instanceShader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureRock);
        viewLoc = glGetUniformLocation(shader.programId, "view");
        projLoc = glGetUniformLocation(shader.programId, "projection");
        textLoc = glGetUniformLocation(shader.programId, "texture");
        glUniformMatrix4fv(projLoc, false, projection.get(data));
        glUniformMatrix4fv(viewLoc, false, view.get(data));
        glUniform1i(textLoc, 0);
        glBindVertexArray(rock.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElementsInstanced(GL_TRIANGLES, rock.getVertexCount(), GL_UNSIGNED_INT, 0, amount);

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
        if (keys[GLFW_KEY_LEFT_SHIFT])
            deltaTime *= 4f;


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
        shader.cleanup();
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
        //glfwShowWindow(window);

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
