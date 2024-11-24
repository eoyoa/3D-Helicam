#version 300 es

precision highp float;

in vec4 texCoord;
in vec4 worldNormal;
in vec4 worldPosition;

uniform struct {
  sampler2D colorTexture; 
} material;

uniform struct {
  vec4 position;
  vec3 powerDensity;
} lights[8];

out vec4 fragmentColor;

vec3 shade(
  vec3 normal, vec3 lightDir,
  vec3 powerDensity, vec3 materialColor) {

  float cosa =
    clamp( dot(lightDir, normal),0.0,1.0);
  return
    powerDensity * materialColor * cosa;
}

void main(void) {
  vec3 normal = normalize(worldNormal.xyz);

  fragmentColor.rgb = vec3(0.0, 0.0, 0.0);

  for (int i = 0; i < 3; i++) {
    vec3 lightDiff = lights[i].position.xyz - worldPosition.xyz * lights[i].position.w;
    vec3 lightDir = normalize (lightDiff); // lights[i].position.xyz
    float distanceSquared = dot(lightDiff, lightDiff);
    if (lights[i].position.w < 1.0) {
      distanceSquared = 1.0;
    }
    vec3 powerDensity = lights[i].powerDensity / distanceSquared; //lights[i].powerDensity

    fragmentColor.rgb += shade(normal, lightDir, powerDensity, 
                                texture(material.colorTexture, texCoord.xy/texCoord.w).rgb);
  }

  fragmentColor.w = 1.0;
}