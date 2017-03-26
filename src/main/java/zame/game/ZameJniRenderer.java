package zame.game;

@SuppressWarnings("WeakerAccess")
public final class ZameJniRenderer {
    static {
        System.loadLibrary("zameJniRenderer");
    }

    private ZameJniRenderer() {
    }

    public static native void renderTriangles(float[] vertexBuffer,
            float[] colorsBuffer,
            float[] textureBuffer,
            short[] indicesBuffer,
            int indicesBufferPos);

    public static native void renderLines(float[] vertexBuffer, float[] colorsBuffer, int vertexCount);
}
