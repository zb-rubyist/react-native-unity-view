package no.asmadsen.unity.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.unity3d.player.IUnityPlayerLifecycleEvents;
import com.unity3d.player.UnityPlayer;
import java.util.concurrent.CopyOnWriteArraySet;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class UnityUtils {
    private static final CopyOnWriteArraySet<UnityEventListener> mUnityEventListeners =
            new CopyOnWriteArraySet<>();
    private static volatile UnityPlayer unityPlayer;
    private static volatile boolean _isUnityReady;
    private static volatile boolean _isUnityPaused;

    public static UnityPlayer getPlayer() {
        if (!_isUnityReady) {
            return null;
        }
        return unityPlayer;
    }

    public static boolean isUnityReady() {
        return _isUnityReady;
    }

    public static boolean isUnityPaused() {
        return _isUnityPaused;
    }

    public static void createPlayer(final Activity activity, IUnityPlayerLifecycleEvents lifecycle, final CreateCallback callback) {
        if (unityPlayer != null) {
            callback.onReady();
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setFormat(PixelFormat.RGBA_8888);
                int flag = activity.getWindow().getAttributes().flags;
                boolean fullScreen = false;
                if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                    fullScreen = true;
                }

                unityPlayer = new UnityPlayer(activity, new IUnityPlayerLifecycleEvents() {
                    @Override
                    public void onUnityPlayerUnloaded() {
                        pause();
                        addUnityViewToBackground();
                        for (UnityEventListener listener : mUnityEventListeners) {
                            try {
                                listener.onUnload();
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onUnityPlayerQuitted() {

                    }
                });

                try {
                    // wait a moument. fix unity cannot start when startup.
                    Thread.sleep(1000);
                } catch (Exception e) {
                }

                // start unity
                Log.d("Unity", "[@:UnityUtils] start unity");
                addUnityViewToBackground();
                unityPlayer.windowFocusChanged(true);
                unityPlayer.requestFocus();
                Log.d("Unity", "[@:UnityUtils] resume unity");
                unityPlayer.resume();

                // restore window layout
                if (!fullScreen) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                _isUnityReady = true;
                callback.onReady();
            }
        });
    }

    public static void postMessage(String gameObject, String methodName, String message) {
        if (!_isUnityReady) {
            return;
        }
        UnityPlayer.UnitySendMessage(gameObject, methodName, message);
    }

    public static void pause() {
        if (unityPlayer != null) {
            Log.d("Unity", "[@:UnityUtils] on native pause 1");
            unityPlayer.pause();
            _isUnityPaused = true;
        }
    }

    public static void resume() {
        if (unityPlayer != null) {
            unityPlayer.resume();
            _isUnityPaused = false;
        }
    }

    public static void tryQuit() {
        if (!_isUnityReady) {
            return;
        }

        UnityPlayer.UnitySendMessage("UnityMessageManager", "onMessage", "try_quit");
    }

    /**
     * Invoke by unity C#
     */
    public static void onUnityMessage(String message) {
        for (UnityEventListener listener : mUnityEventListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
            }
        }
    }

    public static void addUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.add(listener);
    }

    public static void removeUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.remove(listener);
    }

    public static void beginUnityPlayer() {
        Log.d("unity", "[@:UnityUtils] beginUnityPlayer Begin");
        unityPlayer.windowFocusChanged(true);
        unityPlayer.requestFocus();
        Log.d("unity", "[@:UnityUtils] unityPlayer resume");
        unityPlayer.resume();
        Log.d("unity", "[@:UnityUtils] beginUnityPlayer End");
    }

    public static void addUnityViewToBackground() {
        Log.d("unity", "[@:UnityUtils] addUnityViewToBackground Begin");
        if (unityPlayer == null) {
            return;
        }
        if (unityPlayer.getParent() != null) {
            ((ViewGroup) unityPlayer.getParent()).removeView(unityPlayer);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unityPlayer.setZ(-1f);
        }
        final Activity activity = ((Activity) unityPlayer.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        activity.addContentView(unityPlayer, layoutParams);
        Log.d("unity", "[@:UnityUtils] addUnityViewToBackground End");
    }

    public static void addUnityViewToGroup(ViewGroup group) {
        Log.d("unity", "[@:UnityUtils] addUnityViewToGroup Begin");
        if (unityPlayer == null) {
            return;
        }
        if (unityPlayer.getParent() != null) {
            ((ViewGroup) unityPlayer.getParent()).removeView(unityPlayer);
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        group.addView(unityPlayer, 0, layoutParams);
        unityPlayer.windowFocusChanged(true);
        unityPlayer.requestFocus();
        unityPlayer.resume();
        Log.d("unity", "[@:UnityUtils] addUnityViewToGroup End");
    }

    public interface CreateCallback {
        void onReady();
    }
}
