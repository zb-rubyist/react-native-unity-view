package no.asmadsen.unity.view;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.unity3d.player.IUnityPlayerLifecycleEvents;

import java.util.Map;
import javax.annotation.Nullable;

public class UnityViewManager extends SimpleViewManager<UnityView>
        implements LifecycleEventListener, View.OnAttachStateChangeListener {
    private static final String REACT_CLASS = "RNUnityView";

    private ReactApplicationContext context;

    UnityViewManager(ReactApplicationContext context) {
        super();
        this.context = context;
        context.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected UnityView createViewInstance(ThemedReactContext reactContext) {
        Log.d("Unity", "[@:UnityViewManager] createViewInstance begin");
        final UnityView view = new UnityView(reactContext);
        view.addOnAttachStateChangeListener(this);

        if (UnityUtils.getPlayer() != null) {
            Log.d("Unity", "[@:UnityViewManager] Use a exist unity player");
            view.setUnityPlayer(UnityUtils.getPlayer());
            UnityUtils.resume();
            UnityUtils.beginUnityPlayer();
        } else {
            Log.d("Unity", "[@:UnityViewManager] Create unity player");
            UnityUtils.createPlayer(context.getCurrentActivity(), null, new UnityUtils.CreateCallback() {
                @Override
                public void onReady() {
                    view.setUnityPlayer(UnityUtils.getPlayer());
                    Log.d("Unity", "[@:UnityViewManager] createViewInstance done");
                }
            });
        }
        return view;
    }

    @Override
    public void onDropViewInstance(UnityView view) {
        Log.d("Unity", "[@:UnityViewManager] onDropViewInstance Begin");
        view.removeOnAttachStateChangeListener(this);
        super.onDropViewInstance(view);
        Log.d("Unity", "[@:UnityViewManager] onDropViewInstance End");
    }

    @Override
    public void onHostResume() {
        Log.d("Unity", "[@:UnityViewManager] onHostResume Begin");
        if (UnityUtils.isUnityReady()) {
            Log.d("Unity", "[@:UnityViewManager] unityplayer resume");
            UnityUtils.getPlayer().resume();
        }
        Log.d("Unity", "[@:UnityViewManager] onHostResume End");
    }

    @Override
    public void onHostPause() {
        Log.d("Unity", "[@:UnityViewManager] onHostPause");
        if (UnityUtils.isUnityReady()) {
            // Don't use UnityUtils.pause()

            Log.d("Unity", "[@:UnityViewManager] unityplayer pause");
            UnityUtils.getPlayer().pause();
        }
    }

    @Override
    public void onHostDestroy() {
        Log.d("Unity", "[@:UnityViewManager] onHostDestroy");
        if (UnityUtils.isUnityReady()) {
            Log.d("Unity", "[@:UnityViewManager] unityplayer quit");
            UnityUtils.getPlayer().quit();
        }
    }

    private void restoreUnityUserState() {
        Log.d("Unity", "[@:UnityViewManager] restoreUnityUserState");
        // restore the unity player state
        if (UnityUtils.isUnityPaused()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (UnityUtils.getPlayer() != null) {

                        Log.d("Unity", "[@:UnityViewManager] unityplayer pause from handler");
                        UnityUtils.getPlayer().pause();
                    }
                }
            }, 300); //TODO: 300 is the right one?
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        Log.d("Unity", "[@:UnityViewManager] onViewAttachedToWindow");
        restoreUnityUserState();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        Log.d("Unity", "[@:UnityViewManager] onViewDetachedFromWindow");

    }
}
