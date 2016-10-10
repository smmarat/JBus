package bu.ss;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 29.08.14
 * Time: 12:52
 */
public class Bus {
    private ConcurrentMap<String, CopyOnWriteArrayList<Subscriber>> subscribers = new ConcurrentHashMap<String, CopyOnWriteArrayList<Subscriber>>();
    public static final String TAG = "BUS -> ";
    private static Bus instance;
    private static final int MAX_TRY_COUNT = 50;
    private static final int TRY_TIMEOUNT_MLS = 100;
    private ExecutorService pool = Executors.newCachedThreadPool();

    private void log(String msg) {
        System.out.println(TAG + msg);
    }

    private Bus() {
    }

    private void dispatch(final Event evt, final int count) {
        try {
            final String key = evt.getClass().getSimpleName();
            List<Subscriber> subs = subscribers.get(key);
            if (subs == null) {
//                Log.v(TAG, "Not registered subscribers for event "+key+". Retry...");
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (count == MAX_TRY_COUNT) {
                            log("Not registered subscribers for event " + key + " after retry " + count + ". Event ignored");
                            post(new NotDeliveredEvent(evt));
                        } else {
                            try {
                                Thread.sleep(TRY_TIMEOUNT_MLS);
                                dispatch(evt, count + 1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else {
//                StringBuilder sb = new StringBuilder();
//                for (Subscriber s : subs) sb.append(s.getClass().getSimpleName()).append(" ");
//                Log.v(TAG, "Found subscribers for event " + key + ": " + sb);
                for (final Subscriber s : subs) {
                    try {
                        log("Event " + key + " delivered to " + s.getClass().getSimpleName() + " successfully");
                        s.onEvent(evt);
//                        Log.v(TAG, "Event " + key + " processed into " + s.getClass().getSimpleName() + " successfully");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bus getInstance() {
        if (instance == null) instance = new Bus();
        return instance;
    }

    public boolean register(Subscriber s, String evt) {
        if (isRegistered(s, evt)) {
            log("Subscriber " + s.getClass().getSimpleName() + " already subscribed on event " + evt);
            return false;
        }
        CopyOnWriteArrayList<Subscriber> subs = subscribers.get(evt);
        if (subs == null) subs = new CopyOnWriteArrayList<Subscriber>();
        subs.add(s);
        subscribers.put(evt, subs);
        log("Subscriber " + s.getClass().getSimpleName() + " subscribed on event " + evt + " successfully");
        return true;
    }

    public boolean unregister(Subscriber s, String evt) {
        if (!isRegistered(s, evt)) {
            log("Subscriber " + s.getClass().getSimpleName() + " already not subscribed on event " + evt);
            return false;
        }
        CopyOnWriteArrayList<Subscriber> subs = subscribers.get(evt);
        subs.remove(s);
        if (subs.size() == 0) {
            subscribers.remove(evt);
        } else {
            subscribers.put(evt, subs);
        }
        log("Subscriber " + s.getClass().getSimpleName() + " unregistered on event " + evt + " successfully");
        return true;
    }

    public boolean unregister(Subscriber s) {
        for (String key : subscribers.keySet()) {
            CopyOnWriteArrayList<Subscriber> subs = subscribers.get(key);
            if (!subs.contains(s)) continue;
            subs.remove(s);
            if (subs.size() == 0) {
                subscribers.remove(key);
            } else {
                subscribers.put(key, subs);
            }
        }
        log("Subscriber " + s.getClass().getSimpleName() + " unregistered on all events successfully");
        return true;
    }

    public boolean isRegistered(Subscriber s, String evt) {
        List<Subscriber> subs = subscribers.get(evt);
        return subs != null && subs.contains(s);
    }

    public boolean isRegistered(String sName) {
        for (String key : subscribers.keySet()) {
            List<Subscriber> subs = subscribers.get(key);
            for (Subscriber ss : subs) {
                if (sName.equals(ss.getClass().getSimpleName())) return true;
            }
        }
        return false;
    }

    public void post(final Event evt) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Bus dispatcher");
                dispatch(evt, 0);
            }
        });
    }

    public void postThisThread(final Event evt) {
        dispatch(evt, 0);
    }

    public void unregisterSubscribersFromEvent(String eventName) {
        for (String key : subscribers.keySet())
            if (key.equalsIgnoreCase(eventName))
                subscribers.remove(key);
    }

    //------------------------------

    public interface Subscriber {
        void onEvent(Event evt);
    }

    public interface Event {
    }

    public class NotDeliveredEvent implements Event {
        private Event event;

        public NotDeliveredEvent(Event event) {
            this.event = event;
        }

        public Event getEvent() {
            return event;
        }
    }
}
