package com.willh.wz.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.willh.wz.R;

public class ProgressDialogFragment extends BaseDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.dialog_transparent);
        dialog.setContentView(R.layout.dialog_progressbar);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        Window window = dialog.getWindow();
        window.addFlags(LayoutParams.FLAG_FULLSCREEN);
        // window.setWindowAnimations(R.style.popup_buttom);
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        LayoutParams lp = window.getAttributes();
        // lp.width = (int)
        // (ApplicationController.getInstance().getScreenWidth() * 0.8);
        // lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.width = lp.height = getActivity().getResources()
                .getDimensionPixelOffset(R.dimen.progress_dialog_size);
        window.setAttributes(lp);
        window.setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }

}
