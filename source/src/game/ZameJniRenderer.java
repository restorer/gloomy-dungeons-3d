package {$PKG_CURR};

public class ZameJniRenderer
{
     static {
         System.loadLibrary("zameJniRenderer");
     }

     public static native void renderTriangles(float[] vertexBuffer, float[] colorsBuffer, float[] textureBuffer, short[] indicesBuffer, int indicesBufferPos);
     public static native void renderLines(float[] vertexBuffer, float[] colorsBuffer, int vertexCount);
}
