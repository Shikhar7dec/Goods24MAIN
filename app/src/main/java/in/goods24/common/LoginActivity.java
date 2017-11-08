package in.goods24.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.dialog.CustomDialogUserTypeReg;
import in.goods24.home.HomeDistributorActivity;
import in.goods24.home.HomeRunnerActivity;
import in.goods24.home.HomeSMUserActivity;
import in.goods24.home.HomeUserActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

import static in.goods24.util.ConstantsUtil.USERMAP;

public class LoginActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLogin(View view){
        EditText username= (EditText)findViewById(R.id.loginUserText);
        EditText password= (EditText)findViewById(R.id.loginPasswordText);
        boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(username)||
                ValidationUtil.isEmptyTextField(password);
        if(!areAllFieldsEmpty){
            if(ValidationUtil.isValidPhoneNumber(username)){
                    SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                    String utype =  sharedpreferences.getString("selectedUserTypeID","");
                    RequestParams rp = new RequestParams();
                    rp.add("appId","g24API2");
                    rp.add("pwd","API2g24");
                    rp.add("mobile_number",username.getText().toString());
                    rp.add("password",password.getText().toString());
                    rp.add("uType",utype);
                    String phpName = "loginUser.php";
                    Log.d("req","Request is>>>"+rp);
                RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                relLayoutProgress.setVisibility(View.VISIBLE);
                makeLoginRestCall(view,rp,phpName);
            }
            else{
                showValidationMsg("Please enter your phone number as UserName");
            }
        }else{
            showValidationMsg("Please enter all the fields");
        }
    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }

    private void makeLoginRestCall(final View view, RequestParams rp, String phpName) {
        HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                try{

                    String respMsg = (String)res.getString("res");
                    int errCode = (Integer)res.getInt("error_code");
                    String userID = (String)res.getString("user_id");
                    Log.d("res","Response message is>>>>"+respMsg);
                    showLoginResp(view,respMsg,userID,errCode);

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

    private void showLoginResp(View view, String respMsg, final String userID,int errCode) {
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
        relLayoutProgress.setVisibility(View.GONE);
        if(0==errCode){
            showValidationMsg(respMsg);
            SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("loggedInUserID",userID);
            String loginUserTypeID =  sharedpreferences.getString("selectedUserTypeID","");
            Intent intent=null;
            if("2".equalsIgnoreCase(loginUserTypeID)){
                intent= new Intent(getApplicationContext(), HomeSMUserActivity.class);
            }
            else if("3".equalsIgnoreCase(loginUserTypeID)){
                intent= new Intent(getApplicationContext(), HomeRunnerActivity.class);
            }
            else if("4".equalsIgnoreCase(loginUserTypeID)){
                intent= new Intent(getApplicationContext(),HomeDistributorActivity.class);
            }
            else if("5".equalsIgnoreCase(loginUserTypeID)){
                intent= new Intent(getApplicationContext(),HomeUserActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            String loginUser = USERMAP.get(loginUserTypeID);
            editor.putString("loginUserTypeName",loginUser);
            editor.commit();
            startActivity(intent);
        }
        else{
            showValidationMsg(respMsg);
        }

    }



    public void onForgotPassword(View v){
        Intent  i = new Intent(this,ForgotPasswordActivity.class);
        startActivity(i);
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    public void showUserTypeDialogReg(View v){
            CustomDialogUserTypeReg customDialog= new CustomDialogUserTypeReg();
            customDialog.show(getSupportFragmentManager(), "CustomDialogFragment");
    }
    public void onUtypeRadio(View v){
        Intent i= new Intent(this,RegisterActivity.class);
        int id = v.getId();
        String utype = "";
        if(id==R.id.userRadio){
            utype = "5";
        }
        else if(id==R.id.distributorRadio){
            utype = "4";
        }

        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.putString("selectedUserTypeID",utype);
        editor.commit();
        startActivity(i);
    }
}
