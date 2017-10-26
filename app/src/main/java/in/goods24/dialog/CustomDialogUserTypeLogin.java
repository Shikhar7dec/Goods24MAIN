package in.goods24.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import in.goods24.R;

/**
 * Created by Shikhar on 10/20/2017.
 */

public class CustomDialogUserTypeLogin extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.radio_user_type_login,null);
        builder.setTitle("Select User Type");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(v);
        return builder.create();
    }
    @Override
    public void onCancel(DialogInterface dlg){
        super.onCancel(dlg);
    }

}
