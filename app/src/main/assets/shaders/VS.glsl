#version 300 es

uniform mat4 MVP;
uniform mat4 modelMatrix;
uniform mat4 inverseModel;
uniform vec2 texScaling;
uniform vec2 texOffset;

layout(location = 1) in vec3 vPos; //attribute: vertex positions
layout(location = 2) in vec3 normal; // normalized direction vector.
layout(location = 3) in vec2 texCoord; //attribute: texture coordinates



out vec2 varyingTexCoord;
out vec3 transfNormal;
out vec3 fragModel;

//The VS decides where to put a pixel into the screen
void main(){
    vec3 nv = normalize(vPos);
    transfNormal = vec3(inverseModel * vec4(normal,1));
    fragModel = vec3(modelMatrix * vec4(vPos,1));
    varyingTexCoord = texCoord;
    gl_Position = MVP * vec4(vPos,1);
}



