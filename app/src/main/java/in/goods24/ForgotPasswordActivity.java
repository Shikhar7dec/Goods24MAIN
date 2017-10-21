package in.goods24;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        findViewById(R.id.forgotPwdButton).setOnClickListener(this);

    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.forgotPwdButton:
                doForgotPassword(view);
                break;
        }

    }

    private void doForgotPassword(View view) {
        EditText eMail = (EditText)findViewById(R.id.forgotPwdEmailText);
        EditText phone = (EditText)findViewById(R.id.forgotPwdPhoneText);
        boolean areFieldsEmpty = ValidationUtil.isEmptyTextField(eMail)
                                ||ValidationUtil.isEmptyTextField(phone);
        if(!areFieldsEmpty){
            if(ValidationUtil.isValidEmail(eMail)){
                if(ValidationUtil.isValidPhoneNumber(phone)){
                    SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                    String loginUserType=sharedPreferences.getString("selectedUserTypeID","");
                    RequestParams rp = new RequestParams();
                    rp.add("appId","g24API4");
                    rp.add("pwd","API4g24");
                    rp.add("email",eMail.getText().toString());
                    rp.add("mobile_number",phone.getText().toString());
                    rp.add("uType",loginUserType);
                    String phpName="resetPwd.php";
                    Log.d("REQ","request is>>>>>"+rp);
                    makeFPRestCall(view,rp,phpName);
                }
                else
                    showValidationMsg("Please enter valid a Phone Number");
            }
            else
                showValidationMsg("Please enter valid a e-Mail");
        }
        else
            showValidationMsg("Please enter all fields");

    }

    private void makeFPRestCall(final View view, RequestParams rp, String phpName) {
        {
            HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                    try{

                        String respMsg = (String)res.get("res");
                        int errCode = (Integer)res.getInt("error_code");
                        showForgotPwdResp(view,respMsg,errCode);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        showValidationMsg("Please check your internet and try again");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString,
                                      Throwable throwable) {
                    Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
                }
            });
        }


    }

    private void showForgotPwdResp(View view, String respMsg,int errCode) {
        if(0==errCode){
            showValidationMsg("Password reset Successful");
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }

    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }
}

