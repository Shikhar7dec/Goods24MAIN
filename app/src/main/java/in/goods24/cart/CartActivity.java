package in.goods24.cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.pojo.ProductPOJO;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;

public class CartActivity extends AppCompatActivity {
    private RequestParams rp;
    private String loggedInUserID;
    private String phpName;
    private String respMsg;
    private int errCode;
    ArrayList<ProductPOJO> prodList=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fetchCartProducts();
    }

    private void fetchCartProducts() {
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        loggedInUserID =sharedPreferences.getString("loggedInUserID","");
        rp =new RequestParams();
        rp.add("g24API8", "g24API7");
        rp.add("pwd", "API8g24");
        rp.add("oprn","view");
        rp.add("user_id",loggedInUserID);
        phpName = "manageCart.php";
        Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
        new FetchProductsHelper().execute();
    }
    private class FetchProductsHelper extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            Log.d("TODO","preExecuteTask"+rp+">>>"+phpName);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }
        @Override
        protected String doInBackground(String... args) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    makeViewCartProductsRestCall(rp,phpName);
                }
            };
            mainHandler.post(myRunnable);

            return null;
        }
        private void makeViewCartProductsRestCall(RequestParams rp, String phpName) {


            HttpUtils.post(phpName, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                    try {

                        respMsg = res.getString("res");
                        errCode = res.getInt("error_code");
                        Log.d("res", "Response message is>>>>" + respMsg);
                        if(0==errCode){
                            performingOperationAfterFetching(res);
                        }
                        else{
                            showValidationMsg(respMsg);
                        }

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
        private void performingOperationAfterFetching(JSONObject res) {
            if(0==errCode){
                try {
                    JSONArray productsJSONArr = res.getJSONArray("arr");
                    prodList = new ArrayList<ProductPOJO>();
                    ProductPOJO productObj=null;
                    JSONObject jsonObject = null;
                    for(int itr=0;itr<productsJSONArr.length();itr++){
                        productObj=new ProductPOJO();
                        jsonObject= (JSONObject)productsJSONArr.get(itr);

                        productObj.setProductID(jsonObject.getString("product_id"));
                        productObj.setProductName(jsonObject.getString("product_name"));
                        productObj.setProductDesc(jsonObject.getString("product_desc"));
                        productObj.setProductQuantity(jsonObject.getString("no_of_product"));
                        productObj.setProductRate(jsonObject.getString("product_rate"));
                        productObj.setProductDiscount(jsonObject.getString("product_discount"));
                        productObj.setProductImage(jsonObject.getString("product_image"));
                        prodList.add(productObj);
                    }

                    Log.d("DEB","Size of Products"+prodList.size());
                    //addElementsToLayout(prodList);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.d("EXC","Exception occurred in performingOperationAfterFetching");
                }
            }

        }
    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }


}
