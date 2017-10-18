package in.goods24;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinnerDistType;
    private static final String[] distTypeList = {"Select User Type",
            "User","Distributor"};
    private String spinnerChoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        spinnerDistType = (Spinner)findViewById(R.id.userTypeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this,
                android.R.layout.simple_spinner_item,distTypeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistType.setAdapter(adapter);
        spinnerDistType.setSelection(0);
        spinnerDistType.setOnItemSelectedListener(this);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                spinnerChoice=null;
                break;
            case 1:
                spinnerChoice="5";
                break;
            case 2:
                spinnerChoice="4";
                break;

        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent){}
    public void onLogin(View view){
        EditText username= (EditText)findViewById(R.id.loginUserText);
        EditText password= (EditText)findViewById(R.id.loginPasswordText);
        Boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(username)||
                ValidationUtil.isEmptyTextField(password);
        if(!areAllFieldsEmpty){
            if(ValidationUtil.isValidPhoneNumber(username)){
                if(null!=spinnerChoice&&
                        !"".equalsIgnoreCase(spinnerChoice)){
                    RequestParams rp = new RequestParams();
                    rp.add("appId","g24API2");
                    rp.add("pwd","API2g24");
                    rp.add("mobile_number",username.getText().toString());
                    rp.add("password",password.getText().toString());
                    rp.add("uType",spinnerChoice);
                    String phpName = "loginUser.php";
                    Log.d("req","Request is>>>"+rp);
                    makeLoginRestCall(view,rp,phpName);
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "Please select valid User Type",
                            Toast.LENGTH_LONG)
                            .show();
                }

            }
            else{
                Toast.makeText(getApplicationContext(),
                        "Please enter your phone number as UserName",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }else{
            Toast.makeText(getApplicationContext(),
                    "Please enter all the fields",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void makeLoginRestCall(final View view, RequestParams rp, String phpName) {
        HttpUtils.post(phpName,rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res","---------------response from host>>> "+res+"Status is"+statusCode);
                try{

                    String respMsg = (String)res.getString("res");
                    Log.d("res","Response message is>>>>"+respMsg);
                    showLoginResp(view,respMsg);

                }
                catch (Exception e){
                    e.printStackTrace();
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

    private void showLoginResp(View view, String respMsg) {
        Snackbar snackbar = Snackbar.make(view, respMsg, Snackbar.LENGTH_LONG);
        snackbar.setAction("Go home", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(),Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle b = new Bundle();
                if("5".equalsIgnoreCase(spinnerChoice)){
                    spinnerChoice= "User";
                }
                else if("4".equalsIgnoreCase(spinnerChoice)){
                    spinnerChoice= "Distributor";
                }
                b.putString("loginUser",spinnerChoice);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
    }

}
