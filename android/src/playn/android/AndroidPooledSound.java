package playn.android;

import android.media.AudioManager;
import android.media.SoundPool;
import playn.core.PlayN;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static playn.core.PlayN.log;

/**
 * @author shmyga.z@gmail.com <shmyga>
 *         Date: 27.03.12
 */
public class AndroidPooledSound extends AndroidSound {

  private static SoundPool pool;
  private static Map<Integer, AndroidPooledSound> soundsMap;

  static {
    soundsMap = new HashMap<Integer, AndroidPooledSound>();
    pool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
    pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
      @Override
      public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        AndroidPooledSound sound = soundsMap.get(sampleId);
        if (sound != null) {
          if (status == 0) {
            sound.onLoadComplete();
          } else {
            sound.onLoadError(new RuntimeException("AndroidPooledSound load failed with status " + status));
          }
        } else {
          PlayN.log().error("AndroidPooledSound with id=" + sampleId + " not found");
        }
      }
    });
  }

  private File cachedFile;
  private int soundId;
  private int streamId;
  private float volume;
  private boolean looping;

  public AndroidPooledSound(String path, InputStream in, String extension) throws IOException {
    cachedFile = new File(AndroidPlatform.instance.activity.getFilesDir(), "sound-" + Util.md5(path)
            + extension);
    try {
      FileOutputStream out = new FileOutputStream(cachedFile);
      Util.copyStream(in, out);
    } catch (IOException e) {
      log().error("IOException ");
      onLoadError(e);
    }

    volume = 1.0f;
    looping = false;
    streamId = -1;
    soundId = pool.load(cachedFile.getAbsolutePath(), 0);
    soundsMap.put(soundId, this);
  }

  @Override
  public boolean play() {
    streamId = pool.play(soundId, volume, volume, 1, looping ? -1 : 0, 1.0f);
    return true;
  }

  @Override
  public void stop() {
    if (streamId > -1) {
      pool.stop(streamId);
      streamId = -1;
    }
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
    if (streamId > -1) {
      pool.setLoop(streamId, looping ? -1 : 0);
    }
  }

  @Override
  public void setVolume(float volume) {
    this.volume = volume;
    if (streamId > -1) pool.setVolume(streamId, volume, volume);
  }

  @Override
  public boolean isPlaying() {
    return streamId > -1;
  }

  @Override
  void onPause() {
    pool.pause(streamId);
  }

  @Override
  void onResume() {
    pool.resume(streamId);
  }

  @Override
  void onDestroy() {
    cachedFile.delete();
    pool.unload(soundId);
  }

  @Override
  public void finalize() {
    onDestroy();
  }
}
