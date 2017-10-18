package in.goods24;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
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

        //Log.d("check","value of fname>>>"+fname.getText().toString());

        Boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(fname)
                ||ValidationUtil.isEmptyTextField(lname)
                ||ValidationUtil.isEmptyTextField(email)
                ||ValidationUtil.isEmptyTextField(phone)
                ||ValidationUtil.isEmptyTextField(pass)
                ||ValidationUtil.isEmptyTextField(rePass);
        //Log.d("check","value of Boolean areAllFieldsEmpty>>>"+areAllFieldsEmpty);

        if(!areAllFieldsEmpty){
            if(ValidationUtil.isValidEmail(email)){
                if(ValidationUtil.isValidPhoneNumber(phone)){
                    if(ValidationUtil.passwordMatcher(pass,rePass)){
                        Bundle bundle = getIntent().getExtras();
                        String utype = bundle.getString("utype");
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
                        makeRegRestCall(view,rp,phpName);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),
                                "Password doesn't match with Confirm Password",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please enter valid phone number",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
            else{
                Toast.makeText(getApplicationContext(),"Please enter valid e-mail",Toast.LENGTH_LONG)
                        .show();
            }

        }
        else{
            Toast.makeText(getApplicationContext(),"Please enter all fields",Toast.LENGTH_LONG)
                    .show();
        }




    }

    private void makeRegRestCall( final View view, RequestParams rp, String phpName) {
        HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                try{

                   String respMsg = (String)res.get("res");
                    showRegResp(view,respMsg);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Log.d("log","Status code is>>"+statusCode+"Response code>>>"+responseString);
            }
        });
    }

    private void showRegResp(View view,String respMessage) {
        Snackbar snackbar = Snackbar.make(view, respMessage, Snackbar.LENGTH_LONG);
        snackbar.setAction("Done", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
    }
}

