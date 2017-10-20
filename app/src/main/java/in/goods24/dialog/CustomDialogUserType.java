package in.goods24.dialog;

import android.app.AlertDialog;
import android.app.Dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import in.goods24.R;

/**
 * Created by Shikhar on 10/20/2017.
 */

public class CustomDialogUserType extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.radio_user_type,null);
        builder.setTitle("Select User Type");
        builder.setView(v);
        return builder.create();
    }

}
