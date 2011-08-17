/**
 * Copyright 2011 The PlayN Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.android;

import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

public class GameLoop implements Runnable {
  private static final int MAX_DELTA = 100;

  private AtomicBoolean running = new AtomicBoolean();
  private AndroidGraphics gfx;
  
  private long timeOffset = System.currentTimeMillis();

  private int updateRate;
  private int accum;
  private int lastTime;  

  private float paintAlpha;

  public GameLoop() {
    gfx = AndroidPlatform.instance.graphics();
  }

  public void start() {
    if (!running.get()) {
      Log.i("playn", "Starting game loop");
      this.updateRate = AndroidPlatform.instance.game.updateRate();
      running.set(true);
    }
  }

  public void pause() {
    Log.i("playn", "Pausing game loop");
    running.set(false);
  }

  public void run() {
    // The thread can be stopped between runs.
    if (!running.get())
      return;

    int now = time();
    float delta = now - lastTime;
    if (delta > MAX_DELTA)
      delta = MAX_DELTA;
    lastTime = now;

    if (updateRate == 0) {
      AndroidPlatform.instance.update(delta);
      accum = 0;
    } else {
      accum += delta;
      while (accum >= updateRate) {
        AndroidPlatform.instance.update(updateRate);
        accum -= updateRate;
      }
    }

    paintAlpha = (updateRate == 0) ? 0 : accum / updateRate;
    paint();
  }

  private int time() {
    // System.nanoTime() would be better here, but it's busted on the HTC EVO
    // 2.3 update. Instead we use an offset from a known time to keep it within
    // int range.
    return (int) (System.currentTimeMillis() - timeOffset);
  }
  
  public boolean running() {
    return running.get();
  }

  protected void paint() {
    gfx.bindFramebuffer();
    AndroidPlatform.instance.game.paint(paintAlpha);  //Run the game's custom layer-painting code
    gfx.updateLayers();  //Actually draw to the screen
  }

}