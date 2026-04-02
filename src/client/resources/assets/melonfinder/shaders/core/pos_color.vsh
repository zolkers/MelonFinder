#version 330

layout(std140) uniform MeshData {
    mat4 u_Proj;
    mat4 u_ModelView;
    vec2 u_ScreenSize;
};

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

void main() {
    gl_Position = u_Proj * u_ModelView * vec4(Position, 1.0);
    vertexColor = Color;
}
