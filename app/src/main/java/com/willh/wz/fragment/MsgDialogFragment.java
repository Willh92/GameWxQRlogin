package com.willh.wz.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.willh.wz.R;
import com.willh.wz.util.DimenUtil;

public class MsgDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    private View contextView;
    private TextView title;
    private TextView msg;
    private Button left;
    private Button right;
    private Dialog dialog;
    private DialogInterface.OnCancelListener listener;
    private DialogInterface.OnDismissListener onDismissListener;
    private ButtonClickListener leftButtonClickListener;
    private ButtonClickListener rightButtonClickListener;
    private CharSequence leftText;
    private CharSequence rightText;
    private CharSequence titleText;
    private CharSequence msgText;
    private boolean leftGone;
    private boolean rightGone;
    private int mRotation = 0;

    private int dialogStyle = R.style.dialog_transparent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (dialog == null) {
            contextView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_msg_common, null);
            title = contextView.findViewById(R.id.tv_title);
            left = contextView.findViewById(R.id.btn_left);
            left.setVisibility(leftGone ? View.GONE : View.VISIBLE);
            left.setOnClickListener(this);
            right = contextView.findViewById(R.id.btn_right);
            right.setVisibility(rightGone ? View.GONE : View.VISIBLE);
            right.setOnClickListener(this);
            msg = contextView.findViewById(R.id.tv_content);
            dialog = new Dialog(getActivity(), dialogStyle);
            dialog.setContentView(contextView);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams lp = window.getAttributes();
            lp.width = (int) (DimenUtil.getScreenWidth(getActivity()) * 0.75);
            window.setAttributes(lp);
        }

        if (!TextUtils.isEmpty(leftText)) {
            left.setText(leftText);
        }
        if (!TextUtils.isEmpty(rightText)) {
            right.setText(rightText);
        }
        if (!TextUtils.isEmpty(titleText)) {
            title.setText(titleText);
            title.setVisibility(View.VISIBLE);
        } else {
            title.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(msgText)) {
            msg.setText(msgText);
        }

        setRotation(mRotation);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onCancel(dialog);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    public void setRotation(int rotation) {
        if (rotation < 0)
            return;
        mRotation = rotation;
        if (dialog != null && contextView != null) {
            contextView.measure(View.MeasureSpec.makeMeasureSpec((int) (DimenUtil.getScreenWidth(dialog.getContext()) * 0.75), View.MeasureSpec.EXACTLY)
                    , View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int w = contextView.getMeasuredWidth();
            int h = contextView.getMeasuredHeight();
            contextView.setRotation(mRotation);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
            if (mRotation == 90 || mRotation == 270) {
                window.setLayout(h, w);
                contextView.getLayoutParams().height = h;
                contextView.getLayoutParams().width = w;
                contextView.setLayoutParams(contextView.getLayoutParams());
                contextView.setTranslationX((h - w) / 2);
                contextView.setTranslationY((w - h) / 2);
            } else {
                window.setLayout(w, h);
                contextView.getLayoutParams().height = h;
                contextView.getLayoutParams().width = w;
                contextView.setLayoutParams(contextView.getLayoutParams());
                contextView.setTranslationX(0);
                contextView.setTranslationY(0);
            }
        }
    }

    public MsgDialogFragment setLeftText(int resId) {
        this.leftText = getString(resId);
        return this;
    }

    public MsgDialogFragment setLeftText(CharSequence leftText) {
        this.leftText = leftText;
        return this;
    }

    public MsgDialogFragment setRightText(int resId) {
        this.rightText = getString(resId);
        return this;
    }

    public MsgDialogFragment setRightText(CharSequence rightText) {
        this.rightText = rightText;
        return this;
    }

    public MsgDialogFragment setLeftGone(boolean leftGone) {
        this.leftGone = leftGone;
        return this;
    }

    public MsgDialogFragment setRightGone(boolean rightGone) {
        this.rightGone = rightGone;
        return this;
    }

    public MsgDialogFragment setTitleText(int resId) {
        this.titleText = getString(resId);
        return this;
    }

    public MsgDialogFragment setTitleText(CharSequence titleText) {
        this.titleText = titleText;
        return this;
    }

    public MsgDialogFragment setMsgText(int resId) {
        this.msgText = getString(resId);
        return this;
    }

    public MsgDialogFragment setMsgText(CharSequence msgText) {
        this.msgText = msgText;
        return this;
    }

    public MsgDialogFragment setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.listener = listener;
        return this;
    }

    public View getContextView() {
        return contextView;
    }

    public MsgDialogFragment setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    public MsgDialogFragment setLeftButtonClickListener(ButtonClickListener leftButtonClickListener) {
        this.leftButtonClickListener = leftButtonClickListener;
        return this;
    }

    public MsgDialogFragment setRightButtonClickListener(ButtonClickListener rightButtonClickListener) {
        this.rightButtonClickListener = rightButtonClickListener;
        return this;
    }

    public interface ButtonClickListener {
        void onButtonClick(MsgDialogFragment msgDialogFragment);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_left) {
            if (leftButtonClickListener != null) {
                leftButtonClickListener.onButtonClick(this);
            } else {
                dismissAllowingStateLoss();
            }
        } else if (i == R.id.btn_right) {
            if (rightButtonClickListener != null) {
                rightButtonClickListener.onButtonClick(this);
            } else {
                dismissAllowingStateLoss();
            }
        }
    }

}
