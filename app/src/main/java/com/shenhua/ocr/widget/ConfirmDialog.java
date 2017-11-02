package com.shenhua.ocr.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shenhua.ocr.R;

/**
 * Created by shenhua on 2017-11-02-0002.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class ConfirmDialog extends BottomSheetDialog {

    public ConfirmDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_confirm_delete);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            window.findViewById(R.id.cancelTv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNegativeListener(null);
                }
            });
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        Window window = getWindow();
        if (window != null) {
            TextView textView = getWindow().findViewById(R.id.dialogTitleTv);
            textView.setText(title);
        }
    }

    public void setMessage(CharSequence title) {
        Window window = getWindow();
        if (window != null) {
            TextView textView = getWindow().findViewById(R.id.dialogMsgTv);
            textView.setText(title);
        }
    }

    public void setNegativeListener(DialogInterface.OnClickListener listener) {
        if (listener != null) {
            listener.onClick(this, 0);
        }
        dismiss();
    }

    public void setPositiveListener(final DialogInterface.OnClickListener listener) {
        Window window = getWindow();
        if (window != null) {
            window.findViewById(R.id.sureTv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(ConfirmDialog.this, 0);
                    }
                    dismiss();
                }
            });
        }
    }
}
