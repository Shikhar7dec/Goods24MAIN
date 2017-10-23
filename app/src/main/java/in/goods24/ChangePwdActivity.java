package in.goods24;

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
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

public class ChangePwdActivity extends AppCompatActivity implements View.OnClickListener{
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        findViewById(R.id.saveChangePasswordButton).setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.saveChangePasswordButton:
                onSaveButtonChangePwd(v);
                break;
        }
    }

    private void onSaveButtonChangePwd(View v) {
        EditText currPassword = (EditText)findViewById(R.id.changePasswordCurrText);
        EditText newPassword = (EditText)findViewById(R.id.changePasswordNewText);
        EditText reNewPassword = (EditText)findViewById(R.id.changePasswordReText);

        Boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(currPassword)
                ||ValidationUtil.isEmptyTextField(newPassword)
                ||ValidationUtil.isEmptyTextField(reNewPassword);
        if(!areAllFieldsEmpty){
            if(ValidationUtil.passwordMatcher(newPassword,reNewPassword)){
                sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                String userID=sharedPreferences.getString("loggedInUserID","");
                String loginUserType=sharedPreferences.getString("selectedUserTypeID","");

                //Log.d("USERID","User ID from session is>>>"+userID);
                RequestParams rp = new RequestParams();
                rp.add("appId","g24API3");
                rp.add("pwd","API3g24");
                rp.add("uType",loginUserType);
                rp.add("userId",userID);
                rp.add("newPwd",newPassword.getText().toString());
                rp.add("oldPwd",currPassword.getText().toString());
                String phpName = "changePwd.php";
                Log.d("REQ","request is>>>>>"+rp);
                RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarViewProfLayout);
                relLayoutProgress.setVisibility(View.VISIBLE);
                makeChangePwdRestCall(v,rp,phpName);

            }
            else
                showValidationMsg("Password doesn't match with Confirm Password");
        }
        else
            showValidationMsg("Please enter all fields");
    }

    private void makeChangePwdRestCall(final View v, RequestParams rp, String phpName) {
        {
            HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                    try{

                        String respMsg = (String)res.get("res");
                        int errCode = (Integer)res.getInt("error_code");
                        showChangePwdResp(v,respMsg,errCode);

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

    private void showChangePwdResp(View v, String respMsg, int errCode) {
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarViewProfLayout);
        relLayoutProgress.setVisibility(View.GONE);
        if(0==errCode){
                showValidationMsg(respMsg);
                sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.clear();
                editor.commit();
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
