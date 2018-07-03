package zame.game.engine;

import android.content.Context;
import android.widget.Toast;
import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zame.game.Common;
import zame.game.R;
import zame.game.App;

@SuppressWarnings("WeakerAccess")
public final class GameHelper {
    private GameHelper() {
    }

    public static String initPaths(@SuppressWarnings("UnusedParameters") Context appContext) {
        String externalStoragePath = String.format(Locale.US,
                "%1$s%2$sAndroid%2$sdata%2$szame.GloomyDungeons.common",
                Game.getExternalStoragePath(),
                File.separator);

        File externalStorageFile = new File(externalStoragePath);

        if (!externalStorageFile.exists()) {
            String oldExternalStoragePath = String.format(Locale.US,
                    "%1$s%2$sAndroid%2$sdata%2$s{" + "PKG_COMMON" + "}",
                    Game.getExternalStoragePath(),
                    File.separator);

            File oldExternalStorageFile = new File(oldExternalStoragePath);

            if (oldExternalStorageFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                oldExternalStorageFile.renameTo(externalStorageFile);
            } else {
                oldExternalStoragePath = String.format(Locale.US,
                        "%1$s%2$sAndroid%2$sdata%2$szame.GloomyDungeons.freedemo.common",
                        Game.getExternalStoragePath(),
                        File.separator);

                oldExternalStorageFile = new File(oldExternalStoragePath);

                if (oldExternalStorageFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    oldExternalStorageFile.renameTo(externalStorageFile);
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    externalStorageFile.mkdirs();
                }
            }
        } else {
            String oldExternalStoragePath = String.format(Locale.US,
                    "%1$s%2$sAndroid%2$sdata%2$s{" + "PKG_COMMON" + "}",
                    Game.getExternalStoragePath(),
                    File.separator);

            File oldExternalStorageFile = new File(oldExternalStoragePath);

            if (oldExternalStorageFile.exists()) {
                // both old good folder and folder with bad name exists
                String[] files = oldExternalStorageFile.list();

                if (files != null) {
                    Pattern pat = Pattern.compile("^slot-(\\d)\\.(\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2})\\.save$");

                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < files.length; i++) {
                        Matcher mt = pat.matcher(files[i]);

                        if (mt.find()) {
                            int slotNum = Integer.valueOf(mt.group(1)) - 1;

                            if ((slotNum >= 0) && (slotNum < 4)) {
                                Common.copyFile(String.format(Locale.US,
                                        "%1$s%2$s%3$s",
                                        oldExternalStoragePath,
                                        File.separator,
                                        files[i]),
                                        String.format(Locale.US,
                                                "%1$s%2$sslot-%3$s.%4$s.save",
                                                externalStoragePath,
                                                File.separator,
                                                String.valueOf(slotNum + 5),
                                                mt.group(2)));
                            }
                        }

                        //noinspection ResultOfMethodCallIgnored
                        (new File(String.format(Locale.US,
                                "%1$s%2$s%3$s",
                                oldExternalStoragePath,
                                File.separator,
                                files[i]))).delete();
                    }
                }

                //noinspection ResultOfMethodCallIgnored
                oldExternalStorageFile.delete();

                if (App.self != null) {
                    Toast.makeText(App.self, R.string.msg_old_saves_restored, Toast.LENGTH_LONG).show();
                }
            }
        }

        return externalStoragePath;
    }
}
