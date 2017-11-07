package in.goods24.distributor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AsyncPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.EntityUtils;
import in.goods24.R;
import in.goods24.home.HomeDistributorActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;

import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.WIDTH;

public class ViewProductDistActivity extends AppCompatActivity {
    private String selectedProdId;
    private RequestParams rp;
    private  String phpName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product_dist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        selectedProdId=extras.getString("SelectedProductID");
        fetchProductDetails();
    }

    private void fetchProductDetails() {
        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String loginUserID = sharedpreferences.getString("loggedInUserID", "");
        rp = new RequestParams();
        rp.add("appId", "g24API7");
        rp.add("pwd", "API7g24");
        rp.add("oprn","view");
        rp.add("user_id", loginUserID);
        rp.add("product_id",selectedProdId);
        phpName = "manageProducts.php";
        new ViewProductHelper().execute();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,HomeDistributorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_LONG)
                .show();
    }



    private class ViewProductHelper extends AsyncTask<String,String,String>{
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
                    //Code that uses AsyncHttpClient in your case ConsultaCaract()
                    makeViewProdRestCall(rp,phpName);
                }
            };
            mainHandler.post(myRunnable);

            return null;
        }

        private void makeViewProdRestCall(RequestParams rp, String phpName) {


            HttpUtils.post(phpName, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                    try {

                        respMsg = (String) res.getString("res");
                        errCode = (Integer) res.getInt("error_code");
                        Log.d("res", "Response message is>>>>" + respMsg);
                        performingSuccessOperation(respMsg,errCode,res);
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

        private void performingSuccessOperation(String respMsg, int errCode,JSONObject res) {
            if(errCode==0){
                try {
                    TextView prodNameHeader = (TextView) findViewById(R.id.prodNameHeader);
                    TextView prodDesText = (TextView) findViewById(R.id.productDesText);
                    TextView productRateText = (TextView) findViewById(R.id.productRateText);
                    TextView productDiscText = (TextView) findViewById(R.id.productDiscText);
                    TextView productQuantText = (TextView) findViewById(R.id.productQuantText);
                    TextView productApprovalText = (TextView) findViewById(R.id.productApprovalText);
                    ImageView prodViewImage=(ImageView)findViewById(R.id.prodViewImage);
                    String imgStr = res.getString("product_image");
                    imgStr = imgStr.substring(imgStr.indexOf(",")+1);
                    InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
                    Bitmap bitmap = BitmapFactory.decodeStream(new  FlushedInputStream(isImage));
                    prodViewImage.setImageBitmap(bitmap);
                    prodViewImage.getLayoutParams().height = HEIGHT;
                    prodViewImage.getLayoutParams().width = WIDTH;
                    prodViewImage.requestLayout();
                    prodNameHeader.setText(res.getString("product_name"));
                    prodDesText.setText(res.getString("product_desc"));
                    productRateText.setText(res.getString("product_rate"));
                    productDiscText.setText(res.getString("product_discount"));
                    productQuantText.setText(res.getString("product_quantity"));
                    productApprovalText.setText("Y".equalsIgnoreCase(res.getString("is_approved"))?"Approved":"Pending");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
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
