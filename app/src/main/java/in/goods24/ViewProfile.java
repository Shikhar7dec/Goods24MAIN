package in.goods24;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;

public class ViewProfile extends AppCompatActivity {
    private String name;
    private String eMail;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarViewProfLayout);
        relLayoutProgress.setVisibility(View.VISIBLE);
        fetchProfDetails();
    }

    private void fetchProfDetails() {
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
        makeProfDetRestCall(rp,phpName);
    }

    private void makeProfDetRestCall(RequestParams rp, String phpName) {
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

    private void gotoPreviousActivity() {
        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showValidationMsg(String message) {

        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }

    private void putProfDetails(JSONObject res, int errCode) {
        try {
            if(0==errCode){
                String nameRes = (String)res.getString("first_name")+" "
                        +(String)res.getString("last_name");
                String eMailRes = (String)res.getString("email_id");
                String phoneRes = (String)res.getString("mobile_no");
                name=nameRes;
                eMail=eMailRes;
                phone=phoneRes;
                TextView nameTV = (TextView)findViewById(R.id.viewProfNameText);
                TextView eMailTV = (TextView)findViewById(R.id.viewProfeMailText);
                TextView phoneTV = (TextView)findViewById(R.id.viewProfPhoneText);
                Log.d("VALS","Values are>>>"+name+">>>"+eMail+">>>"+phone);
                nameTV.setText(name);
                eMailTV.setText(eMail);
                phoneTV.setText(phone);
                /*requestWindowFeature(Window.FEATURE_NO_TITLE);*/
                RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarViewProfLayout);
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
