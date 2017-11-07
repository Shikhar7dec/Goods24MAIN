package in.goods24.common;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.home.HomeDistributorActivity;
import in.goods24.home.HomeRunnerActivity;
import in.goods24.home.HomeSMUserActivity;
import in.goods24.home.HomeUserActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class UpdateProfile extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        findViewById(R.id.updateProfButton).setOnClickListener(this);
        findViewById(R.id.resetProfButton).setOnClickListener(this);
        fetchAndSetAllFields();
    }
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.updateProfButton:
                onUpdateProfButton(v);
                break;
            case R.id.resetProfButton:
                Intent i = new Intent(this,UpdateProfile.class);
                startActivity(i);
                break;
        }
    }

    private void onUpdateProfButton(View v) {
        EditText fNameTF = (EditText)findViewById(R.id.updateFNameEditText);
        EditText lNameTF = (EditText)findViewById(R.id.updateLNameEditText);
        EditText eMailTF = (EditText)findViewById(R.id.updateEmailEditText);
        EditText phoneTF = (EditText)findViewById(R.id.updatePhoneEditText);
        EditText add1TF = (EditText)findViewById(R.id.updateAddline1EditText);
        EditText add2TF = (EditText)findViewById(R.id.updateAddline2EditText);
        EditText add3TF = (EditText)findViewById(R.id.updateAddline3EditText);
        EditText cityTF = (EditText)findViewById(R.id.updateCityEditText);
        EditText stateTF = (EditText)findViewById(R.id.updateStateText);
        EditText pincodeTF = (EditText)findViewById(R.id.updatePincodeEditText);
        boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(fNameTF)
                ||ValidationUtil.isEmptyTextField(lNameTF)
                ||ValidationUtil.isEmptyTextField(eMailTF)
                ||ValidationUtil.isEmptyTextField(phoneTF)
                ||ValidationUtil.isEmptyTextField(add1TF)
                ||ValidationUtil.isEmptyTextField(add2TF)
                ||ValidationUtil.isEmptyTextField(add3TF)
                ||ValidationUtil.isEmptyTextField(cityTF)
                ||ValidationUtil.isEmptyTextField(stateTF)
                ||ValidationUtil.isEmptyTextField(pincodeTF);
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String loggedInUserID =sharedPreferences.getString("loggedInUserID","");
        String userType =sharedPreferences.getString("selectedUserTypeID","");
        if(!areAllFieldsEmpty){
            RequestParams rp = new RequestParams();
            rp.add("appId","g24API6");
            rp.add("pwd","API6g24");
            rp.add("userId",loggedInUserID);
            rp.add("uType",userType);
            rp.add("fname",fNameTF.getText().toString());
            rp.add("lname",lNameTF.getText().toString());
            rp.add("email",eMailTF.getText().toString());
            rp.add("mobile_no",phoneTF.getText().toString());
            rp.add("address_line_1",add1TF.getText().toString());
            rp.add("address_line_2",add2TF.getText().toString());
            rp.add("address_line_3",add3TF.getText().toString());
            rp.add("city",cityTF.getText().toString());
            rp.add("state",stateTF.getText().toString());
            rp.add("pincode",pincodeTF.getText().toString());
            String phpName = "updateProfileInfo.php";
            Log.d("REQ","request is>>>>>"+rp);
            RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
            relLayoutProgress.setVisibility(View.VISIBLE);
            makeUpdateProfileRestCall(phpName,rp);

        }
        else{
            showValidationMsg("Please enter all fields");
        }

    }

    private void makeUpdateProfileRestCall(String phpName, RequestParams rp) {
                    {
                HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                        Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                        try{

                            String respMsg = (String)res.get("res");
                            int errCode = (Integer)res.getInt("error_code");
                            showUpdateProfResp(respMsg,errCode);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                            showValidationMsg("Please check your internet and try again");
                            RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                            relLayoutProgress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString,
                                          Throwable throwable) {
                        Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
                        showValidationMsg("Some Error occurred please try again");
                        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                        relLayoutProgress.setVisibility(View.GONE);
                    }
                });
            }
        }

    private void showUpdateProfResp(String respMsg, int errCode) {
        if(0==errCode){
            showValidationMsg(respMsg);
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
        else{
            showValidationMsg(respMsg);
        }
    }

    private void fetchAndSetAllFields() {
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String loggedInUserID =sharedPreferences.getString("loggedInUserID","");
        String userType =sharedPreferences.getString("selectedUserTypeID","");
        RequestParams rp = new RequestParams();
        rp.add("appId","g24API5");
        rp.add("pwd","API5g24");
        rp.add("userId",loggedInUserID);
        rp.add("uType",userType);
        String phpName = "getProfileInfo.php";
        Log.d("REQ","request is>>>>>"+rp);
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
        relLayoutProgress.setVisibility(View.VISIBLE);
        makeFetchDetRestCall(rp,phpName);
    }

    private void makeFetchDetRestCall(RequestParams rp, String phpName) {
        HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                try{
                    int errCode = (Integer)res.getInt("error_code");
                    putProfDetails(res,errCode);
                }
                catch (Exception e){
                    e.printStackTrace();
                    showValidationMsg("Please check your internet and try again");
                    RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                    relLayoutProgress.setVisibility(View.GONE);
                    gotoPreviousActivity();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
            }
        });
    }

    private void showValidationMsg(String message) {

        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }
    private void gotoPreviousActivity() {
        Intent intent=null;
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String userType =sharedPreferences.getString("selectedUserTypeID","");
        if("2".equalsIgnoreCase(userType)){
            intent = new Intent(getApplicationContext(),HomeSMUserActivity.class);
        }
        else if("3".equalsIgnoreCase(userType)){
            intent = new Intent(getApplicationContext(),HomeRunnerActivity.class);
        }
        else if("4".equalsIgnoreCase(userType)){
            intent = new Intent(getApplicationContext(), HomeDistributorActivity.class);
        }
        else if("5".equalsIgnoreCase(userType)){
            intent = new Intent(getApplicationContext(),HomeUserActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    private void putProfDetails(JSONObject res, int errCode) {
        try {
            if(0==errCode){
                String fName = (String)res.getString("first_name");
                String lName =(String)res.getString("last_name");
                String eMail = (String)res.getString("email_id");
                String phone = (String)res.getString("mobile_no");
                String add1 = (String)res.getString("address_line_1");
                String add2 = (String)res.getString("address_line_2");
                String add3 = (String)res.getString("address_line_3");
                String city = (String)res.getString("city");
                String state = (String)res.getString("state");
                String pincode = (String)res.getString("pincode");

                EditText fNameEditText = (EditText)findViewById(R.id.updateFNameEditText);
                EditText lNameEditText = (EditText)findViewById(R.id.updateLNameEditText);
                EditText eMailEditText = (EditText)findViewById(R.id.updateEmailEditText);
                EditText phoneEditText = (EditText)findViewById(R.id.updatePhoneEditText);
                EditText add1EditText = (EditText)findViewById(R.id.updateAddline1EditText);
                EditText add2EditText = (EditText)findViewById(R.id.updateAddline2EditText);
                EditText add3EditText = (EditText)findViewById(R.id.updateAddline3EditText);
                EditText cityEditText = (EditText)findViewById(R.id.updateCityEditText);
                EditText stateEditText = (EditText)findViewById(R.id.updateStateText);
                EditText pincodeEditText = (EditText)findViewById(R.id.updatePincodeEditText);


                Log.d("VAL","Values are>>>"+fName+">>>"+eMail+">>>"+add1);
                fNameEditText.setText(fName);
                lNameEditText.setText(lName);
                eMailEditText.setText(eMail);
                phoneEditText.setText(phone);
                add1EditText.setText(add1);
                add2EditText.setText(add2);
                add3EditText.setText(add3);
                cityEditText.setText(city);
                stateEditText.setText(state);
                pincodeEditText.setText(pincode);
                RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                relLayoutProgress.setVisibility(View.GONE);
            }
            else{
                throw new Exception("Please try again later");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            showValidationMsg("Please try again later");
            gotoPreviousActivity();
        }
    }
}

