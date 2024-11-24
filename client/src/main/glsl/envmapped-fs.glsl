#version 300 es

precision highp float;

in vec4 modelPosition;
in vec4 worldPosition;
in vec4 texCoord;
in vec4 worldNormal;

uniform struct {
  samplerCube envTexture; 
} material;

uniform struct{
  mat4 viewProjMatrix; 
  vec3 position;
} camera;

out vec4 fragmentColor;

vec3 noiseGrad(vec3 r) {
  uvec3 s = uvec3(
    0x1D4E1D4E,
    0x58F958F9,
    0x129F129F);
  vec3 f = vec3(0, 0, 0);
  for(int i=0; i<16; i++) {
    vec3 sf =
    vec3(s & uvec3(0xFFFF))
  / 65536.0 - vec3(0.5, 0.5, 0.5);
    
    f += cos(dot(sf, r)) * sf;
    s = s >> 1;
  }
  return f;
}

void main(void) {
  vec3 normal = normalize(worldNormal.xyz);
  vec3 normalPertubed = normal;
  normalPertubed += noiseGrad(modelPosition.xyz * 50.0) * 0.05;
  vec3 viewDir = normalize(camera.position.xyz - worldPosition.xyz);
  fragmentColor = texture(material.envTexture, reflect(-viewDir, normalPertubed));
}