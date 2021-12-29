package com.willh.wz.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.willh.wz.R;
import com.willh.wz.bean.GameInfo;
import com.willh.wz.filter.NameFilter;
import com.willh.wz.menu.MenuAdapter;
import com.willh.wz.util.DimenUtil;

import java.util.Arrays;

public class MenuDialogFragment extends BaseDialogFragment implements AdapterView.OnItemClickListener {

    private EditText mSearchView;
    private TextView mUpdateView;
    private ListView mListView;

    private MenuAdapter mMenuAdapter;
    private MenuClickListener mListener;

    private View mContentView;
    private String mSearchStr;
    private boolean mNeedReset;

    public static MenuDialogFragment getInstance(MenuAdapter adapter, MenuClickListener listener) {
        MenuDialogFragment menu = new MenuDialogFragment();
        menu.setMenuClickListener(listener);
        menu.setMenuAdapter(adapter);
        return menu;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.menu_list, container, false);
            mListView = mContentView.findViewById(R.id.list);
            mUpdateView = mContentView.findViewById(R.id.tv_update);
            mUpdateView.setText(getString(R.string.update_game, mMenuAdapter.getOriginalCount()));
            mUpdateView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onGameUpdateClick();
                }
                dismissAllowingStateLoss();
            });
            mSearchView = mContentView.findViewById(R.id.et_search);
            InputFilter[] filters = Arrays.copyOf(mSearchView.getFilters(), mSearchView.getFilters().length + 1);
            filters[filters.length - 1] = new NameFilter();
            mSearchView.setFilters(filters);
            mSearchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mMenuAdapter != null) {
                        mMenuAdapter.getFilter().filter(s, count -> {
                            if (isVisible()) {
                                setSelection(0);
                            }
                        });
                    }
                }
            });
            mSearchView.setOnEditorActionListener((v, actionId, event) -> true);
            mListView.setAdapter(mMenuAdapter);
            mListView.setOnItemClickListener(this);
        }
        if (mContentView.getParent() != null) {
            ((ViewGroup) mContentView.getParent()).removeView(mContentView);
        }
        return mContentView;
    }

    @Override
    public int getTheme() {
        return R.style.menu_dialog_style;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(true);
        window.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = getActivity().getResources().getDimensionPixelSize(R.dimen.menu_width);
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setSearchStr(mSearchStr);
        mUpdateView.setText(getString(R.string.update_game, mMenuAdapter.getOriginalCount()));
        if (mNeedReset) {
            setSelection(0);
        }
        mNeedReset = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.getAttributes().windowAnimations = R.style.menu_dialog_style;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mSearchView != null) {
            mSearchStr = mSearchView.getText().toString();
        }
    }

    public void setMenuAdapter(MenuAdapter adapter) {
        this.mMenuAdapter = adapter;
    }

    public void setMenuClickListener(MenuClickListener mListener) {
        this.mListener = mListener;
    }

    public void setSearchStr(String searchStr) {
        if (TextUtils.isEmpty(searchStr)) {
            mSearchStr = "";
        } else {
            mSearchStr = searchStr;
        }
        if (mSearchView != null) {
            if (!mSearchStr.equals(mSearchView.getText().toString())) {
                mSearchView.setText(mSearchStr);
                mSearchView.setSelection(mSearchStr.length());
            }
        }
    }

    public void resetView() {
        mNeedReset = true;
        setSearchStr("");
        if (mUpdateView != null) {
            mUpdateView.setText(mUpdateView.getContext().getString(R.string.update_game, mMenuAdapter.getOriginalCount()));
        }
        setSelection(0);
    }

    public void setSelection(int position) {
        if (mListener != null) {
            mListView.post(() -> {
                if (mListView != null) mListView.setSelection(position);
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            if (position >= 0 && position < mMenuAdapter.getCount()) {
                mListener.onGameSelect(mMenuAdapter.getItem(position));
            }
        }
        dismissAllowingStateLoss();
    }

    public interface MenuClickListener {
        void onGameUpdateClick();

        void onGameSelect(GameInfo gameInfo);
    }

}
