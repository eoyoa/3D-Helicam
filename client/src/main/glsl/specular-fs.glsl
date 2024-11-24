#version 300 es

precision highp float;

in vec4 texCoord;
in vec4 worldNormal;
in vec4 worldPosition;

uniform struct {
    sampler2D colorTexture;
    vec3 specularColor;
    float shininess;
} material;

uniform struct{
    mat4 viewProjMatrix;
    vec3 position;
} camera;

uniform struct {
    vec4 position;
    vec3 powerDensity;
} lights[8];

out vec4 fragmentColor;

vec3 shade(
    vec3 normal, vec3 lightDir, vec3 viewDir,
    vec3 powerDensity, vec3 materialColor, vec3 specularColor, float shininess) {

    float cosa = clamp(dot(lightDir, normal), 0.0, 1.0);
    vec3 halfway = normalize(viewDir + lightDir);
    float cosDelta = clamp(dot(halfway, normal), 0.0, 1.0);

    return
    powerDensity * materialColor * cosa
    + powerDensity * specularColor * pow(cosDelta, shininess);
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

        fragmentColor.rgb += shade(normal, lightDir, camera.position - lights[i].position.xyz,
                                    powerDensity, texture(material.colorTexture, texCoord.xy/texCoord.w).rgb,
                                    material.specularColor, material.shininess);
    }

    fragmentColor.w = 1.0;
}