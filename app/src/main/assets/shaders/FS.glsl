#version 300 es

precision mediump float;

uniform sampler2D moondiff;  // Uniform int value passed from host. It is a 2D texture GL object
uniform sampler2D moonspec;  // Uniform int value passed from host. It is a 2D texture GL object

uniform vec3 lightPos;
uniform vec3 eyePos;

uniform int usingLightD; //se uso la dash light
uniform int lightDPos; //quale parte deve essere illuminata

uniform int usingSensor; //se uso il sensore
uniform vec4 sensorAreasPos;//quale parte deve essere illuminata


in vec2 varyingTexCoord;
in vec3 transfNormal;
in vec3 fragModel;
out vec4 fragColor; //output to rest of the graphic pipeline


/*
Funzione che serve a capire se il pixel si trova in una parte del rettangolo scelto in base ad un
valore n.

Ritorna true se il valore se il pixel è nella parte compresa nel n, false altrimenti.
*/
bool checkArea(int n, vec2 flipped_texcoord){

    if (n <= 0){
        return false;
    }

    // ogni spia occupa 30% di larghezza e 50% di altezza
    float width = 0.333;
    float height = 0.5;
    float x = float((n - 1) / 2) * width;// quale colonna sceglere
    float y = float((n - 1) % 2) * height;// quale riga scegliere

    // ritorna vero se il pixel si trova in tale area, falso altrimenti.
    return (x < flipped_texcoord.x && flipped_texcoord.x < (x + width) &&
    y < flipped_texcoord.y && flipped_texcoord.y < (y + height));

}

/*
Funzione che serve ad assegnare il colore ad un pixel in base ad un valore preciso.
    -- Bianco: se il valore è maggiore di 0.75
    -- Giallo: se il valore è compreso tra 0.25 e 0.75
    -- Rosso: se il valore è minore di 0.25

Ritorna il colore RGBA.
*/
vec4 colorPixel(float n){

    if (n > 0.0 && n < 0.25)
        return vec4(1, 0, 0, 1);//Rosso
    if (n > 0.25 && n < 0.75)
        return vec4(1, 1, 0, 1);//Giallo
    return vec4(1, 1, 1, 1);//Bianco
}

/*
Funzione che serve a capire se il pixel si trova in una delle 4 parti del cerchio in base ad un
valore di threshold 1/2:
Ritorna in colore RGBA in base al valore comrpesso nel vettore sensorAreasPos.
*/
vec4 checkSensorArea(vec2 flipped_texcoord){
    float n = 0.5;
    if (flipped_texcoord.x < n){
        if (flipped_texcoord.y > n)
            return colorPixel(sensorAreasPos[0]);
        else
            return colorPixel(sensorAreasPos[1]);
    }else{
        if (flipped_texcoord.y > n)
            return colorPixel(sensorAreasPos[2]);
        else
            return colorPixel(sensorAreasPos[3]);
    }
}

// The FS decides the color for that pixel
void main() {
    vec3 eyeDir = normalize(eyePos-fragModel);
    vec3 lightDir = normalize(lightPos-fragModel);
    float diffuse = max(dot(lightDir, transfNormal), 0.0);
    vec3 halfWay = normalize(lightDir + eyeDir);
    float specular = pow(max(dot(halfWay, transfNormal), 0.0), 1.0);

    //Blender coord parsedsfr
    vec2 flipped_texcoord = vec2(varyingTexCoord.x, 1.0 - varyingTexCoord.y);

    vec4 diffuseMap = texture(moondiff, flipped_texcoord);  // retrieve a rgba floating value from texture
    vec4 specMap = texture(moonspec, flipped_texcoord);
    vec4 ambientMap = mix(diffuseMap, specMap, vec4(0.5));
    vec4 ambientComponent = 0.15 * ambientMap;

    //vettore temporaneo del colore
    vec4 tmp = ambientComponent;

    if (usingLightD > 0){
        if (checkArea(lightDPos, flipped_texcoord)){
            if (tmp.xyz != vec3(0, 0, 0)){
                tmp *= vec4(10, 10, 10, 1);//moltiplico quei pixel che non sono neri di un fattore 10
            }
        }
    }
    if (usingSensor > 0){
        tmp = checkSensorArea(flipped_texcoord);
    }

    tmp += ambientComponent + diffuse*diffuseMap + specular*specMap;
    fragColor = tmp;

}
