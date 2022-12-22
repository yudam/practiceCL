#version 120

attribute vec4 aPosition;

uniform mat4 mvpMatrix;

layout (std140) uniform StaticUniform{

    mat4 projection;
    mat4 view;
    mat4 model;
};


void main() {

    gl_Position = projection * view * model * aPosition;
}
