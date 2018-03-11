package br.brunodea.nevertoolate.frag;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;

import br.brunodea.nevertoolate.NeverTooLateApp;
import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.NotificationDaoAsyncTask;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.frag.list.NotificationsAdapter;
import br.brunodea.nevertoolate.frag.list.NotificationsListViewModel;
import br.brunodea.nevertoolate.frag.list.NotificationsViewHolder;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.NotificationUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    private static final int LOADER_ID = 20;
    private static final int GEOFENCE_DEFAULT_RADIUS_IN_METERS = 100;
    public static final int NOTIFICATION_PLACE_PICKER_REQUEST = 4321;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 123;

    private static final String ANALYTICS_EVENT_CREATE = "create_notification";
    private static final String ANALYTICS_EVENT_DELETE = "delete_notification";
    private static final String ANALYTICS_EVENT_PERMISSION = "permission_asked";

    @BindView(R.id.cl_notification_root) ConstraintLayout mCLRoot;
    @BindView(R.id.rv_notifications) RecyclerView mRecyclerView;
    @BindView(R.id.tv_notifications_error_message) TextView mTVErrorMessage;

    private NeverTooLateDatabase mDB;
    private NotificationsAdapter mAdapter;
    private GeofencingClient mGeofencingClient;
    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsFragment() {
    }

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_list, container, false);
        // Just to connect to google
        NeverTooLateApp.googleClient(getActivity(), connectionResult -> {
            Snackbar.make(mCLRoot, R.string.google_play_conn_failed, Snackbar.LENGTH_LONG).show();
            Log.e(TAG, "Unable to connect to google: " + connectionResult.getErrorMessage());
        });

        if (mGeofencingClient == null) {
            mGeofencingClient = LocationServices.getGeofencingClient(getContext());
        }

        ButterKnife.bind(this, view);

        mDB = NeverTooLateDatabase.getInstance(getContext());
        mAdapter = new NotificationsAdapter(new ArrayList<>(), mDB);

        setHasOptionsMenu(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        NotificationsListViewModel notificationsListViewModel = ViewModelProviders.of(this)
                .get(NotificationsListViewModel.class);
        notificationsListViewModel.getModelList().observe(this,
            notifications -> {
                mAdapter.setNotifications(notifications);
                if (notifications == null || notifications.isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    mTVErrorMessage.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTVErrorMessage.setVisibility(View.GONE);
                }
            }
        );

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                NotificationsViewHolder vh = (NotificationsViewHolder)  viewHolder;
                NotificationModel nm = vh.notificationModel();
                NotificationModel.Type notification_type = nm.type();
                NeverTooLateUtil.showRemoveNotificationDialog(getContext(), nm.info(), new NeverTooLateUtil.OnRemoveDialogListener() {
                    @Override
                    public void onPositive() {
                        if (nm.type() == NotificationModel.Type.Time) {
                            NotificationUtil.cancelNotificationSchedule(getContext(), nm.id());
                        } else if (nm.type() == NotificationModel.Type.GeoFence) {
                            mGeofencingClient.removeGeofences(
                                    NotificationUtil.pendingIntentForNotification(getContext(), nm.id()));
                        }
                        new NotificationDaoAsyncTask(mDB.getNotificationDao().findById(nm.id()),
                                mDB, NotificationDaoAsyncTask.Action.DELETE);
                        if (mAnalyticsListener != null) {
                            Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, notification_type.name());
                            Pair p2 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "delete_complete");
                            mAnalyticsListener.onEvent(ANALYTICS_EVENT_DELETE, p1, p2);
                        }
                    }

                    @Override
                    public void onNegative() {
                        // to re-draw stuff
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    public void onFabClick() {
        View dialog_notification_type = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notification_type, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialog_notification_type)
                .setTitle(R.string.dialog_notification_title)
                .setCancelable(true)
                .create();

        LinearLayout daily_notification = dialog_notification_type.findViewById(R.id.ll_daily_notification);
        daily_notification.setOnClickListener(view -> {
            // dismiss the dialog to choose the notification type
            dialog.dismiss();
            addDailyNotification();
        });
        ConstraintLayout geofence_notification = dialog_notification_type.findViewById(R.id.cl_dialog_geofance_notification);
        TextView permission_explanation = geofence_notification.findViewById(R.id.tv_geofence_permission_explanation);
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            permission_explanation.setVisibility(View.VISIBLE);
        } else {
            permission_explanation.setVisibility(View.GONE);
        }
        geofence_notification.setOnClickListener(view -> {
            dialog.dismiss();
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                addGeofenceNotification();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
            }
        });

        dialog.show();
    }

    private void addGeofenceNotification() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            getActivity().startActivityForResult(builder.build(getActivity()), NOTIFICATION_PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Snackbar.make(mCLRoot, R.string.google_play_conn_failed, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void onPlacePickerResult(int result_code, Intent data) {
        Log.d(TAG, "On Place Picker Result");
        if (result_code == RESULT_OK) {
            Log.d(TAG, "On Place Picker Result: result OK!");
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Place place = PlacePicker.getPlace(getContext(), data);
                NotificationModel nm = new NotificationModel(place.getName().toString(),
                        NotificationModel.Type.GeoFence.ordinal());
                Notification notification = new Notification(Notification.NotificationType.GEOFENCE,
                        nm.info(), 0);
                NotificationDaoAsyncTask ndat =
                        new NotificationDaoAsyncTask(notification, mDB, NotificationDaoAsyncTask.Action.INSERT);
                ndat.setInsertListener(new_id -> {
                    notification.id = new_id;
                    nm.setID(new_id);
                    Geofence geofence = new Geofence.Builder()
                            .setRequestId(String.valueOf(new_id))
                            .setCircularRegion(
                                    place.getLatLng().latitude,
                                    place.getLatLng().longitude,
                                    GEOFENCE_DEFAULT_RADIUS_IN_METERS)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                            .build();

                    GeofencingRequest req = new GeofencingRequest.Builder()
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .addGeofence(geofence).build();

                    mGeofencingClient.addGeofences(req,
                            NotificationUtil.pendingIntentForNotification(getContext(), new_id))
                            .addOnSuccessListener(aVoid -> {
                                mAdapter.notifyDataSetChanged();
                                Snackbar.make(mCLRoot,
                                        getString(R.string.location_notification_success),
                                        Snackbar.LENGTH_LONG).show();
                                if (mAnalyticsListener != null) {
                                    Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "success_geofence");
                                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_CREATE, p1);
                                }
                            })
                            .addOnFailureListener(e -> {
                                new NotificationDaoAsyncTask(notification, mDB, NotificationDaoAsyncTask.Action.DELETE)
                                        .execute();
                                mAdapter.notifyDataSetChanged();
                                Snackbar.make(mCLRoot,
                                        getString(R.string.location_notification_failed),
                                        Snackbar.LENGTH_LONG).show();
                                String err = e.toString();
                                if (err.contains(String.valueOf(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE))) {
                                    NeverTooLateUtil.displayWarningDialog(getContext(), R.string.geofence_error_status_not_available);
                                } else if (err.contains(String.valueOf(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES))) {
                                    NeverTooLateUtil.displayWarningDialog(getContext(), R.string.geofence_error_status_too_many);
                                } else if (err.contains(String.valueOf(GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS))) {
                                    NeverTooLateUtil.displayWarningDialog(getContext(), R.string.geofence_error_status_pending_intents);
                                } else {
                                    NeverTooLateUtil.displayWarningDialog(getContext(), R.string.geofence_error_status_unknown);
                                }
                                Log.d(TAG, e.toString());
                                if (mAnalyticsListener != null) {
                                    Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "failure_geofence");
                                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_CREATE, p1);
                                }
                            });
                });
                ndat.execute();
            }
        } else {
            Log.d(TAG, "On Place Picker Result: result not OK!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mAnalyticsListener != null) {
                        Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "fine_location_given");
                        mAnalyticsListener.onEvent(ANALYTICS_EVENT_PERMISSION, p1);
                    }
                    addGeofenceNotification();
                }
            } break;
            default: {
                if (mAnalyticsListener != null) {
                    Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "fine_location_refused");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_PERMISSION, p1);
                }
            } break;
        }
    }

    private void addDailyNotification() {
        // open time picker dialog
        TimePickerDialog.OnTimeSetListener listener = (timePicker, hour_of_day, minute) -> {
            Log.d(TAG, "Time picked: " + hour_of_day + ":" + minute);
            //schedule notification
            NotificationModel nm = new NotificationModel(
                    getString(R.string.daily_notification_info_text, hour_of_day, minute),
                    0);
            NotificationDaoAsyncTask ndat = new NotificationDaoAsyncTask(
                    new Notification(Notification.NotificationType.TIME, nm.info(), 0),
                    mDB,
                    NotificationDaoAsyncTask.Action.INSERT
            );
            ndat.setInsertListener(new_id -> {
                NotificationUtil.scheduleNotification(getContext(), hour_of_day, minute, new_id);
                Snackbar.make(mCLRoot, getString(R.string.notification_scheduled),
                        Snackbar.LENGTH_LONG).show();
                if (mAnalyticsListener != null) {
                    Pair p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME, "success_daily");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_CREATE, p1);
                }
            });
            ndat.execute();
        };
        Calendar c = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                listener,
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.setCancelable(true);
        timePickerDialog.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setAnalyticsListener(NeverTooLateUtil.AnalyticsListener listener) {
        mAnalyticsListener = listener;
    }
}
