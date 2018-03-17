package in.goods24.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.pojo.ProductPOJO;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.GenericUtil;
import in.goods24.util.HttpUtils;

import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.WIDTH;

public class CartActivity extends AppCompatActivity implements View.OnClickListener{
    private RequestParams rp;
    private String loggedInUserID=null;
    private String phpName;
    private String respMsg;
    private int errCode;
    ArrayList<ProductPOJO> prodList=null;
    private ArrayList<String> prodIDsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fetchCartProducts();
    }
    @Override
    public void onClick(View v){
        if(null!=prodIDsList){
            for(String prodID:prodIDsList){
                if(v.getId()==Integer.parseInt("777"+prodID+"777")){
                    deleteCurrProd(prodID);
                }
            }
        }

    }

    private void deleteCurrProd(String prodID) {
        if(null==loggedInUserID){
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        loggedInUserID =sharedPreferences.getString("loggedInUserID","");
        }
        rp =new RequestParams();
        rp.add("g24API8", "g24API7");
        rp.add("pwd", "API8g24");
        rp.add("oprn","delete");
        rp.add("user_id",loggedInUserID);
        rp.add("product_id",prodID);
        phpName = "manageCart.php";
        Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
        makeDeleteProdFromCartRestCall(rp,phpName);
    }

    private void makeDeleteProdFromCartRestCall(RequestParams requestParams, String phpName) {

        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = res.getString("res");
                    int errCode =  res.getInt("error_code");
                    Log.d("res", "Response message is>>>>" + respMsg);
                    showValidationMsg(respMsg);
                    if(0==errCode){
                    reloadActivity(respMsg,errCode);
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

    private void reloadActivity(String respMsg, int errCode) {
        Intent i = new Intent(this, CartActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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
                    addElementsToLayout(prodList);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.d("EXC","Exception occurred in performingOperationAfterFetching");
                }
            }

        }

        private void addElementsToLayout(ArrayList<ProductPOJO> prodList) {
            TableLayout tableLayout  = (TableLayout)findViewById(R.id.tableLayoutCart);
            tableLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) CartActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            prodIDsList=new ArrayList<>();
            while(0!=prodList.size()){
                TableRow tableRow = new TableRow(CartActivity.this);
                //tableRow.setBackgroundResource(R.drawable.border);
                    View viewForTableRow = inflater.inflate(R.layout.prod_layout_for_cart, null,false);
                    ImageView prodImg = (ImageView) viewForTableRow
                            .findViewById(R.id.prodImage);
                    String imgStr = prodList.get(0).getProductImage();
                    imgStr = imgStr.substring(imgStr.indexOf(",")+1);
                    InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
                    Bitmap bitmap = BitmapFactory.decodeStream(new  FlushedInputStream(isImage));
                    prodImg.setImageBitmap(bitmap);
                    prodImg.getLayoutParams().height = HEIGHT/8;
                    prodImg.getLayoutParams().width = WIDTH/8;
                    prodImg.requestLayout();
                    TextView prodNameTxt = (TextView)viewForTableRow.findViewById(R.id.prodName);
                    TextView prodDescTxt = (TextView)viewForTableRow.findViewById(R.id.prodDesc);
                    TextView prodPriceTxt = (TextView)viewForTableRow.findViewById(R.id.prodPrice);
                    TextView prodDiscPriceTxt = (TextView)viewForTableRow.findViewById(R.id.prodDiscountedPrice);
                    TextView prodQuantityTxt =(TextView)viewForTableRow.findViewById(R.id.prodQuantity);
                    prodNameTxt.setText(prodList.get(0).getProductName());
                    prodDescTxt.setText(prodList.get(0).getProductDesc());
                    prodPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodList.get(0).getProductRate())));
                    prodPriceTxt.setPaintFlags(prodPriceTxt.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                    prodDiscPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodList.get(0).getProductRate())-Float.parseFloat(prodList.get(0).getProductDiscount())));
                    prodQuantityTxt.setText(prodList.get(0).getProductQuantity());
                    TextView deleteProdCartBtn = (TextView) viewForTableRow.findViewById(R.id.deleteProdCart);
                    deleteProdCartBtn.setId(Integer.parseInt("777"+prodList.get(0).getProductID()+"777"));
                    GenericUtil.setMargins(viewForTableRow,10,0,0,0);
                    GenericUtil.setMarginStart(viewForTableRow,10);
                    tableRow.addView(viewForTableRow);
                    setRequiredFieldsToList(prodList.get(0));
                    prodList.remove(0);
                TableLayout.LayoutParams tableRowParams=
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);

                int leftMargin=10;
                int topMargin=2;
                int rightMargin=10;
                int bottomMargin=2;

                tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                //tableRowParams.gravity = Gravity.END;
                tableRow.setLayoutParams(tableRowParams);
                tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                tableLayout.addView(tableRow);
            }
            if(0!=prodIDsList.size()){
                makeProductButtonsClickable();
            }

        }


        private void setRequiredFieldsToList(ProductPOJO productPOJO) {
            prodIDsList.add(productPOJO.getProductID());
        }

    }
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }
    }
        private void makeProductButtonsClickable() {
            for(String cartDeleteBtn:prodIDsList){
                findViewById(Integer.parseInt("777"+cartDeleteBtn+"777")).setOnClickListener(this);
            }
        }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }


}
