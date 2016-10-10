package bu.ss;


import org.json.JSONObject;

/**
 * Created by vp
 * on 14.04.16.
 */
public abstract class SyncEvent implements Bus.Event {

    private SRespListener listener;

    public SyncEvent(SRespListener listener) {
        this.listener = listener;
    }

    public SRespListener getListener() {
        return listener;
    }

    public interface SRespListener {
        void onResponse(JSONObject data);
        void onError(Exception e);
    }
}