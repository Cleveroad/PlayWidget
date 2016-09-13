package com.cleveroad.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Simple observer for displaying and hiding empty view.
 */
final class EmptyViewObserver extends RecyclerView.AdapterDataObserver {

	private WeakReference<View> viewWeakReference;
    private WeakReference<RecyclerView> recyclerViewWeakReference;

	public EmptyViewObserver(@NonNull View view) {
		viewWeakReference = new WeakReference<>(view);
	}

	/**
	 * Bind observer to recycler view's adapter. This method must be called after setting adapter to recycler view.
	 * @param recyclerView instance of recycler view
	 */
	public void bind(@NonNull RecyclerView recyclerView) {
		unbind();
		this.recyclerViewWeakReference = new WeakReference<>(recyclerView);
		recyclerView.getAdapter().registerAdapterDataObserver(this);
	}

	public void unbind() {
		if (recyclerViewWeakReference == null)
			return;
		RecyclerView recyclerView = recyclerViewWeakReference.get();
		if (recyclerView != null) {
			recyclerView.getAdapter().unregisterAdapterDataObserver(this);
			recyclerViewWeakReference.clear();
		}
	}

	@Override
	public void onChanged() {
		super.onChanged();
		somethingChanged();
	}

	@Override
	public void onItemRangeChanged(int positionStart, int itemCount) {
		super.onItemRangeChanged(positionStart, itemCount);
		somethingChanged();
	}

	@Override
	public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
		super.onItemRangeChanged(positionStart, itemCount, payload);
		somethingChanged();
	}

	@Override
	public void onItemRangeInserted(int positionStart, int itemCount) {
		super.onItemRangeInserted(positionStart, itemCount);
		somethingChanged();
	}

	@Override
	public void onItemRangeRemoved(int positionStart, int itemCount) {
		super.onItemRangeRemoved(positionStart, itemCount);
		somethingChanged();
	}

	private void somethingChanged() {
		View view = viewWeakReference.get();
		RecyclerView recyclerView = recyclerViewWeakReference.get();
		if (view != null && recyclerView != null) {
			if (recyclerView.getAdapter().getItemCount() == 0) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}
		}
	}
}