package com.willh.wz.fragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseDialogFragment extends DialogFragment {

    private AtomicBoolean isShow = new AtomicBoolean(false);
    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public void show(FragmentManager manager, String tag) {
        if (!isAdded() && isShow.compareAndSet(false, true)) {
            super.show(manager, tag);
        }
    }

    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        if (!isAdded() && isShow.compareAndSet(false, true)) {
            try {
                Field mDismissedField = DialogFragment.class.getDeclaredField("mDismissed");
                mDismissedField.setAccessible(true);
                mDismissedField.set(this, false);

                Field mShownByMeField = DialogFragment.class.getDeclaredField("mShownByMe");
                mShownByMeField.setAccessible(true);
                mShownByMeField.set(this, true);
            } catch (Exception e) {
            }
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        if (getFragmentManager() == null)
            return;
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShow.set(false);
        super.onDismiss(dialog);
        if (this.mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    public BaseDialogFragment setOnDismissListener(DialogInterface.OnDismissListener mOnDismissListener) {
        this.mOnDismissListener = mOnDismissListener;
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mOnDismissListener = null;
    }

}
