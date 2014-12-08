package zame.game;

import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import com.zeemote.zc.Controller;
import com.zeemote.zc.event.BatteryEvent;
import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.ControllerEvent;
import com.zeemote.zc.event.DisconnectEvent;
import com.zeemote.zc.event.IButtonListener;
import com.zeemote.zc.event.IJoystickListener;
import com.zeemote.zc.event.IStatusListener;
import com.zeemote.zc.event.JoystickEvent;
import com.zeemote.zc.ui.android.ControllerAndroidUi;
import zame.game.Common;
import zame.game.engine.Controls;

public class GameActivityZeemoteHelper implements IStatusListener, IJoystickListener, IButtonListener
{
    private static final int MENU_ITEM_ZEEMOTE = 4;

    private Controller zeemoteController = null;
    private ControllerAndroidUi zeemoteControllerUi = null;
    private boolean keepConnection = false;

    public int getMenuResId()
    {
        return R.menu.game_zeemote;
    }

    public void onPrepareOptionsMenu(Menu menu)
    {
        menu.findItem(R.id.menu_zeemote).setVisible(Config.controlsType == Controls.TYPE_ZEEMOTE);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_zeemote:
                if (zeemoteControllerUi != null) {
                    zeemoteControllerUi.showControllerMenu();
                    keepConnection = true;
                }
                return true;
        }

        return false;
    }

    public void onStart(GameActivity activity)
    {
        if (Config.controlsType == Controls.TYPE_ZEEMOTE) {
            if (zeemoteController == null) {
                zeemoteController = new Controller(1);
                zeemoteController.addStatusListener(this);
                zeemoteController.addButtonListener(this);
                zeemoteController.addJoystickListener(this);
            }

            if (zeemoteControllerUi == null) {
                zeemoteControllerUi = new ControllerAndroidUi(activity, zeemoteController);
                keepConnection = false;
            }

            if (!keepConnection && !zeemoteController.isConnected()) {
                zeemoteControllerUi.startConnectionProcess();
                keepConnection = true;
            } else {
                keepConnection = false;
            }
        }
    }

    public void onPause()
    {
        if (!keepConnection && zeemoteController != null && zeemoteController.isConnected()) {
            try {
                zeemoteController.disconnect();
            } catch (Exception ex) {
                Log.e(Common.LOG_KEY, "Exception", ex);
            }
        }
    }

    @Override
    public void batteryUpdate(BatteryEvent event)
    {
    }

    @Override
    public void connected(ControllerEvent event)
    {
        Controls.initJoystickVars();
    }

    @Override
    public void disconnected(DisconnectEvent event)
    {
        Controls.initJoystickVars();
    }

    @Override
    public void joystickMoved(JoystickEvent e)
    {
        if (Config.controlsType == Controls.TYPE_ZEEMOTE) {
            Controls.joyX = (float)(e.getScaledX(-100, 100)) / 150.0f * ConfigZeemote.zeemoteXAccel;
            Controls.joyY = - (float)(e.getScaledY(-100, 100)) / 150.0f * ConfigZeemote.zeemoteYAccel;
        }
    }

    @Override
    public void buttonPressed(ButtonEvent e)
    {
        if (Config.controlsType == Controls.TYPE_ZEEMOTE) {
            int buttonId = e.getButtonGameAction();

            if ((buttonId >= 0) && (buttonId < ConfigZeemote.zeemoteButtonMappings.length)) {
                Controls.joyButtonsMask |= ConfigZeemote.zeemoteButtonMappings[buttonId];
            }
        }
    }

    @Override
    public void buttonReleased(ButtonEvent e)
    {
        if (Config.controlsType == Controls.TYPE_ZEEMOTE) {
            int buttonId = e.getButtonGameAction();

            if ((buttonId >= 0) && (buttonId < ConfigZeemote.zeemoteButtonMappings.length)) {
                Controls.joyButtonsMask &= ~(ConfigZeemote.zeemoteButtonMappings[buttonId]);
            }
        }
    }
}
