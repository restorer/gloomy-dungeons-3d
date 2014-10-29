package zame.game.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zame.game.Common;
import zame.game.GameActivity;
import zame.game.MenuActivity;
import zame.game.R;
import zame.game.SoundManager;
import zame.game.ZameApplication;
import zame.game.engine.Game;

public class MenuView extends RelativeLayout
{
	public static class Data
	{
		public int currentIndex;
		public ArrayList<String> slotStringsForLoad;
		public ArrayList<String> slotFileNamesForLoad;
		public ArrayList<String> slotStringsForSave;
		public ArrayList<String> slotFileNamesForSave;

		public ArrayAdapter<String> loadSlotsAdapter;
		public ArrayAdapter<String> saveSlotsAdapter;

		public AlertDialog aboutDialog;
		public AlertDialog saveSlotsDialog;
	}

	private static final int DIALOG_NEW_GAME_WARN = 101;
	private static final int DIALOG_LOAD_WARN = 102;
	private static final int DIALOG_LOAD_SLOTS = 103;
	private static final int DIALOG_SAVE_SLOTS = 104;
	private static final int DIALOG_ABOUT = 105;

	// actuallty 4 slots is enough, but this is required to fix issued with bad-named saves folder
	private static final int MAX_SLOTS = 8;

	private MenuActivity activity;
	private Data data;

	public MenuView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		activity = (MenuActivity)context;
		data = activity.menuViewData;
	}

	public static void onActivityCreate(MenuActivity activity)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		Common.setTypeface(this, new int[] {
			R.id.BtnContinue,
			R.id.BtnNewGame,
			R.id.BtnLoad,
			R.id.BtnSave,
			R.id.TxtHelp,
		});

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

		((Button)findViewById(R.id.BtnContinue)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				startGame(activity, Game.INSTANT_NAME);
			}
		});

		((Button)findViewById(R.id.BtnNewGame)).setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				if (hasInstantSave() && !MenuActivity.justLoaded) {
					activity.showDialog(DIALOG_NEW_GAME_WARN);
				} else {
					startGame(activity, "");
				}
			}
		});

		((Button)findViewById(R.id.BtnLoad)).setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				fillSlots(data.slotStringsForLoad, data.slotFileNamesForLoad, true);
				data.loadSlotsAdapter.notifyDataSetChanged();
				activity.showDialog(DIALOG_LOAD_SLOTS);
			}
		});

		((Button)findViewById(R.id.BtnSave)).setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				ZameApplication.trackEvent("Menu", "Save", "", 0);
				ZameApplication.flushEvents();

				fillSlots(data.slotStringsForSave, data.slotFileNamesForSave, false);
				data.saveSlotsAdapter.notifyDataSetChanged();
				data.currentIndex = (data.saveSlotsDialog == null ? 0 : data.saveSlotsDialog.getListView().getCheckedItemPosition());
				activity.showDialog(DIALOG_SAVE_SLOTS);
			}
		});

		data.slotStringsForLoad = new ArrayList<String>();
		data.slotFileNamesForLoad = new ArrayList<String>();
		data.slotStringsForSave = new ArrayList<String>();
		data.slotFileNamesForSave = new ArrayList<String>();

		data.loadSlotsAdapter = new ArrayAdapter<String>(
			activity,
			android.R.layout.select_dialog_item,
			data.slotStringsForLoad
		);

		data.saveSlotsAdapter = new ArrayAdapter<String>(
			activity,
			android.R.layout.select_dialog_singlechoice,
			data.slotStringsForSave
		);

		updateSlotsAndButtons();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			updateSlotsAndButtons();
		}
	}

	private void updateSlotsAndButtons()
	{
		fillSlots(data.slotStringsForSave, data.slotFileNamesForSave, false);

		((Button)findViewById(R.id.BtnContinue)).setEnabled(hasInstantSave());
		((Button)findViewById(R.id.BtnSave)).setEnabled(hasInstantSave());

		((Button)findViewById(R.id.BtnLoad)).setEnabled(fillSlots(
			data.slotStringsForLoad,
			data.slotFileNamesForLoad,
			true
		) > 0);
	}

	private int fillSlots(ArrayList<String> slotStrings, ArrayList<String> slotFileNames, boolean hideUnused)
	{
		slotStrings.clear();
		slotFileNames.clear();

		String[] files = (new File(Game.SAVES_FOLDER)).list();

		if (files == null) {
			return 0;
		}

		Pattern pat = Pattern.compile("^slot\\-(\\d)\\.(\\d{4}\\-\\d{2}\\-\\d{2})\\-(\\d{2})\\-(\\d{2})\\.save$");
		HashMap<Integer, Pair<String, String>> saves = new HashMap<Integer, Pair<String, String>>();

		for (int i = 0; i < files.length; i++) {
			Matcher mt = pat.matcher(files[i]);

			if (mt.find()) {
				int slotNum = Integer.valueOf(mt.group(1)) - 1;

				if (slotNum >= 0 && slotNum < MAX_SLOTS) {
					saves.put(Integer.valueOf(slotNum), new Pair<String, String>(
						String.format(Locale.US, "Slot %d: %s %s:%s", slotNum + 1, mt.group(2), mt.group(3), mt.group(4)),
						files[i].substring(0, files[i].length() - 5)
					));
				}
			}
		}

		for (int i = 0; i < MAX_SLOTS; i++) {
			Pair<String, String> pair = saves.get(Integer.valueOf(i));

			if (pair != null) {
				slotStrings.add(pair.first);
				slotFileNames.add(pair.second);
			} else if (!hideUnused) {
				try {
					slotStrings.add(String.format(Locale.US, "Slot %d: <%s>", i + 1, activity.getString(R.string.val_empty)));
				} catch (Exception ex) {
					slotStrings.add(String.format(Locale.US, "Slot %d: <Empty>", i + 1));
				}

				slotFileNames.add("");
			}
		}

		return saves.size();
	}

	@SuppressWarnings("deprecation")
	public static boolean onOptionsItemSelected(final MenuActivity activity, MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.menu_about:
				activity.showDialog(DIALOG_ABOUT);
				final TextView dlgText = (TextView)activity.menuViewData.aboutDialog.findViewById(android.R.id.message);
				dlgText.setMovementMethod(LinkMovementMethod.getInstance());
				dlgText.setTextColor(0xFFFFFFFF);
				return true;

			case R.id.menu_site_help:
				Common.openBrowser(activity, "http://mobile.zame-dev.org/gloomy/help.php?hl=" + Locale.getDefault().getLanguage().toLowerCase());
				return true;

			case R.id.menu_exit:
				if (MenuViewHelper.canExit(activity)) {
					activity.finish();
				}

				return true;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public static Dialog onCreateDialog(MenuActivity ownerActivity, int id)
	{
		final MenuActivity activity = ownerActivity;
		final Data data = activity.menuViewData;

		switch (id) {
			case DIALOG_ABOUT: {
				data.aboutDialog = new AlertDialog.Builder(activity)
					.setTitle(R.string.dlg_about_title)
					.setMessage(Html.fromHtml(activity.getApplicationContext().getText(R.string.dlg_about_text).toString()))
					.setPositiveButton(R.string.dlg_ok, null)
					.create();

				return data.aboutDialog;
			}

			case DIALOG_NEW_GAME_WARN: {
				return new AlertDialog.Builder(activity)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_new_game)
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startGame(activity, "");
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();
			}

			case DIALOG_LOAD_WARN: {
				return new AlertDialog.Builder(activity)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_new_game)
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startGame(activity, data.slotFileNamesForLoad.get(data.currentIndex));
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();
			}

			case DIALOG_LOAD_SLOTS: {
				return new AlertDialog.Builder(activity)
					.setTitle(R.string.dlg_select_slot_load)
					.setAdapter(data.loadSlotsAdapter, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							data.currentIndex = which;

							if (hasInstantSave() && !MenuActivity.justLoaded) {
								activity.showDialog(DIALOG_LOAD_WARN);
							} else {
								startGame(activity, data.slotFileNamesForLoad.get(data.currentIndex));
							}
						}
					})
					.create();
			}

			case DIALOG_SAVE_SLOTS: {
				data.saveSlotsDialog = new AlertDialog.Builder(activity)
					.setTitle(R.string.dlg_select_slot_save)
					.setSingleChoiceItems(data.saveSlotsAdapter, 0, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							data.currentIndex = which;
						}
					})
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String newSaveName = String.format(
								Locale.US,
								"%sslot-%d.%s.save",
								Game.SAVES_ROOT,
								data.currentIndex + 1,
								(new SimpleDateFormat("yyyy-MM-dd-HH-mm")).format(Calendar.getInstance().getTime())
							);

							if (Common.copyFile(Game.INSTANT_PATH, newSaveName + ".new")) {
								if (data.slotFileNamesForSave.get(data.currentIndex).length() != 0) {
									(new File(Game.SAVES_ROOT + data.slotFileNamesForSave.get(data.currentIndex) + ".save")).delete();
								}

								(new File(newSaveName + ".new")).renameTo(new File(newSaveName));

								Toast.makeText(ZameApplication.self, R.string.msg_game_saved, Toast.LENGTH_LONG).show();
								MenuActivity.justLoaded = true;	// just saved
							}
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();

					data.saveSlotsDialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
					return data.saveSlotsDialog;
			}
		}

		return MenuViewHelper.onCreateDialog(activity, data, id);
	}

	private static void startGame(MenuActivity activity, String saveName)
	{
		if (!saveName.equals(Game.INSTANT_NAME)) {
			// new game started or loaded non-instant state
			MenuActivity.justLoaded = true;
		}

		Game.savedGameParam = saveName;
		activity.instantMusicPause = false;
		activity.startActivity(new Intent(activity, GameActivity.class));
	}

	private static boolean hasInstantSave()
	{
		return (new File(Game.INSTANT_PATH)).exists();
	}
}
