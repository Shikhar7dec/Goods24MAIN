package in.goods24.distributor;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;
import in.goods24.R;
import in.goods24.common.MainActivity;
import in.goods24.home.HomeDistributorActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;
import pub.devrel.easypermissions.EasyPermissions;

import static android.provider.MediaStore.Images.Media.getBitmap;
import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.IMGDIVFACTOR;
import static in.goods24.util.ConstantsUtil.WIDTH;
import static in.goods24.util.ConstantsUtil.tempImgFilePath;

public class UpdateProductDistActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_WRITE_STORAGE = 112;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String imgPath;
    Bitmap bitmap;
    private String selectedProdId;
    private RequestParams rp;
    private  String phpName;
    File prodImageFile;
    private Dialog imgDialog;
    private boolean isCameraFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product_dist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        selectedProdId=extras.getString("SelectedProductID");
        fetchProductDetails();
        Boolean hasPermission = (ContextCompat.checkSelfPermission(UpdateProductDistActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(UpdateProductDistActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        findViewById(R.id.changeImageUpdateBtn).setOnClickListener(this);
        findViewById(R.id.updateBtn).setOnClickListener(this);
        findViewById(R.id.resetBtn).setOnClickListener(this);

    }
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.changeImageUpdateBtn:
                if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
                    selectImageFromGallery();
                } else {
                    EasyPermissions.requestPermissions(this, "Access for storage",
                            101, galleryPermissions);
                }
                break;
            case R.id.updateBtn:
                onUpdateProduct();
                break;
            case R.id.resetBtn:
               onReset();
                break;
            case R.id.imageButtonCamera:
                isCameraFlag = true;
                selectImage();
                break;
            case R.id.imageButtonGallery:
                isCameraFlag = false;
                selectImage();
                break;
            case R.id.cancelButtonDialogImg:
                imgDialog.dismiss();
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,HomeDistributorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void onReset() {
        Bundle b = new Bundle();
        b.putString("SelectedProductID",selectedProdId);
        Intent i = new Intent(this, UpdateProductDistActivity.class);
        i.putExtras(b);
        startActivity(i);
    }
    private void onUpdateProduct() {
            EditText prodNameEditText = (EditText) findViewById(R.id.prodNameEditText);
            EditText productDesEditText = (EditText) findViewById(R.id.productDesEditText);
            EditText productQuantEditText = (EditText) findViewById(R.id.productQuantEditText);
            EditText productRateEditText = (EditText) findViewById(R.id.productRateEditText);
            EditText productDiscEditText = (EditText) findViewById(R.id.productDiscEditText);

            boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(prodNameEditText) ||
                    ValidationUtil.isEmptyTextField(productDesEditText) ||
                    ValidationUtil.isEmptyTextField(productQuantEditText) ||
                    ValidationUtil.isEmptyTextField(productRateEditText) ||
                    ValidationUtil.isEmptyTextField(productDiscEditText);
            if (!areAllFieldsEmpty) {
                try {
                    Log.d("", "Path is>>" + imgPath);

                    prodImageFile = new File(tempImgFilePath);
                    prodImageFile.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,80, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    FileOutputStream fos = new FileOutputStream(prodImageFile);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
                    String loginUserID = sharedpreferences.getString("loggedInUserID", "");
                    rp = new RequestParams();
                    rp.add("appId", "g24API7");
                    rp.add("pwd", "API7g24");
                    rp.add("oprn","edit");
                    rp.put("product_image", prodImageFile);
                    rp.add("user_id", loginUserID);
                    rp.add("product_id",selectedProdId);
                    rp.add("product_name", prodNameEditText.getText().toString());
                    rp.add("product_desc", productDesEditText.getText().toString());
                    rp.add("product_quantity", productQuantEditText.getText().toString());
                    rp.add("product_rate", productRateEditText.getText().toString());
                    rp.add("product_discount", productDiscEditText.getText().toString());
                    phpName = "manageProducts.php";
                    Log.d("REQ","Request is>>>"+rp);
                    new FileAndDataSendHelper().execute();
                    //makeAddProdRestCall(rp, phpName);


                } catch (Exception e) {
                    e.printStackTrace();
                    showValidationMsg("Some error occurred please try after some time");
                }
            } else {
                showValidationMsg("Please enter all fields");
            }

    }

    private void selectImageFromGallery() {
        imgDialog = new Dialog(this);
        imgDialog.setContentView(R.layout.dialog_image_selection);
        imgDialog.setTitle("Select Image");
        imgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        imgDialog.show();
        imgDialog.findViewById(R.id.cancelButtonDialogImg).setOnClickListener(this);
        imgDialog.findViewById(R.id.imageButtonCamera).setOnClickListener(this);
        imgDialog.findViewById(R.id.imageButtonGallery).setOnClickListener(this);
    }
    private void selectImage() {
        if(isCameraFlag){
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, 0);
        }
        else{
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , 1);
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
                bitmap = (Bitmap) data.getExtras().get("data");
                bitmap = getScaledImage(bitmap,4*IMGDIVFACTOR);
                ImageView imgView = (ImageView) findViewById(R.id.prodUpdateImage);
                imgView.setImageBitmap(bitmap);
                imgView.getLayoutParams().height = HEIGHT;
                imgView.getLayoutParams().width = ConstantsUtil.WIDTH;
                imgView.requestLayout();
            }
            if (resultCode == RESULT_OK && requestCode == 1) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    imgPath = selectedImageUri.getPath();
                    Log.d("DIS", "Image Path : " + imgPath);
                    bitmap = getBitmap(this.getContentResolver(), selectedImageUri);
                    bitmap = getScaledImage(bitmap,2*IMGDIVFACTOR);
                    ImageView imgView = (ImageView) findViewById(R.id.prodUpdateImage);
                    imgView.setImageBitmap(bitmap);
                    imgView.getLayoutParams().height = HEIGHT;
                    imgView.getLayoutParams().width = ConstantsUtil.WIDTH;
                    imgView.requestLayout();
                }
            }
            imgDialog.dismiss();
        }
        catch(Exception e){
            e.printStackTrace();
            showValidationMsg("Some error occurred");
        }
    }
    public Bitmap getScaledImage(Bitmap bitmap, int particularDivisionFactor) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int heightFactor =height/particularDivisionFactor;
        int widthFactor = width/particularDivisionFactor;
        int averageFactor = (heightFactor+widthFactor)/2;
        averageFactor = (0==averageFactor)?1:averageFactor;
        height=height/averageFactor;
        width=width/averageFactor;
        bitmap=Bitmap.createScaledBitmap(bitmap, width,height , true);
        return bitmap;
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

    private void showValidationMsg(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_LONG)
                .show();
    }
    private void deleteFileFromPath(String path) {
        try {
            File fileToDelete = new File(path);
            fileToDelete.delete();
            if (fileToDelete.exists()) {
                fileToDelete.getCanonicalFile().delete();
                if (fileToDelete.exists()) {
                    getApplicationContext().deleteFile(fileToDelete.getName());
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    private class ViewProductHelper extends AsyncTask<String,String,String> {
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
                    EditText prodNameEditText = (EditText) findViewById(R.id.prodNameEditText);
                    TextView prodDesEditText = (TextView) findViewById(R.id.productDesEditText);
                    TextView productRateEditText = (TextView) findViewById(R.id.productRateEditText);
                    TextView productDiscEditText = (TextView) findViewById(R.id.productDiscEditText);
                    TextView productQuantEditText = (TextView) findViewById(R.id.productQuantEditText);
                    ImageView prodUpdateImage=(ImageView)findViewById(R.id.prodUpdateImage);
                    String imgStr = res.getString("product_image");
                    imgStr = imgStr.substring(imgStr.indexOf(",")+1);
                    InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
                    bitmap = BitmapFactory.decodeStream(new  FlushedInputStream(isImage));
                    prodUpdateImage.setImageBitmap(bitmap);
                    prodUpdateImage.getLayoutParams().height = HEIGHT;
                    prodUpdateImage.getLayoutParams().width = WIDTH;
                    prodUpdateImage.requestLayout();
                    prodNameEditText.setText(res.getString("product_name"));
                    prodDesEditText.setText(res.getString("product_desc"));
                    productRateEditText.setText(res.getString("product_rate"));
                    productDiscEditText.setText(res.getString("product_discount"));
                    productQuantEditText.setText(res.getString("product_quantity"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void performingSuccessOperationOnUpadte(String respMsg, int errCode) {
        if(errCode==0){
            showValidationMsg(respMsg);
            onBackPressed();
        }
        else{
            showValidationMsg(respMsg);
        }
    }
    private class FileAndDataSendHelper extends AsyncTask<String, String, String> {
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
                    makeUpdateProdRestCall(rp,phpName);
                }
            };
            mainHandler.post(myRunnable);

            return null;
        }
        private void makeUpdateProdRestCall(RequestParams rp, String phpName) {

            HttpUtils.post(phpName, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                    try {
                        deleteFileFromPath(tempImgFilePath);
                        /*prodImageFile.delete();*/
                        respMsg = (String) res.getString("res");
                        errCode = (Integer) res.getInt("error_code");
                        Log.d("res", "Response message is>>>>" + respMsg);
                        performingSuccessOperationOnUpadte(respMsg,errCode);
                    } catch (Exception e) {
                        deleteFileFromPath(tempImgFilePath);
                        e.printStackTrace();
                        showValidationMsg("Please check your internet and try again");
                        /*RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                        relLayoutProgress.setVisibility(View.GONE);*/
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString,
                                      Throwable throwable) {
                    deleteFileFromPath(tempImgFilePath);
                    Log.d("log", "Status code is>>" + statusCode + "Response code>>>" + responseString);
                    showValidationMsg("Some Error occurred please try again");
                    /*RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarLayout);
                    relLayoutProgress.setVisibility(View.GONE);*/
                }
            });

        }

        @Override
        protected void onPostExecute(String result) {

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