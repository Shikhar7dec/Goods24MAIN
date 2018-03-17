package in.goods24.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.home.HomeUserActivity;
import in.goods24.pojo.ProductPOJO;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;

import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.WIDTH;

public class BuyProduct extends AppCompatActivity implements View.OnClickListener{

    ProductPOJO prodObj=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fetchProductDetailsToBuy();
    }
    @Override
    public void onClick(View v){
        if(v.getId()==R.id.checkoutProduct){
            Log.d("DEB","inside onClick id is"+v.getId());
        }
        if(v.getId()==R.id.checkoutProduct){
                checkOutProduct();
        }

    }

    private void checkOutProduct() {
        EditText checkOutQuantity = (EditText)findViewById(R.id.prodQuantity);
        boolean isCheckoutQuantBlank = ValidationUtil.isEmptyTextField(checkOutQuantity);
        if(!isCheckoutQuantBlank){
            int checkOutQuantityVal = Integer.parseInt(checkOutQuantity.getText().toString());
            double productAmount = Double.parseDouble(String.format("%.2f",Float.parseFloat(prodObj.getProductRate())-Float.parseFloat(prodObj.getProductDiscount())));
            double finalAmt = productAmount * checkOutQuantityVal;
            SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
            String loggedInUserID =sharedPreferences.getString("loggedInUserID","");
            RequestParams rp =new RequestParams();
            rp.add("appId", "g24API9");
            rp.add("pwd", "API9g24");
            rp.add("oprn","buyNow");
            rp.add("user_id",loggedInUserID);
            rp.add("product_cost",String.valueOf(productAmount));
            rp.add("product_id",prodObj.getProductID());
            rp.add("total_cost",String.valueOf(finalAmt));
            rp.add("no_of_product",String.valueOf(checkOutQuantityVal));
            String phpName = "checkout.php";
            Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
            makeCheckoutRestCall(rp,phpName);
        }
        else{
            showValidationMsg("product quantity cannot be blank");
        }
    }

    private void makeCheckoutRestCall(RequestParams requestParams, String phpName) {

        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = res.getString("res");
                    int errCode =  res.getInt("error_code");
                    Log.d("res", "Response message is>>>>" + respMsg);
                    if(errCode==0){

                    showBoughtResp(respMsg,errCode);
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

    private void showBoughtResp(String respMsg, int errCode) {
        showValidationMsg("Congratulations! Product will be delivered to you shortly");
        Intent i = new Intent(this, HomeUserActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(),message,
                Toast.LENGTH_LONG)
                .show();
    }

    private void fetchProductDetailsToBuy() {
        String jsonObject=null;
        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            jsonObject=extras.getString("selectedProdObject");
        }
        prodObj = new Gson().fromJson(jsonObject,ProductPOJO.class);
        Log.d("DEB","Product Object value is"+prodObj.getProductID());

        TextView prodNameTxt = (TextView)findViewById(R.id.prodName);
        TextView prodDescTxt = (TextView)findViewById(R.id.prodDesc);
        TextView prodPriceTxt = (TextView)findViewById(R.id.prodPrice);
        TextView prodDiscPriceTxt = (TextView)findViewById(R.id.prodDiscountedPrice);
        ImageView prodImg = (ImageView)findViewById(R.id.prodImage);
        String imgStr = prodObj.getProductImage();
        imgStr = imgStr.substring(imgStr.indexOf(",")+1);
        InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
        Bitmap bitmap = BitmapFactory.decodeStream(new CartActivity.FlushedInputStream(isImage));
        prodImg.setImageBitmap(bitmap);
        prodImg.getLayoutParams().height = HEIGHT/2;
        prodImg.getLayoutParams().width = WIDTH/2;
        prodImg.requestLayout();
        prodNameTxt.setText(prodObj.getProductName());
        prodDescTxt.setText(prodObj.getProductDesc());
        prodPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodObj.getProductRate())));
        prodPriceTxt.setPaintFlags(prodPriceTxt.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
        prodDiscPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodObj.getProductRate())-Float.parseFloat(prodObj.getProductDiscount())));
        findViewById(R.id.checkoutProduct).setOnClickListener(this);

    }
}
