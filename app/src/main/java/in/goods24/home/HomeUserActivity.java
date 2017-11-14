package in.goods24.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import in.goods24.common.ChangePwdActivity;
import in.goods24.common.MainActivity;
import in.goods24.R;
import in.goods24.common.UpdateProfile;
import in.goods24.common.ViewProfile;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;

public class HomeUserActivity extends AppCompatActivity {
    private int backButtonCount= 0;
    private String[] prodCatTypeArr;
    private String[] prodCatTypeIDArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        fetchProductCatTypes();


        /*SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String userTypeName =  sharedpreferences.getString("loginUserTypeName","");
        TextView userTypeTextView= (TextView)findViewById(R.id.userTypeTextView);
        userTypeTextView.setText(userTypeName);*/
    }

    private void setTabs() {
        TableRow tabs = (TableRow)findViewById(R.id.upperMenuBarRow);
        for(int i=0;i<prodCatTypeArr.length;i++){
            //Button element = new Button(this);
            TextView element = new TextView(this);
            element.setText(prodCatTypeArr[i]);
            element.setBackgroundResource(R.drawable.button_design_background);
            element.setTextSize(10);
            element.setTextColor(Color.WHITE);
            element.setId(100*(Integer.parseInt(prodCatTypeIDArr[i])+1));

            tabs.addView(element);
        }

    }

    private void fetchProductCatTypes() {
        RequestParams requestParams =new RequestParams();
        requestParams.add("appId", "g24API7");
        requestParams.add("pwd", "API7g24");
        requestParams.add("oprn","getProductType");
        String phpName = "manageProducts.php";
        Log.d("REQ","Request parameter are>>>>"+requestParams+">>"+phpName);
        makeFetchCatDetailsRestCall(requestParams,phpName);
    }

    private void makeFetchCatDetailsRestCall(RequestParams requestParams, String phpName) {

        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = (String) res.getString("res");
                    int errCode = (Integer) res.getInt("error_code");
                    String catIds = (String)res.getString("category_id");
                    String catNames = (String)res.getString("category_name");
                    Log.d("res", "Response message is>>>>" + respMsg+"CatNames>>"+catNames);
                    fetchingDetailsIntoArray(respMsg,errCode,catIds,catNames);
                } catch (Exception e) {
                    e.printStackTrace();
                    showValidationMsg("Please check your internet and try again");
                        /*RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                        relLayoutProgress.setVisibility(View.GONE);*/
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Log.d("log", "Status code is>>" + statusCode + "Response code>>>" + responseString);
                showValidationMsg("Some Error occurred please try again");
                    /*RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                    relLayoutProgress.setVisibility(View.GONE);*/
            }
        });


    }

    private void fetchingDetailsIntoArray(String respMsg, int errCode, String catIds, String catNames) {
        if(0==errCode){
            prodCatTypeArr = catNames.split("#");
            prodCatTypeIDArr = catIds.split("#");
            setTabs();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeUserActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.submenuViewProfile){
            Intent i = new Intent(this, ViewProfile.class);
            startActivity(i);
        }
        if(id==R.id.submenuUpdateProfile){
            Intent i= new Intent(this, UpdateProfile.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.changePasswordOptionsMenu) {
            Intent i = new Intent(this,ChangePwdActivity.class);
            startActivity(i);
            return true;
        }
        if(id==R.id.logoutOptionsMenu){
            SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= sharedPreferences.edit();
            editor.clear();
            editor.commit();
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed()
    {
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
}
