package in.goods24.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import in.goods24.distributor.AddProductDistActivity;
import in.goods24.distributor.DeleteProductDistActivity;
import in.goods24.distributor.UpdateProductDistActivity;
import in.goods24.distributor.ViewProductDistActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;

public class HomeDistributorActivity extends AppCompatActivity
        implements View.OnClickListener,AdapterView.OnItemSelectedListener{
    private String[] catProdValues;
    private String[] catProdIds;
    private String[] prodValues;
    private String[] prodIds;
    private int backButtonCount=0;
    private String selectedCategoryID;
    private Dialog prodDialog;
    private int veiwProdFlag= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_distributor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        findViewById(R.id.buttonAddProduct).setOnClickListener(this);
        findViewById(R.id.buttonViewProduct).setOnClickListener(this);
        findViewById(R.id.buttonUpdateProduct).setOnClickListener(this);
        findViewById(R.id.buttonDeleteProduct).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAddProduct:
                Intent intent = new Intent(this, AddProductDistActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonViewProduct:
                veiwProdFlag=1;
                showProductDialog();
                break;
            case R.id.buttonUpdateProduct:
                veiwProdFlag=2;
                showProductDialog();
                break;
            case R.id.buttonDeleteProduct:
                veiwProdFlag=3;
                showProductDialog();
                break;
            case R.id.cancelButtonDialogViewProd:
                prodDialog.dismiss();
        }
    }

    private void showProductDialog() {
        prodDialog = new Dialog(this);
        prodDialog.setContentView(R.layout.dialog_product_dist);
        prodDialog.setTitle("Select Product");
        fetchProdSpinnerValues();
    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }
    @Override
    public void onBackPressed() {
        if (backButtonCount >= 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.distributor_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeUserActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.submenuViewProfile) {
            Intent i = new Intent(this, ViewProfile.class);
            startActivity(i);
        }
        if (id == R.id.submenuUpdateProfile) {
            Intent i = new Intent(this, UpdateProfile.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.changePasswordOptionsMenu) {
            Intent i = new Intent(this, ChangePwdActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.logoutOptionsMenu) {
            SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void fetchProdSpinnerValues() {
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
                    fetchingDetailsIntoSpinner(respMsg,errCode,catIds,catNames);
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

            private void fetchingDetailsIntoSpinner(String respMsg, int errCode,
                                                    String catIds,String catNames){
                if(0==errCode){
                    catIds="0#"+catIds;
                    catNames="Select Product Type#"+catNames;
                    catProdIds=catIds.split("#");
                    catProdValues= catNames.split("#");
                    Spinner prodCatValSpinner = (Spinner) prodDialog.findViewById(R.id.spinnerProductType);
                    Spinner prodNameSpinner = (Spinner) prodDialog.findViewById(R.id.spinnerProductList);
                    ArrayAdapter<String> prodCatAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item,catProdValues);
                    ArrayAdapter<String> prodNameAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item,new String[]{"Select Product Type First"});
                    prodCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    prodCatValSpinner.setAdapter(prodCatAdapter);
                    prodCatValSpinner.setSelection(0);
                    prodNameSpinner.setAdapter(prodNameAdapter);
                    prodDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    prodDialog.show();
                    prodDialog.findViewById(R.id.cancelButtonDialogViewProd).setOnClickListener(this);
                    prodCatValSpinner.setOnItemSelectedListener(this);
                }
                else{
                    showValidationMsg(respMsg);
                }
            }
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if(0!=position){
            Spinner s = (Spinner)parent;
            Log.d("DEB","value of id is>>"+s.getId()+">>>"+R.id.spinnerProductType);
            if(s.getId()==R.id.spinnerProductType){
                Log.d("DEB", "item Selected" + catProdValues[position]);
                selectedCategoryID = catProdIds[position];
                SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                String loggedInUserID =sharedPreferences.getString("loggedInUserID","");
                RequestParams requestParams =new RequestParams();
                requestParams.add("appId", "g24API7");
                requestParams.add("pwd", "API7g24");
                requestParams.add("oprn","getProductByCategory");
                requestParams.add("category_id",selectedCategoryID);
                requestParams.add("user_id",loggedInUserID);
                String phpName = "manageProducts.php";
                Log.d("REQ","Request parameter are>>>>"+requestParams+">>"+phpName);
                makeFetchProdListRestCall(requestParams,phpName);
            }
            if(s.getId()==R.id.spinnerProductList){
                Bundle b = new Bundle();
                b.putString("SelectedProductID",prodIds[position]);
                Intent i=null;
                switch(veiwProdFlag){
                    case 1:
                        i = new Intent(this, ViewProductDistActivity.class);
                        break;
                    case 2:
                        i = new Intent(this, UpdateProductDistActivity.class);
                        break;
                    case 3:
                        i = new Intent(this, DeleteProductDistActivity.class);
                        break;
                }
                i.putExtras(b);
                startActivity(i);
            }

        }
    }

    private void makeFetchProdListRestCall(RequestParams requestParams, String phpName) {
        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = (String) res.getString("res");
                    int errCode = (Integer) res.getInt("error_code");
                    String prodIds = (String)res.getString("product_id");
                    String prodNames = (String)res.getString("product_name");
                    Log.d("res", "Response message is>>>>" + respMsg+"CatNames>>"+prodNames);
                    fetchingProdListIntoSpinner(respMsg,errCode,prodIds,prodNames);
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        showValidationMsg("Please select any category");
    }

    private void fetchingProdListIntoSpinner(String respMsg, int errCode, String prodIds, String prodNames) {
        if(0==errCode){
            prodIds="0#"+prodIds;
            prodNames="Select Product#"+prodNames;
            this.prodIds=prodIds.split("#");
            prodValues= prodNames.split("#");
            Spinner prodNameSpinner = (Spinner) prodDialog.findViewById(R.id.spinnerProductList);
            ArrayAdapter<String> prodNameAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item,prodValues);
            prodNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            prodNameSpinner.setAdapter(prodNameAdapter);
            prodNameSpinner.setSelection(0);
            prodNameSpinner.setOnItemSelectedListener(this);
        }
        else{
            showValidationMsg(respMsg);
        }
    }
}

