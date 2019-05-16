package com.lyc.mediaroutertest.mediarouteapplication;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.RemotePlaybackClient;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouterSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaRouterSelector = new MediaRouteSelector.Builder().addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK).build();

        // Get the media router service.
        mediaRouter = MediaRouter.getInstance(this);
    }

    // Use this callback to run your MediaRouteSelector to generate the list of available media routes
    @Override
    public void onStart() {
        mediaRouter.addCallback(mediaRouterSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        super.onStart();
    }

    // Remove the selector on stop to tell the media router that it no longer
    // needs to discover routes for your app.
    @Override
    public void onStop() {
        mediaRouter.removeCallback(mediaRouterCallback);
        super.onStop();
    }

    // Variables to hold the currently selected route and its playback client
    private MediaRouter.RouteInfo mRoute;
    private RemotePlaybackClient remotePlaybackClient;

    // Define the Callback object and its methods, save the object in a class variable
    private final MediaRouter.Callback mediaRouterCallback =
            new MediaRouter.Callback() {

                @Override
                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                    Log.d(TAG, "onRouteSelected: route=" + route);

                    if (route.supportsControlCategory(
                            MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                        // Stop local playback (if necessary)
                        // ...

                        // Save the new route
                        mRoute = route;

                        // Attach a new playback client
                        remotePlaybackClient = new RemotePlaybackClient(this, mRoute);

                        // Start remote playback (if necessary)
                        // ...
                    }
                }

                @Override
                public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route, int reason) {
                    Log.d(TAG, "onRouteUnselected: route=" + route);

                    if (route.supportsControlCategory(
                            MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {

                        // Changed route: tear down previous client
                        if (mRoute != null && remotePlaybackClient != null) {
                            remotePlaybackClient.release();
                            remotePlaybackClient = null;
                        }

                        // Save the new route
                        mRoute = route;

                        if (reason != MediaRouter.UNSELECT_REASON_ROUTE_CHANGED) {
                            // Resume local playback  (if necessary)
                            // ...
                        }
                    }
                }
            };
}
