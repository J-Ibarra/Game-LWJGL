#version 330 core

//in vec3 ourColor;
//in vec2 TexCoord;

out vec4 color;

//uniform sampler2D ourTexture1;
//uniform sampler2D ourTexture2;
uniform vec3 objectColor;
uniform vec3 lightColor;

void main() {
   //color = mix(texture(ourTexture1, TexCoord), texture(ourTexture2, TexCoord),0.2);
   color = vec4(lightColor * objectColor, 1.0f);
}