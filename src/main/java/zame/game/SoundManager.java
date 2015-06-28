package zame.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SoundManager
{
	private static class PlayList
	{
		public String[] list;
		public int idx;

		public PlayList(String[] list)
		{
			this.list = list;
		}
	}

	private static Context appContext;
	private static AssetManager assetManager;
	private static volatile MediaPlayer mediaPlayer = null;
	private static volatile SoundPool soundPool = null;

	public static PlayList LIST_MAIN = new PlayList(new String[] {
		"l1.mid",
		"l2.mid",
		"l3.mid",
		"l4.mid",
	});

	public static PlayList LIST_ENDL = new PlayList(new String[] { "endl.mid" });
	public static PlayList LIST_GAMEOVER = new PlayList(new String[] { "gameover.mid" });

	public static final int SOUND_BTN_PRESS = 0;
	public static final int SOUND_NOWAY = 1;
	public static final int SOUND_DOOR_OPEN = 2;
	public static final int SOUND_DOOR_CLOSE = 3;
	public static final int SOUND_SHOOT_PIST = 4;
	public static final int SOUND_SHOOT_SHTG = 5;
	public static final int SOUND_LEVEL_START = 6;
	public static final int SOUND_LEVEL_END = 7;
	public static final int SOUND_SWITCH = 8;
	public static final int SOUND_PICK_ITEM = 9;
	public static final int SOUND_PICK_AMMO = 10;
	public static final int SOUND_PICK_WEAPON = 11;
	public static final int SOUND_SHOOT_EAT = 12;
	public static final int SOUND_DETH_HERO = 13;
	public static final int SOUND_SHOOT_HAND = 14;
	public static final int SOUND_SHOOT_DBLSHTG = 15;
	public static final int SOUND_SHOOT_SAW = 16;
	public static final int SOUND_LAST = 17;

	public static final int SOUND_DETH_MON = SOUND_DETH_HERO;

	private static int[] soundIds = new int[SOUND_LAST];
	private static float[] soundVolumes = new float[SOUND_LAST];

	private static PlayList current = null;
	private static boolean musicLoaded = false;
	private static volatile Timer pauseTimer = null;
	private static volatile TimerTask pauseTask = null;

	private static boolean soundEnabled = false;
	private static float musicVolume = 1.0f;
	private static float effectsVolume = 1.0f;

	private static class PauseTask extends TimerTask
	{
		public void run()
		{
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}

			pauseTask = null;
		}
	}

	public static synchronized void init(Context context, AssetManager assets, boolean reInitialize)
	{
		appContext = context;
		assetManager = assets;

		if (reInitialize)
		{
			if (pauseTimer != null) {
				pauseTimer.purge();
			} else {
				pauseTimer = new Timer();
			}
		}
		else if (pauseTimer == null)
		{
			pauseTimer = new Timer();
		}

		if (mediaPlayer == null)
		{
			mediaPlayer = new MediaPlayer();
			// setWakeMode

			mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
			{
				public boolean onError(MediaPlayer mp, int what, int extra)
				{
					Log.e(Common.LOG_KEY, "MediaPlayer error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
					return false;
				}
			});

			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
			{
				public void onCompletion(MediaPlayer mp)
				{
					if (current != null)
					{
						current.idx = (current.idx + 1) % current.list.length;
						play(true);
					}
				}
			});
		}

		if (soundPool == null)
		{
			soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

			loadSound("btn_press", SOUND_BTN_PRESS, 1.0f);
			loadSound("noway", SOUND_NOWAY, 1.0f);
			loadSound("door_open", SOUND_DOOR_OPEN, 0.5f);
			loadSound("door_close", SOUND_DOOR_CLOSE, 0.5f);
			loadSound("shoot_pist", SOUND_SHOOT_PIST, 0.5f);
			loadSound("shoot_shtg", SOUND_SHOOT_SHTG, 0.75f);
			loadSound("level_start", SOUND_LEVEL_START, 0.75f);
			loadSound("level_end", SOUND_LEVEL_END, 1.0f);
			loadSound("switch", SOUND_SWITCH, 1.0f);
			loadSound("pick_item", SOUND_PICK_ITEM, 0.75f);
			loadSound("pick_ammo", SOUND_PICK_AMMO, 0.75f);
			loadSound("pick_weapon", SOUND_PICK_WEAPON, 1.0f);
			loadSound("shoot_eat", SOUND_SHOOT_EAT, 1.0f);
			loadSound("deth_hero", SOUND_DETH_HERO, 0.5f);
			loadSound("shoot_hand", SOUND_SHOOT_HAND, 0.5f);
			loadSound("shoot_dblshtg", SOUND_SHOOT_DBLSHTG, 0.5f);
			loadSound("shoot_saw", SOUND_SHOOT_SAW, 0.5f);
		}
	}

	private static void loadSound(String name, int idx, float volume)
	{
		try
		{
			AssetFileDescriptor afd = assetManager.openFd("sounds/" + name + ".ogg");
			int soundId = soundPool.load(afd, 1);
			afd.close();

			soundIds[idx] = soundId;
			soundVolumes[idx] = volume;
		}
		catch (IOException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			soundIds[idx] = -1;
		}
	}

	private static void updateVolume()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
		soundEnabled = sp.getBoolean("EnableSound", false);

		musicVolume = (float)sp.getInt("MusicVolume", 10) / 10.0f;		// 0 .. 1.0
		effectsVolume = (float)sp.getInt("EffectsVolume", 5) / 10.0f;	// 0 .. 1.0

		/*
		 * Actually this code is not required. Leaved here for educational purposes
		 *
		AudioManager mgr = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);
		int streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		int streamMaxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if (streamMaxVolume <= 0) {
			streamMaxVolume = 1;	// just for case
		}

		float volume = (float)streamVolume / (float)streamMaxVolume;

		if (volume > 1.0f) {
			volume = 1.0f;		// just for case
		}

		musicVolume *= volume;
		effectsVolume *= volume;
		*/
	}

	private static void play(boolean wasPlaying)
	{
		mediaPlayer.reset();
		musicLoaded = false;

		try
		{
			AssetFileDescriptor afd = assetManager.openFd("music/" + current.list[current.idx]);

			try {
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepare();
				musicLoaded = true;
			} catch (IllegalStateException ex) {
				Log.e(Common.LOG_KEY, "Exception", ex);
			} catch (IOException ex) {
				Log.e(Common.LOG_KEY, "Exception", ex);
			}

			afd.close();
		}
		catch (IOException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
		}

		if (pauseTask != null)
		{
			pauseTask.cancel();
			pauseTimer.purge();
			pauseTask = null;
		}
		else if (musicLoaded && wasPlaying)		// don't check musicVolume here, because music was playing (so it is ok with volume)
		{
			mediaPlayer.setVolume(musicVolume, musicVolume);
			mediaPlayer.start();
		}
	}

	public static void playSound(int idx)
	{
		playSound(idx, 1.0f);
	}

	public static void playSound(int idx, float volume)
	{
		if ((soundPool != null) && (soundIds[idx] >= 0) && soundEnabled && (effectsVolume > 0.01f) && (volume > 0.01f))
		{
			float actVolume = soundVolumes[idx] * effectsVolume * volume;
			soundPool.play(soundIds[idx], actVolume, actVolume, 0, 0, 1.0f);
		}
	}

	public static void ensurePlaylist()
	{
		if (current == null) {
			setPlaylist(SoundManager.LIST_MAIN);
		}
	}

	// SoundManager.init should be called in every activity constructor
	public static void setPlaylist(PlayList playlist)
	{
		if (current != playlist)
		{
			current = playlist;

			if (mediaPlayer != null) {
				play(mediaPlayer.isPlaying());
			}
		}
	}

	public static void onStart()
	{
		updateVolume();

		if (mediaPlayer != null)
		{
			if (pauseTask != null)
			{
				pauseTask.cancel();
				pauseTimer.purge();
				pauseTask = null;
			}

			if (soundEnabled)
			{
				if (musicLoaded && (musicVolume > 0.01f))
				{
					mediaPlayer.setVolume(musicVolume, musicVolume);
					mediaPlayer.start();	// pause, stop, reset, release
				}
			}
			else
			{
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
			}
		}
	}

	public static void onPause()
	{
		onPause(false);
	}

	public static synchronized void onPause(boolean instant)
	{
		if (mediaPlayer != null && mediaPlayer.isPlaying())
		{
			if (instant)
			{
				if (pauseTask != null)
				{
					pauseTask.cancel();
					pauseTimer.purge();
					pauseTask = null;
				}

				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
			}
			else if (pauseTask == null)
			{
				pauseTask = new PauseTask();
				pauseTimer.schedule(pauseTask, 2000);
			}
		}
	}
}
