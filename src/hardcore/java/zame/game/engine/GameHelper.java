package zame.game.engine;

import android.content.Context;
import android.widget.Toast;
import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zame.game.Common;
import zame.game.R;

public class GameHelper
{
    public static String initPaths(Context appContext)
    {
        String externalStoragePath = String.format(
            Locale.US,
            "%1$s%2$sAndroid%2$sdata%2$sorg.zamedev.gloomydungeons1hardcore.common",
            Game.getExternalStoragePath(),
            File.separator
        );

        File externalStorageFile = new File(externalStoragePath);

        if (!externalStorageFile.exists()) {
            externalStorageFile.mkdirs();
        }

        return externalStoragePath;
    }
}
