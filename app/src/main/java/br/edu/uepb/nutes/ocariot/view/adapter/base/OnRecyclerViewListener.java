package br.edu.uepb.nutes.ocariot.view.adapter.base;

import android.view.View;

/**
 * Interface to capture event click on itemsList listed by the adapter.
 *
 * @author Copyright (c) 2017, NUTES/UEPB
 */
public interface OnRecyclerViewListener<T> {
    void onItemClick(T item);

    void onLongItemClick(View v, T item);

    void onMenuContextClick(View v, T item);
}
