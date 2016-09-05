package com.cleveroad.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base adapter for recycler view
 */
abstract class BaseRecyclerViewAdapter<TData, TViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<TViewHolder> implements BaseFilter.FilterableAdapter<TData> {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<TData> data;
    private BaseFilter<TData> filter;

    public BaseRecyclerViewAdapter(@NonNull final Context context) {
        this.context = context.getApplicationContext();
	    this.inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public BaseRecyclerViewAdapter(@NonNull final Context context, @NonNull List<TData> data) {
        this.context = context.getApplicationContext();
	    this.inflater = LayoutInflater.from(context);
        this.data = new ArrayList<>(data);
    }

    @Override
    public void withFilter(@Nullable BaseFilter<TData> filter) {
        if (this.filter != null)
            unregisterAdapterDataObserver(this.filter.getAdapterDataObserver());
        this.filter = filter;
        if (this.filter != null) {
            this.filter.init(this);
            registerAdapterDataObserver(this.filter.getAdapterDataObserver());
        }
    }

    protected Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        if (filter != null && filter.isFiltered())
            return filter.getCount();
        return data.size();
    }

    public TData getItem(final int position) throws ArrayIndexOutOfBoundsException {
        if (filter != null && filter.isFiltered())
            return filter.getItem(position);
        return data.get(position);
    }

    @Override
    public boolean isFiltered() {
        return filter != null && filter.isFiltered();
    }

    @Override
    public Spannable highlightFilteredSubstring(String text) {
        return isFiltered() ? filter.highlightFilteredSubstring(text) : new SpannableString(text);
    }

    @Override
    public TData getNonFilteredItem(int position) {
        return data.get(position);
    }

    @Override
    public int getNonFilteredCount() {
        return data.size();
    }

    public boolean add(TData object) {
        return data.add(object);
    }

    public boolean remove(TData object) {
        return data.remove(object);
    }

    public TData remove(int position) {
        return data.remove(position);
    }

    public void clear() {
        data.clear();
    }

    public boolean addAll(@NonNull Collection<? extends TData> collection) {
        return data.addAll(collection);
    }

    public BaseFilter<TData> getFilter() {
        return filter;
    }

    public List<TData> getSnapshot() {
        return new ArrayList<>(data);
    }

	protected LayoutInflater getInflater() {
		return inflater;
	}
}
