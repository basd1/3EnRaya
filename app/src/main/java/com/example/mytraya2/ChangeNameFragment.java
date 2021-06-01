package com.example.mytraya2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ChangeNameFragment extends DialogFragment implements DialogInterface.OnClickListener {

    MainActivity mActivity;
    EditText getEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.change_name_fragment, container);
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        String value = getEditText.getText().toString();
        Log.d("Name : ", value);
        dialog.dismiss();
    }
}
