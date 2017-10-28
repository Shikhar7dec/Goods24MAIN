package in.goods24.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

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
                    RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                    relLayoutProgress.setVisibility(View.VISIBLE);
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
                        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                        relLayoutProgress.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString,
                                      Throwable throwable) {
                    Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
                }
            });



    }

    private void showForgotPwdResp(View view, String respMsg,int errCode) {
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
        relLayoutProgress.setVisibility(View.GONE);
        if(0==errCode){
            showValidationMsg(respMsg);
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
        else{
            showValidationMsg(respMsg);
        }

    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }
}

