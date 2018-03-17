package in.goods24.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.goods24.user.BuyProduct;
import in.goods24.user.CartActivity;
import in.goods24.common.ChangePwdActivity;
import in.goods24.common.MainActivity;
import in.goods24.R;
import in.goods24.common.UpdateProfile;
import in.goods24.common.ViewProfile;
import in.goods24.pojo.ProductPOJO;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.GenericUtil;
import in.goods24.util.HttpUtils;

import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.WIDTH;

public class HomeUserActivity extends AppCompatActivity implements View.OnClickListener{
    private RequestParams rp;
    private String phpName;
    private int backButtonCount= 0;
    private String[] prodCatTypeArr;
    private String[] prodCatTypeIDArr;
    private ArrayList<String> distUserIDsProdList;
    private ArrayList<String> prodIDsList;
    private String loggedInUserID;
    ArrayList<ProductPOJO> prodList=null;
    ArrayList<ProductPOJO> prodListClone=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        fetchProductCatTypes();
        findViewById(R.id.fab_cart).setOnClickListener(this);

        showCartCount();

        /*CounterFab counterFab = (CounterFab) findViewById(R.id.fab_cart);
        counterFab.setCount(7);*/
        /*SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String userTypeName =  sharedpreferences.getString("loginUserTypeName","");
        TextView userTypeTextView= (TextView)findViewById(R.id.userTypeTextView);
        userTypeTextView.setText(userTypeName);*/
    }

    private void showCartCount() {
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        loggedInUserID =sharedPreferences.getString("loggedInUserID","");
        rp =new RequestParams();
        rp.add("appId", "g24API8");
        rp.add("pwd", "API8g24");
        rp.add("oprn","getCartCount");
        rp.add("user_id",loggedInUserID);
        String phpName = "manageCart.php";
        Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
        makeShowCartCountRestCall(rp,phpName);
    }

    private void makeShowCartCountRestCall(RequestParams requestParams, String phpName) {

        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = res.getString("res");
                    int errCode =  res.getInt("error_code");
                    int cartCount = res.getInt("cart_count");
                    Log.d("res", "Response message is>>>>" + respMsg+"CatNames>>"+cartCount);
                    settingCartCount(respMsg,errCode,cartCount);
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

    private void settingCartCount(String respMsg, int errCode, int cartCount) {
        if(errCode==0){
            CounterFab counterFab = (CounterFab) findViewById(R.id.fab_cart);
            counterFab.setCount(cartCount);
        }
    }

    @Override
    public void onClick(View v){
        //Log.d("deb","inside onClick");
        if(v.getId()==R.id.fab_cart){
            //FloatingActionButton cartButton = (FloatingActionButton)findViewById(R.id.fab_cart);
            //CounterFab counterFab = (CounterFab) findViewById(R.id.fab_cart);
            /*counterFab.increase();
            showValidationMsg("Functionality under development");*/
            Intent i = new Intent(this, CartActivity.class);
            startActivity(i);

        }
        if(null!=prodCatTypeIDArr){
            for(int itr=0;itr<prodCatTypeIDArr.length;itr++){
                if(v.getId()==Integer.parseInt(prodCatTypeIDArr[itr])){
                    fetchProductsByCategory(prodCatTypeIDArr[itr]);
                    //showValidationMsg("You have selected>>"+prodCatTypeArr[itr]);
                    break;
                }
            }
        }

        if(null!=prodIDsList){
            for(String prodID:prodIDsList){
                if(v.getId()==Integer.parseInt("777"+prodID+"777")){
                    //showValidationMsg("Add to Cart is selected for Prod ID>>"+prodID);
                    addCurrProdCart(prodID,loggedInUserID);
                    break;
                }
                if(v.getId()==Integer.parseInt("555"+prodID+"555")){
                    //showValidationMsg("Buy now is selected for Prod ID>>"+prodID);
                    buyCurrProd(prodID);
                    break;
                }

            }
        }



    }

    private void buyCurrProd(String prodID) {
        for(ProductPOJO currObject:prodListClone){
            if(currObject.getProductID().equalsIgnoreCase(prodID)){
                Intent i = new Intent(this, BuyProduct.class);
                i.putExtra("selectedProdObject",new Gson().toJson(currObject));
                startActivity(i);
            }
        }
        //Intent i = new Intent(this,)
    }

    private void addCurrProdCart(String prodID,String loggedInUserID) {
        rp =new RequestParams();
        rp.add("appId", "g24API8");
        rp.add("pwd", "API8g24");
        rp.add("oprn","add");
        rp.add("user_id",loggedInUserID);
        rp.add("product_id",prodID);
        String phpName = "manageCart.php";
        Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
        makeAddToCartCountRestCall(rp,phpName);

    }

    private void makeAddToCartCountRestCall(RequestParams requestParams, String phpName) {

        HttpUtils.post(phpName, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                try {

                    String respMsg = res.getString("res");
                    int errCode =  res.getInt("error_code");
                    Log.d("res", "Response message is>>>>" + respMsg+"CatNames>>"+errCode);
                    if(0==errCode)
                        onSuccessfulCartAdd(respMsg);
                    else
                        showValidationMsg(respMsg);
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

    private void onSuccessfulCartAdd(String respMsg) {
        showCartCount();
    }

    private void setTabs() {
        TableRow tabs = (TableRow)findViewById(R.id.upperMenuBarRow);
        for(int i=0;i<prodCatTypeArr.length;i++){
            Button element = new Button(this);
            //TextView element = new TextView(this);
            element.setText(prodCatTypeArr[i]);
            element.setBackgroundResource(R.drawable.button_design_background);
            element.setTextSize(10);
            element.setTextColor(Color.WHITE);
            element.setId(Integer.parseInt(prodCatTypeIDArr[i]));
            tabs.addView(element);
            TextView tv =new TextView(this);
            tv.setText(" ");
            tabs.addView(tv);
        }
        makeCategoryClickable();
        fetchProductsByCategory(prodCatTypeIDArr[0]);
    }

    private void fetchProductsByCategory(String catID) {
        rp =new RequestParams();
        rp.add("appId", "g24API7");
        rp.add("pwd", "API7g24");
        rp.add("oprn","getAllProductsByCategory");
        rp.add("category_id",catID);
        phpName = "manageProducts.php";
        Log.d("REQ","Request parameter are>>>>"+rp+">>"+phpName);
        new FetchProductsHelper().execute();
    }

    private void makeCategoryClickable() {
        for(String catID:prodCatTypeIDArr){
            findViewById(Integer.parseInt(catID)).setOnClickListener(this);
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
    private class FetchProductsHelper extends AsyncTask<String,String,String> {
        int errCode=1;
        String respMsg;
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
                    makeViewProductsRestCall(rp,phpName);
                }
            };
            mainHandler.post(myRunnable);

            return null;
        }
        private void makeViewProductsRestCall(RequestParams rp, String phpName) {


            HttpUtils.post(phpName, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                    try {

                        respMsg = (String) res.getString("res");
                        errCode = (Integer) res.getInt("error_code");
                        Log.d("res", "Response message is>>>>" + respMsg);
                        performingOperationAfterFetching(respMsg,errCode,res);
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

        private void performingOperationAfterFetching(String respMsg, int errCode, JSONObject res) {
            if(0==errCode){
                try {
                    JSONArray productsJSONArr = res.getJSONArray("arr");
                    prodList = new ArrayList<ProductPOJO>();
                    ProductPOJO productObj=null;
                    JSONObject jsonObject = null;
                    for(int itr=0;itr<productsJSONArr.length();itr++){
                        productObj=new ProductPOJO();
                        jsonObject= (JSONObject)productsJSONArr.get(itr);
                        productObj.setUserID(jsonObject.getString("user_id"));
                        productObj.setProductID(jsonObject.getString("product_id"));
                        productObj.setProductName(jsonObject.getString("product_name"));
                        productObj.setProductDesc(jsonObject.getString("product_desc"));
                        productObj.setProductQuantity(jsonObject.getString("product_quantity"));
                        productObj.setProductRate(jsonObject.getString("product_rate"));
                        productObj.setProductDiscount(jsonObject.getString("product_discount"));
                        productObj.setProductImage(jsonObject.getString("product_image"));
                        prodList.add(productObj);
                    }

                    Log.d("DEB","Size of Products"+prodList.size());
                    prodListClone = new ArrayList<>(prodList);
                    addElementsToTableLayout(prodList);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.d("EXC","Exception occurred in performingOperationAfterFetching");
                }
            }

        }

        private void addElementsToTableLayout(ArrayList<ProductPOJO> prodList) {
            TableLayout tableLayout  = (TableLayout)findViewById(R.id.tableLayoutProducts);
            tableLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) HomeUserActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            distUserIDsProdList=new ArrayList<>();
            prodIDsList=new ArrayList<>();
            while(0!=prodList.size()){
                TableRow tableRow = new TableRow(HomeUserActivity.this);
                //tableRow.setBackgroundResource(R.drawable.border);
                for(int itr=0;itr<2;itr++){
                    if(0==prodList.size()){
                        TextView tv = new TextView(HomeUserActivity.this);
                        tableRow.addView(tv);
                        break;
                    }
                    View viewForTableRow = inflater.inflate(R.layout.prod_layout_for_table, null,false);
                    ImageView prodImg = (ImageView) viewForTableRow
                            .findViewById(R.id.prodImage);
                    String imgStr = prodList.get(0).getProductImage();
                    imgStr = imgStr.substring(imgStr.indexOf(",")+1);
                    InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
                    Bitmap bitmap = BitmapFactory.decodeStream(new  FlushedInputStream(isImage));
                    prodImg.setImageBitmap(bitmap);
                    prodImg.getLayoutParams().height = HEIGHT/2;
                    prodImg.getLayoutParams().width = WIDTH/2;
                    prodImg.requestLayout();
                    TextView prodNameTxt = (TextView)viewForTableRow.findViewById(R.id.prodName);
                    TextView prodDescTxt = (TextView)viewForTableRow.findViewById(R.id.prodDesc);
                    TextView prodPriceTxt = (TextView)viewForTableRow.findViewById(R.id.prodPrice);
                    TextView prodDiscPriceTxt = (TextView)viewForTableRow.findViewById(R.id.prodDiscountedPrice);
                    prodNameTxt.setText(prodList.get(0).getProductName());
                    prodDescTxt.setText(prodList.get(0).getProductDesc());
                    prodPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodList.get(0).getProductRate())));
                    prodPriceTxt.setPaintFlags(prodPriceTxt.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                    prodDiscPriceTxt.setText(getResources().getString(R.string.rsSymbol)+String.format("%.2f",Float.parseFloat(prodList.get(0).getProductRate())-Float.parseFloat(prodList.get(0).getProductDiscount())));
                    TextView addCartBtn = (TextView) viewForTableRow.findViewById(R.id.addCartButton);
                    TextView buyProdBtn = (TextView) viewForTableRow.findViewById(R.id.buyNowButton);
                    addCartBtn.setId(Integer.parseInt("777"+prodList.get(0).getProductID()+"777"));
                    buyProdBtn.setId(Integer.parseInt("555"+prodList.get(0).getProductID()+"555"));
                    viewForTableRow.setId(Integer.parseInt("555"+prodList.get(0).getProductID()+"555"));
                    if(itr==0){
                        GenericUtil.setMargins(viewForTableRow,10,0,0,0);
                        GenericUtil.setMarginStart(viewForTableRow,10);
                    }
                    else{
                        GenericUtil.setMargins(viewForTableRow,0,0,10,0);
                        GenericUtil.setMarginEnd(viewForTableRow,10);
                    }
                    tableRow.addView(viewForTableRow);
                    setRequiredFieldsToList(prodList.get(0));
                    prodList.remove(0);
                }
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
    }

    private void makeProductButtonsClickable() {
        for(String cartBtn:prodIDsList){
            findViewById(Integer.parseInt("777"+cartBtn+"777")).setOnClickListener(this);
            findViewById(Integer.parseInt("555"+cartBtn+"555")).setOnClickListener(this);
        }
    }

    private void setRequiredFieldsToList(ProductPOJO productPOJO) {
        distUserIDsProdList.add(productPOJO.getUserID());
        prodIDsList.add(productPOJO.getProductID());
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
