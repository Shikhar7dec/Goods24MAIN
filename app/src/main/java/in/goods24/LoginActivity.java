package in.goods24;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

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
        Boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(username)||
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
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
                Toast.makeText(getApplicationContext(),
                        "Connection Error!!!",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void showLoginResp(View view, String respMsg, final String userID,int errCode) {
        if(0==errCode){
            Snackbar snackbar = Snackbar.make(view, respMsg, Snackbar.LENGTH_LONG);
            snackbar.setAction("Go home", new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("loggedInUserID",userID);
                    String loginUserTypeID =  sharedpreferences.getString("selectedUserTypeID","");
                    String loginUser = "5".equalsIgnoreCase(loginUserTypeID)
                            ?"User":"Distributor";
                    editor.putString("loginUserTypeName",loginUser);
                    editor.commit();
                    startActivity(intent);
                }
            });
            snackbar.setActionTextColor(Color.GREEN);
            snackbar.show();
        }
        else{
            showValidationMsg(respMsg);
        }

    }
    public void onForgotPassword(View v){
        Intent  i = new Intent(this,ForgotPasswordActivity.class);
        startActivity(i);
    }

}
