package zame.game;

import android.app.Activity;
import android.os.Build;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public abstract class BaseActivity extends Activity {
    private MenuInflater overriddenMenuInflater;

    public void appOpenOptionsMenu(View anchor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            PopupMenu popupMenu = new PopupMenu(this, anchor);

            overriddenMenuInflater = popupMenu.getMenuInflater();
            onCreateOptionsMenu(popupMenu.getMenu());
            onPrepareOptionsMenu(popupMenu.getMenu());
            overriddenMenuInflater = null;

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });

            popupMenu.show();
            return;
        }

        openOptionsMenu();
    }

    @Override
    public MenuInflater getMenuInflater() {
        return ((overriddenMenuInflater == null) ? super.getMenuInflater() : overriddenMenuInflater);
    }
}
