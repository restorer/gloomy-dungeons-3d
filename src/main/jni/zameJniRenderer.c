#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedImportStatement"

#include <jni.h>
#include <GLES/gl.h>

JNIEXPORT void JNICALL Java_zame_game_ZameJniRenderer_renderTriangles(
        JNIEnv* env,
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
        jobject obj,
#pragma clang diagnostic pop
        jfloatArray vertexBuffer,
        jfloatArray colorsBuffer,
        jfloatArray textureBuffer,
        jshortArray indicesBuffer,
        jint indicesBufferPos) {

    jfloat* nativeVertexBuffer = (*env)->GetFloatArrayElements(env, vertexBuffer, NULL);
    jfloat* nativeColorsBuffer = (*env)->GetFloatArrayElements(env, colorsBuffer, NULL);

    jfloat* nativeTextureBuffer = (textureBuffer == NULL ? NULL : (*env)->GetFloatArrayElements(env,
            textureBuffer,
            NULL));

    jshort* nativeIndicesBuffer = (*env)->GetShortArrayElements(env, indicesBuffer, NULL);

    glVertexPointer(3, GL_FLOAT, 0, nativeVertexBuffer);
    glColorPointer(4, GL_FLOAT, 0, nativeColorsBuffer);

    if (nativeTextureBuffer != NULL) {
        glTexCoordPointer(2, GL_FLOAT, 0, nativeTextureBuffer);
    }

    glDrawElements(GL_TRIANGLES, indicesBufferPos, GL_UNSIGNED_SHORT, nativeIndicesBuffer);

    (*env)->ReleaseShortArrayElements(env, indicesBuffer, nativeIndicesBuffer, 0);

    if (nativeTextureBuffer != NULL) {
        (*env)->ReleaseFloatArrayElements(env, textureBuffer, nativeTextureBuffer, 0);
    }

    (*env)->ReleaseFloatArrayElements(env, colorsBuffer, nativeColorsBuffer, 0);
    (*env)->ReleaseFloatArrayElements(env, vertexBuffer, nativeVertexBuffer, 0);
}

JNIEXPORT void JNICALL Java_zame_game_ZameJniRenderer_renderLines(
        JNIEnv* env,
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
        jobject obj,
#pragma clang diagnostic pop
        jfloatArray vertexBuffer,
        jfloatArray colorsBuffer,
        jint vertexCount) {

    jfloat* nativeVertexBuffer = (*env)->GetFloatArrayElements(env, vertexBuffer, NULL);
    jfloat* nativeColorsBuffer = (*env)->GetFloatArrayElements(env, colorsBuffer, NULL);

    glVertexPointer(2, GL_FLOAT, 0, nativeVertexBuffer);
    glColorPointer(4, GL_FLOAT, 0, nativeColorsBuffer);
    glDrawArrays(GL_LINES, 0, vertexCount);

    (*env)->ReleaseFloatArrayElements(env, colorsBuffer, nativeColorsBuffer, 0);
    (*env)->ReleaseFloatArrayElements(env, vertexBuffer, nativeVertexBuffer, 0);
}

#pragma clang diagnostic pop
