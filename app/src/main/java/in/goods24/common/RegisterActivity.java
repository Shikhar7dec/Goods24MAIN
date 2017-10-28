package in.goods24.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
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
import in.goods24.common.LoginActivity;
import in.goods24.common.MainActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;


public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    public void onFinalRegister(View view){

        EditText fname = (EditText)findViewById(R.id.regFName);
        EditText lname = (EditText)findViewById(R.id.regLName);
        EditText email = (EditText)findViewById(R.id.regEmail);
        EditText phone = (EditText)findViewById(R.id.regPhone);
        EditText pass = (EditText)findViewById(R.id.regPass);
        EditText rePass= (EditText)findViewById(R.id.regPassRe);
        Boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(fname)
                ||ValidationUtil.isEmptyTextField(lname)
                ||ValidationUtil.isEmptyTextField(email)
                ||ValidationUtil.isEmptyTextField(phone)
                ||ValidationUtil.isEmptyTextField(pass)
                ||ValidationUtil.isEmptyTextField(rePass);

        if(!areAllFieldsEmpty){
            if(ValidationUtil.isValidEmail(email)){
                if(ValidationUtil.isValidPhoneNumber(phone)){
                    if(ValidationUtil.passwordMatcher(pass,rePass)){
                        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                        String utype =  sharedpreferences.getString("selectedUserTypeID","");
                        RequestParams rp = new RequestParams();
                        rp.add("appId","g24API1");
                        rp.add("pwd","API1g24");
                        rp.add("utype",utype);
                        rp.add("fname",fname.getText().toString());
                        rp.add("lname",lname.getText().toString());
                        rp.add("email",email.getText().toString());
                        rp.add("mobile",phone.getText().toString());
                        rp.add("password",pass.getText().toString());
                        String phpName = "createUser.php";
                        Log.d("REQ","Request from app>>>>>"+rp);
                        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                        relLayoutProgress.setVisibility(View.VISIBLE);
                        makeRegRestCall(view,rp,phpName);
                    }
                    else
                        showValidationMsg("Password doesn't match with Confirm Password");
                }
                else
                    showValidationMsg("Please enter valid phone number");
            }
            else
                showValidationMsg("Please enter valid e-mail");
        }
        else
            showValidationMsg("Please enter all fields");
    }
    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }
    private void makeRegRestCall( final View view, RequestParams rp, String phpName) {
        HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                try{

                   String respMsg = (String)res.get("res");
                   int errCode= (Integer)res.getInt("error_code");
                    showRegResp(view,respMsg,errCode);

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

    private void showRegResp(View view,String respMessage, int errCode) {
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
        relLayoutProgress.setVisibility(View.GONE);
        if(0==errCode){
            showValidationMsg(respMessage);
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else{
            showValidationMsg(respMessage);
        }

    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

