package com.jcs;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by Jcs on 19/8/2016.
 */
public class Camera {

    public enum Camera_Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    ;

    // Camera Attributes
    public Vector3f Position;
    public Vector3f Front;
    public Vector3f Up;
    public Vector3f Right;
    public Vector3f WorldUp;
    // Eular Angles
    public float Yaw;
    public float Pitch;
    // Camera options
    public float MovementSpeed;
    public float MouseSensitivity;
    public float Zoom;

    public Camera() {
        Position = new Vector3f(0.0f, 0.0f, 0.0f);
        WorldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Yaw = -90.0f;
        Pitch = 0.0f;
        Front = new Vector3f(0.0f, 0.0f, -1.0f);
        MovementSpeed = 3.0f;
        MouseSensitivity = 0.25f;
        Zoom = 45.0f;

        Right = new Vector3f();
        Up = new Vector3f();

        updateCameraVectors();
    }

    public Camera(float posX, float posY, float posZ,
                  float upX, float upY, float upZ,
                  float yaw, float pitch) {
        this();
        Position = new Vector3f(posX, posY, posZ);
        WorldUp = new Vector3f(upX, upY, upZ);
        Yaw = yaw;
        Pitch = pitch;
        updateCameraVectors();
    }

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this();
        Position = position;
        WorldUp = up;
        Yaw = yaw;
        Pitch = pitch;
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(Position, Position.add(Front, new Vector3f()), Up);
    }

    void ProcessKeyboard(Camera_Movement direction, float deltaTime) {
        float velocity = MovementSpeed * deltaTime;
        if (direction == Camera_Movement.FORWARD)
            Position.add(Front.mul(velocity, new Vector3f()));
        if (direction == Camera_Movement.BACKWARD)
            Position.sub(Front.mul(velocity, new Vector3f()));
        if (direction == Camera_Movement.LEFT)
            Position.sub(Right.mul(velocity, new Vector3f()));
        if (direction == Camera_Movement.RIGHT)
            Position.add(Right.mul(velocity, new Vector3f()));
    }

    void ProcessMouseMovement(float xoffset, float yoffset) {
        xoffset *= MouseSensitivity;
        yoffset *= MouseSensitivity;

        Yaw += xoffset;
        Pitch += yoffset;

        // Make sure that when pitch is out of bounds, screen doesn't get flipped

        if (Pitch > 89.0f)
            Pitch = 89.0f;
        if (Pitch < -89.0f)
            Pitch = -89.0f;


        // Update Front, Right and Up Vectors using the updated Eular angles
        updateCameraVectors();
    }

    void ProcessMouseScroll(float yoffset) {
        if (Zoom >= 1.0f && Zoom <= 45.0f)
            Zoom -= yoffset;
        if (Zoom <= 1.0f)
            Zoom = 1.0f;
        if (Zoom >= 45.0f)
            Zoom = 45.0f;
    }


    /**
     * Calculates the front vector from the Camera's (updated) Eular Angles
     */
    private void updateCameraVectors() {

        // Calculate the new Front vector
        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(Math.toRadians(Yaw)) * Math.cos(Math.toRadians(Pitch)));
        front.y = (float) Math.sin(Math.toRadians(Pitch));
        front.z = (float) (Math.sin(Math.toRadians(Yaw)) * Math.cos(Math.toRadians(Pitch)));
        Front = front.normalize();

        // Also re-calculate the Right and Up vector
        Right = Front.cross(WorldUp, Right).normalize(); //glm::normalize(glm::cross(this->Front, this->WorldUp));  // Normalize the vectors, because their length gets closer to 0 the more you look up or down which results in slower movement.
        Up = Right.cross(Front, Up).normalize(); //glm::normalize(glm::cross(this->Right, this->Front));

    }
}
