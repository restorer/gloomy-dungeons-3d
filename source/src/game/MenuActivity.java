package {$PKG_CURR};

import android.app.*;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.util.Log;
import android.util.Pair;
import android.graphics.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.widget.Toast;
import android.content.Context;
import android.content.res.Resources;
import android.widget.*;
import android.os.Environment;
import android.media.AudioManager;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.net.Uri;

import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.google.android.apps.analytics.easytracking.EasyTracker;

public class MenuActivity extends TrackedActivity
{
	public static class InitialSetupEntryItemSetting
	{
		public String name;
		public String value;

		public InitialSetupEntryItemSetting(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
	}

	public static class InitialSetupEntryItem
	{
		public int imageResourceId;
		public String type;
		public InitialSetupEntryItemSetting[] settings;

		public InitialSetupEntryItem(int imageResourceId, String type, InitialSetupEntryItemSetting[] settings)
		{
			this.imageResourceId = imageResourceId;
			this.type = type;
			this.settings = settings;
		}
	}

	public static class InitialSetupEntry
	{
		public String title;
		public InitialSetupEntryItem[] items;

		public InitialSetupEntry(String title, InitialSetupEntryItem[] items)
		{
			this.title = title;
			this.items = items;
		}
	}

	private static final int VIEW_TYPE_MENU = 1;
	private static final int VIEW_TYPE_INITIAL_SETUP = 2;

	private static final int DIALOG_NEW_GAME_WARN = 1;
	private static final int DIALOG_LOAD_WARN = 2;
	private static final int DIALOG_LOAD_SLOTS = 3;
	private static final int DIALOG_SAVE_SLOTS = 4;
	private static final int DIALOG_CHECK_SOUND = 5;
	private static final int DIALOG_ABOUT = 6;

	// actuallty 4 slots is enough, but this is required to fix issued with bad-named saves folder
	private static final int MAX_SLOTS = 8;

	private static int currentIndex;
	private static int lastSaveSlot = 0;
	private static ArrayList<String> slotStringsForLoad = new ArrayList<String>();
	private static ArrayList<String> slotFileNamesForLoad = new ArrayList<String>();
	private static ArrayList<String> slotStringsForSave = new ArrayList<String>();
	private static ArrayList<String> slotFileNamesForSave = new ArrayList<String>();

	private static ArrayAdapter<String> loadSlotsAdapter;
	private static ArrayAdapter<String> saveSlotsAdapter;
	private static AlertDialog saveSlotsDialog;
	private static AlertDialog aboutDialog;

	private static Context appContext;
	private static Resources resources;

	private static boolean instantMusicPause = true;
	public static boolean justLoaded = false;	// or just saved, or just new game started

	private int currentViewType = 0;
	private InitialSetupEntry[] initialSetupEntries;
	private int currentInitialSetupIdx;
	private int currentInitialSetupItemIdx;

	public static MenuActivity self;

	@Override
	protected void onCreate(Bundle state)
	{
		super.onCreate(state);
		self = this;

		appContext = getApplicationContext();
		resources = getResources();

		Game.initPaths(appContext);

		SoundManager.init(appContext, getAssets(), true);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
		PreferenceManager.setDefaultValues(appContext, R.xml.preferences, false);

		boolean firstRun = sp.getBoolean("FirstRun", true);

		if (firstRun) {
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putBoolean("FirstRun", false);
			spEditor.commit();
		}

		if (sp.getBoolean("RunInitialSetup", true)) {
			setInitialSetupView();
		} else {
			setMenuView();
		}

		if (firstRun) {
			showDialog(DIALOG_CHECK_SOUND);
		}
	}

	protected void ensureInitialSetupEntries()
	{
		if (initialSetupEntries == null) {
			initialSetupEntries = new InitialSetupEntry[] {
				new InitialSetupEntry(
					getString(R.string.setup_controls),
					new InitialSetupEntryItem[] {
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_improved,
							getString(R.string.setup_ctl_improved),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "Improved")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_left_pad,
							getString(R.string.setup_ctl_left_pad),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "PadL")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_right_pad,
							getString(R.string.setup_ctl_right_pad),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "PadR")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_experimental_a,
							getString(R.string.setup_ctl_experimental_a),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "ExperimentalA")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_experimental_b,
							getString(R.string.setup_ctl_experimental_b),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "ExperimentalB")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_zeemote,
							getString(R.string.setup_ctl_zeemote),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "Zeemote")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_ctl_classic,
							getString(R.string.setup_ctl_classic),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("ControlsType", "Classic")
							}
						),
					}
				),
				new InitialSetupEntry(
					getString(R.string.setup_smoothing),
					new InitialSetupEntryItem[] {
						new InitialSetupEntryItem(
							R.drawable.sel_smooth_lv_n_weap_y,
							getString(R.string.setup_smooth_optimal),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("LevelTextureSmoothing", "@false"),
								new InitialSetupEntryItemSetting("WeaponsTextureSmoothing", "@true")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_smooth_lv_y_weap_y,
							getString(R.string.setup_smooth_maximum),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("LevelTextureSmoothing", "@true"),
								new InitialSetupEntryItemSetting("WeaponsTextureSmoothing", "@true")
							}
						),
						new InitialSetupEntryItem(
							R.drawable.sel_smooth_lv_n_weap_n,
							getString(R.string.setup_smooth_hardcore),
							new InitialSetupEntryItemSetting[] {
								new InitialSetupEntryItemSetting("LevelTextureSmoothing", "@false"),
								new InitialSetupEntryItemSetting("WeaponsTextureSmoothing", "@false")
							}
						)
					}
				)
			};
		}

		if (currentInitialSetupIdx < 0) {
			currentInitialSetupIdx = 0;
		}

		if (currentInitialSetupIdx >= initialSetupEntries.length) {
			currentInitialSetupIdx = initialSetupEntries.length - 1;
		}

		if (currentInitialSetupItemIdx < 0) {
			currentInitialSetupItemIdx = 0;
		}

		if (currentInitialSetupItemIdx >= initialSetupEntries[currentInitialSetupIdx].items.length) {
			currentInitialSetupItemIdx = initialSetupEntries[currentInitialSetupIdx].items.length - 1;
		}
	}

	protected void setInitialSetupView()
	{
		setContentView(R.layout.initial_setup);
		currentViewType = VIEW_TYPE_INITIAL_SETUP;
		Typeface btnTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + getString(R.string.font_name));

		((Button)findViewById(R.id.BtnPrev)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnSelect)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnNext)).setTypeface(btnTypeface);
		((TextView)findViewById(R.id.TxtTitle)).setTypeface(btnTypeface);
		((TextView)findViewById(R.id.TxtType)).setTypeface(btnTypeface);

		ensureInitialSetupEntries();

		((Button)findViewById(R.id.BtnPrev)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				ensureInitialSetupEntries();
				currentInitialSetupItemIdx = (currentInitialSetupItemIdx - 1 + initialSetupEntries[currentInitialSetupIdx].items.length) % initialSetupEntries[currentInitialSetupIdx].items.length;
				updateInitialSetupView();
			}
		});

		((Button)findViewById(R.id.BtnNext)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				ensureInitialSetupEntries();
				currentInitialSetupItemIdx = (currentInitialSetupItemIdx + 1) % initialSetupEntries[currentInitialSetupIdx].items.length;
				updateInitialSetupView();
			}
		});

		((Button)findViewById(R.id.BtnSelect)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				ensureInitialSetupEntries();
				InitialSetupEntryItemSetting[] settings = initialSetupEntries[currentInitialSetupIdx].items[currentInitialSetupItemIdx].settings;
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
				SharedPreferences.Editor spEditor = sp.edit();

				for (InitialSetupEntryItemSetting item : settings)
				{
					if (item.value.equals("@false")) {
						spEditor.putBoolean(item.name, false);
					} else if (item.value.equals("@true")) {
						spEditor.putBoolean(item.name, true);
					} else {
						spEditor.putString(item.name, item.value);
					}
				}

				spEditor.commit();
				currentInitialSetupIdx++;
				currentInitialSetupItemIdx = 0;

				if (currentInitialSetupIdx < initialSetupEntries.length) {
					updateInitialSetupView();
				} else {
					spEditor = sp.edit();
					spEditor.putBoolean("RunInitialSetup", false);
					spEditor.commit();

					setMenuView();
					onResumeMenuView();
				}
			}
		});

		currentInitialSetupIdx = 0;
		currentInitialSetupItemIdx = 0;
		updateInitialSetupView();
	}

	protected void updateInitialSetupView()
	{
		((TextView)findViewById(R.id.TxtTitle)).setText(initialSetupEntries[currentInitialSetupIdx].title);
		((View)findViewById(R.id.InitialSetupView)).setBackgroundResource(initialSetupEntries[currentInitialSetupIdx].items[currentInitialSetupItemIdx].imageResourceId);
		((TextView)findViewById(R.id.TxtType)).setText(initialSetupEntries[currentInitialSetupIdx].items[currentInitialSetupItemIdx].type);
	}

	protected void setMenuView()
	{
		setContentView(R.layout.menu);
		currentViewType = VIEW_TYPE_MENU;
		Typeface btnTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + getString(R.string.font_name));

		((Button)findViewById(R.id.BtnContinue)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnNewGame)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnLoad)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnSave)).setTypeface(btnTypeface);

		((Button)findViewById(R.id.BtnContinue)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				EasyTracker.getTracker().trackPageView("/menu/continue");
				startGame(Game.INSTANT_NAME);
			}
		});

		((Button)findViewById(R.id.BtnNewGame)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				EasyTracker.getTracker().trackPageView("/menu/new-game");

				if (hasInstantSave() && !justLoaded) {
					showDialog(DIALOG_NEW_GAME_WARN);
				} else {
					startGame("");
				}
			}
		});

		loadSlotsAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, slotStringsForLoad);
        saveSlotsAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, slotStringsForSave);

		((Button)findViewById(R.id.BtnLoad)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				EasyTracker.getTracker().trackPageView("/menu/load");

				fillSlots(slotStringsForLoad, slotFileNamesForLoad, true);
				loadSlotsAdapter.notifyDataSetChanged();
				showDialog(DIALOG_LOAD_SLOTS);
			}
		});

		((Button)findViewById(R.id.BtnSave)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				EasyTracker.getTracker().trackPageView("/menu/save");

				fillSlots(slotStringsForSave, slotFileNamesForSave, false);
				saveSlotsAdapter.notifyDataSetChanged();
				currentIndex = (saveSlotsDialog == null ? 0 : saveSlotsDialog.getListView().getCheckedItemPosition());
				showDialog(DIALOG_SAVE_SLOTS);
			}
		});
	}

	protected void onResumeMenuView()
	{
		// slots for load dialog filled in updateButtonsState()
		updateButtonsState();

		// fix bug with pressing HOME when save/load dialogs is active
		fillSlots(slotStringsForSave, slotFileNamesForSave, false);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (currentViewType == VIEW_TYPE_MENU) {
			onResumeMenuView();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		// moved from onResume, because onResume called even when app is not visible, but lock screen is visible
		if (hasFocus) {
			SoundManager.setPlaylist(SoundManager.LIST_MAIN);
			SoundManager.onStart();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		SoundManager.onPause(instantMusicPause);
		instantMusicPause = true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		self = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_about:
				showDialog(DIALOG_ABOUT);

				final TextView dlgText = (TextView)aboutDialog.findViewById(android.R.id.message);
				dlgText.setMovementMethod(LinkMovementMethod.getInstance());
				dlgText.setTextColor(0xFFFFFFFF);
				return true;

			case R.id.menu_options:
				startActivity(new Intent(MenuActivity.this, GamePreferencesActivity.class));
				return true;

			case R.id.menu_exit:
				finish();
				return true;
		}

		return false;
	}

	private void updateButtonsState()
	{
		((Button)findViewById(R.id.BtnContinue)).setEnabled(hasInstantSave());
		((Button)findViewById(R.id.BtnSave)).setEnabled(hasInstantSave());
		((Button)findViewById(R.id.BtnLoad)).setEnabled(fillSlots(slotStringsForLoad, slotFileNamesForLoad, true) > 0);
	}

	private static boolean hasInstantSave()
	{
		return (new File(Game.INSTANT_PATH)).exists();
	}

	private void startGame(String saveName)
	{
		if (!saveName.equals(Game.INSTANT_NAME)) {
			// new game started or loaded non-instant state
			justLoaded = true;
		}

		Game.savedGameParam = saveName;
		instantMusicPause = false;
		startActivity(new Intent(MenuActivity.this, GameActivity.class));
	}

	private static int fillSlots(ArrayList<String> slotStrings, ArrayList<String> slotFileNames, boolean hideUnused)
	{
		slotStrings.clear();
		slotFileNames.clear();

		String[] files = (new File(Game.SAVES_FOLDER)).list();

		if (files == null) {
			return 0;
		}

		Pattern pat = Pattern.compile("^slot\\-(\\d)\\.(\\d{4}\\-\\d{2}\\-\\d{2})\\-(\\d{2})\\-(\\d{2})\\.save$");
		HashMap<Integer, Pair<String, String>> saves = new HashMap<Integer, Pair<String, String>>();

		for (int i = 0; i < files.length; i++)
		{
			Matcher mt = pat.matcher(files[i]);

			if (mt.find())
			{
				int slotNum = Integer.valueOf(mt.group(1)) - 1;

				if (slotNum >= 0 && slotNum < MAX_SLOTS)
				{
					saves.put(Integer.valueOf(slotNum), new Pair(
						(new StringBuilder("Slot ").append(slotNum + 1).append(": ")
							.append(mt.group(2)).append(" ").append(mt.group(3)).append(":").append(mt.group(4))
						).toString(),
						files[i].substring(0, files[i].length() - 5)
					));
				}
			}
		}

		for (int i = 0; i < MAX_SLOTS; i++)
		{
			Pair<String, String> pair = saves.get(Integer.valueOf(i));

			if (pair != null)
			{
				slotStrings.add(pair.first);
				slotFileNames.add(pair.second);
			}
			else if (!hideUnused)
			{
				slotStrings.add((new StringBuilder("Slot ").append(i + 1)
					.append(": <").append(resources.getString(R.string.val_empty)).append(">")
				).toString());

				slotFileNames.add("");
			}
		}

		return saves.size();
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DIALOG_CHECK_SOUND:
			{
				return new AlertDialog.Builder(MenuActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_enable_sound)
					.setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
							SharedPreferences.Editor spEditor = sp.edit();
							spEditor.putBoolean("EnableSound", true);
							spEditor.commit();

							SoundManager.onStart();
						}
					})
					.setNegativeButton(R.string.dlg_no, null)
					.create();
			}

			case DIALOG_NEW_GAME_WARN:
			{
				return new AlertDialog.Builder(MenuActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_new_game)
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startGame("");
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();
			}

			case DIALOG_LOAD_WARN:
			{
				return new AlertDialog.Builder(MenuActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_new_game)
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startGame(slotFileNamesForLoad.get(currentIndex));
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();
			}

			case DIALOG_LOAD_SLOTS:
				return new AlertDialog.Builder(MenuActivity.this)
					.setTitle(R.string.dlg_select_slot_load)
					.setAdapter(loadSlotsAdapter, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							currentIndex = which;

							if (hasInstantSave() && !justLoaded) {
								showDialog(DIALOG_LOAD_WARN);
							} else {
								startGame(slotFileNamesForLoad.get(currentIndex));
							}
						}
					})
					.create();

			case DIALOG_SAVE_SLOTS:
			{
				saveSlotsDialog = new AlertDialog.Builder(MenuActivity.this)
					.setTitle(R.string.dlg_select_slot_save)
					.setSingleChoiceItems(saveSlotsAdapter, 0, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							currentIndex = which;
						}
					})
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							Calendar cal = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

							String newSaveName = (new StringBuilder(Game.SAVES_ROOT)
								.append("slot-")
								.append(currentIndex + 1)
								.append(".")
								.append(sdf.format(cal.getTime()))
								.append(".save")
							).toString();

							if (copyFile(Game.INSTANT_PATH, newSaveName + ".new"))
							{
								if (slotFileNamesForSave.get(currentIndex).length() != 0) {
									(new File(Game.SAVES_ROOT + slotFileNamesForSave.get(currentIndex) + ".save")).delete();
								}

								(new File(newSaveName + ".new")).renameTo(new File(newSaveName));

								Toast.makeText(MenuActivity.appContext, R.string.msg_game_saved, Toast.LENGTH_LONG).show();
								updateButtonsState();
								justLoaded = true;	// just saved
							}
						}
					})
					.setNegativeButton(R.string.dlg_cancel, null)
					.create();

					saveSlotsDialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
					return saveSlotsDialog;
			}

			case DIALOG_ABOUT:
			{
				aboutDialog = new AlertDialog.Builder(MenuActivity.this)
					.setTitle(R.string.dlg_about_title)
					.setMessage(Html.fromHtml(appContext.getText(R.string.dlg_about_text).toString()))
					.setPositiveButton(R.string.dlg_ok, null)
					.create();

				return aboutDialog;
			}
		}

		return null;
	}

	public static boolean copyFile(String srFile, String dtFile)
	{
		boolean success = true;

		try
		{
			InputStream in = new FileInputStream(srFile);
			OutputStream out = new FileOutputStream(dtFile);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
		}
		catch (FileNotFoundException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (SecurityException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (IOException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			Toast.makeText(MenuActivity.appContext, R.string.msg_cant_copy_state, Toast.LENGTH_LONG).show();
		}

		return success;
	}
}
