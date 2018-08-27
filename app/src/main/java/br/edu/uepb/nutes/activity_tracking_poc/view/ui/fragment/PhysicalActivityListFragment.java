package br.edu.uepb.nutes.activity_tracking_poc.view.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;

import br.edu.uepb.nutes.activity_tracking_poc.R;
import br.edu.uepb.nutes.activity_tracking_poc.data.model.Activity;
import br.edu.uepb.nutes.activity_tracking_poc.data.model.ActivityList;
import br.edu.uepb.nutes.activity_tracking_poc.data.model.User;
import br.edu.uepb.nutes.activity_tracking_poc.data.model.UserAccess;
import br.edu.uepb.nutes.activity_tracking_poc.data.repository.local.pref.AppPreferencesHelper;
import br.edu.uepb.nutes.activity_tracking_poc.data.repository.remote.fitbit.FitBitNetRepository;
import br.edu.uepb.nutes.activity_tracking_poc.data.repository.remote.ocariot.OcariotNetRepository;
import br.edu.uepb.nutes.activity_tracking_poc.utils.DateUtils;
import br.edu.uepb.nutes.activity_tracking_poc.view.adapter.PhysicalActivityListAdapter;
import br.edu.uepb.nutes.activity_tracking_poc.view.adapter.base.OnRecyclerViewListener;
import br.edu.uepb.nutes.activity_tracking_poc.view.ui.activity.MainActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

/**
 * A fragment representing a list of Items.
 *
 * @author Douglas Rafael <douglas.rafael@nutes.uepb.edu.br>
 * @version 1.0
 * @copyright Copyright (c) 2018, NUTES/UEPB
 */
public class PhysicalActivityListFragment extends Fragment {
    private final String LOG_TAG = "PhysicalActivityList";

    private Disposable disposableFitBit, disposableOcariot;
    private PhysicalActivityListAdapter mAdapter;
    private FitBitNetRepository fitBitRepository;
    private OcariotNetRepository ocariotNetRepository;
    private OnClickActivityListener mListener;
    private UserAccess userAccess;

    /**
     * We need this variable to lock and unlock loading more.
     * We should not charge more when a request has already been made.
     * The load will be activated when the requisition is completed.
     */
    private boolean itShouldLoadMore = true;

    @BindView(R.id.activities_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.data_swiperefresh)
    SwipeRefreshLayout mDataSwipeRefresh;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicalActivityListFragment() {
    }

    public static PhysicalActivityListFragment newInstance() {
        PhysicalActivityListFragment fragment = new PhysicalActivityListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferencesHelper.getInstance(getContext())
                .getUserAccessOcariot().subscribe(result -> {
            userAccess = result;
        }, error -> {

        }).dispose();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physical_activity_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fitBitRepository = FitBitNetRepository.getInstance(getContext());
        ocariotNetRepository = OcariotNetRepository.getInstance(getContext());

        initComponents();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClickActivityListener) {
            mListener = (OnClickActivityListener) context;
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (disposableOcariot != null) disposableOcariot = null;
        if (disposableFitBit != null) disposableFitBit = null;
        if (fitBitRepository != null) fitBitRepository.dispose();

        mListener = null;
    }

    /**
     * Initialize components
     */
    private void initComponents() {
        initRecyclerView();
        initDataSwipeRefresh();
        loadDataFitBit();
        initToolBar();
    }

    private void initToolBar() {
        ActionBar mActionBar = ((MainActivity) getActivity()).getSupportActionBar();
        mActionBar.setTitle(R.string.title_physical_activity);
        mActionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void initRecyclerView() {
        mAdapter = new PhysicalActivityListAdapter(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation()));

        mAdapter.setListener(new OnRecyclerViewListener<Activity>() {
            @Override
            public void onItemClick(Activity item) {
                Log.w(LOG_TAG, "item: " + item.toString());
                if (mListener != null) mListener.onClickActivity(item);
            }

            @Override
            public void onLongItemClick(View v, Activity item) {

            }

            @Override
            public void onMenuContextClick(View v, Activity item) {

            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Initialize SwipeRefresh
     */
    private void initDataSwipeRefresh() {
        mDataSwipeRefresh.setOnRefreshListener(() -> {
            if (itShouldLoadMore) loadDataFitBit();
        });
    }

    private void loadData() {
        loadDataFitBit();
        loadDataOcariot();
    }

    /**
     * Load data in FitBit Server.
     */
    private void loadDataFitBit() {
        String currentDate = DateUtils.getCurrentDatetime(getResources().getString(R.string.date_format1));

        disposableFitBit = fitBitRepository
                .listActivities(currentDate, null, "desc", 0, 10)
                .doOnComplete(() -> loadDataOcariot())
                .subscribe(activityList -> {
                    if (activityList != null) {
                        // TODO enviar para universAAL
                        sendUniverssAAl(activityList);
                    }
                    Log.w(LOG_TAG, "DATA FITBIT: " + Arrays.toString(activityList.getActivities().toArray()));
                }, error -> {
                    loadDataOcariot();
                });
    }

    /**
     * Load data in OCARIoT Server.
     * If there is no internet connection, we can display the local database.
     * Otherwise it displays from the remote server.
     */
    private void loadDataOcariot() {
        Log.w(LOG_TAG, "loadDataOcariot()");
        mAdapter.clearItems();
        mDataSwipeRefresh.setRefreshing(true);

        disposableOcariot = ocariotNetRepository
                .listActivities(userAccess.getSubject())
                .subscribe(activityList -> {
                    if (activityList != null)
                        mAdapter.addItems(activityList);

                    Log.w(LOG_TAG, "DATA OCARIoT: " + Arrays.toString(activityList.toArray()));
                    mDataSwipeRefresh.setRefreshing(false);
                }, error -> {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    mDataSwipeRefresh.setRefreshing(false);
                });
    }

    private void sendUniverssAAl(ActivityList activityList) {
        Log.w(LOG_TAG, "sendUniverssAAl()");
    }

}