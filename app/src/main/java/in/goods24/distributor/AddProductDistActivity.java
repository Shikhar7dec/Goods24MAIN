package in.goods24.distributor;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import in.goods24.R;
import in.goods24.common.MainActivity;
import in.goods24.home.HomeDistributorActivity;
import in.goods24.util.ConstantsUtil;
import in.goods24.util.HttpUtils;
import in.goods24.util.ValidationUtil;
import pub.devrel.easypermissions.EasyPermissions;
import static android.provider.MediaStore.Images.Media.getBitmap;
import static in.goods24.util.ConstantsUtil.IMGDIVFACTOR;
import static in.goods24.util.ConstantsUtil.tempImgFilePath;

public class AddProductDistActivity extends AppCompatActivity
        implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    String imgPath;
    ImageView imageview;
    int totalSize = 0;
    private static final int REQUEST_WRITE_STORAGE = 112;
    File prodImageFile;
    RequestParams rp;
    String phpName;
    private Bitmap bitmap=null;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] catProdValues;
    private String[] catProdIds;
    private String selectedCategoryID="0";
    private static final int CAMERA_REQUEST = 1888;
    private Dialog imgDialog;
    private boolean isCameraFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_dist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Boolean hasPermission = (ContextCompat.checkSelfPermission(AddProductDistActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(AddProductDistActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        fetchProdSpinnerValues();
        findViewById(R.id.selectImageButtonAdd).setOnClickListener(this);
        findViewById(R.id.addItemButton).setOnClickListener(this);
        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        // String userTypeName = sharedpreferences.getString("loginUserTypeName", "");
    }


    private void fetchProdSpinnerValues() {
        RequestParams requestParams =new RequestParams();
        requestParams.add("appId", "g24API7");
        requestParams.add("pwd", "API7g24");
        requestParams.add("oprn","getProductType");
        phpName = "manageProducts.php";
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
            Spinner prodCatValSpinner = (Spinner)findViewById(R.id.prodCategorySpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item,catProdValues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            prodCatValSpinner.setAdapter(adapter);
            prodCatValSpinner.setSelection(0);
            prodCatValSpinner.setOnItemSelectedListener(this);
        }
        else{
            showValidationMsg(respMsg);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if(0!=position){
            selectedCategoryID = catProdIds[position];
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        showValidationMsg("Please select any category");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selectImageButtonAdd:
                if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
                    selectImageFromGallery();
                } else {
                    EasyPermissions.requestPermissions(this, "Access for storage",
                            101, galleryPermissions);
                }
                break;
            case R.id.addItemButton:
                onAddItem();
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

    private void onAddItem() {


        if (null != bitmap) {
            EditText prodNameText = (EditText) findViewById(R.id.prodNameAddText);
            EditText prodDescText = (EditText) findViewById(R.id.prodDescAddText);
            EditText prodQuantText = (EditText) findViewById(R.id.prodQuantAddText);
            EditText prodRateText = (EditText) findViewById(R.id.prodRateAddText);
            EditText prodDisText = (EditText) findViewById(R.id.prodDiscountAddText);

            boolean areAllFieldsEmpty = ValidationUtil.isEmptyTextField(prodNameText) ||
                    ValidationUtil.isEmptyTextField(prodDescText) ||
                    ValidationUtil.isEmptyTextField(prodQuantText) ||
                    ValidationUtil.isEmptyTextField(prodRateText) ||
                    ValidationUtil.isEmptyTextField(prodDisText);
            if (!areAllFieldsEmpty) {
                if(!"0".equalsIgnoreCase(selectedCategoryID)){
                    try {
                        Log.d("", "Path is>>" + imgPath);

                        prodImageFile = new File(tempImgFilePath);
                        prodImageFile.createNewFile();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.WEBP,100, bos);
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
                        rp.add("oprn","add");
                        rp.put("product_image", prodImageFile);
                        rp.add("user_id", loginUserID);
                        rp.add("category_id", selectedCategoryID);
                        rp.add("product_name", prodNameText.getText().toString());
                        rp.add("product_desc", prodDescText.getText().toString());
                        rp.add("product_quantity", prodQuantText.getText().toString());
                        rp.add("product_rate", prodRateText.getText().toString());
                        rp.add("product_discount", prodDisText.getText().toString());
                        phpName = "manageProducts.php";
                        Log.d("REQ","Request is>>>"+rp);
                        new FileAndDataSendHelper().execute();


                    } catch (Exception e) {
                        e.printStackTrace();
                        showValidationMsg("Some error occurred please try after some time");
                    }
                }
                else{
                    showValidationMsg("Please select proper Category");
                }
            }
            else {
                showValidationMsg("Please enter all fields");
            }
        } else {
            showValidationMsg("Please select Image");
        }
    }



    private void showAddProdResp(String respMsg, int errCode) {
        showValidationMsg(respMsg);
        if (errCode == 0) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
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
                ImageView imgView = (ImageView) findViewById(R.id.addProdImage);
                imgView.setImageBitmap(bitmap);
                imgView.getLayoutParams().height = ConstantsUtil.HEIGHT;
                imgView.getLayoutParams().width = ConstantsUtil.WIDTH;
                imgView.requestLayout();
            }
            if (resultCode == Activity.RESULT_OK && requestCode == 1) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    imgPath = selectedImageUri.getPath();
                    Log.d("DIS", "Image Path : " + imgPath);
                    bitmap = getBitmap(this.getContentResolver(), selectedImageUri);
                    bitmap = getScaledImage(bitmap,2*IMGDIVFACTOR);
                    ImageView imgView = (ImageView) findViewById(R.id.addProdImage);
                    imgView.setImageBitmap(bitmap);
                    imgView.getLayoutParams().height = ConstantsUtil.HEIGHT;
                    imgView.getLayoutParams().width = ConstantsUtil.WIDTH;
                    imgView.requestLayout();
                }
            }
            imgDialog.dismiss();
        }
        catch (Exception e){
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

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,HomeDistributorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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
                    //Code that uses AsyncHttpClient in your case ConsultaCaract()
                    makeAddProdRestCall(rp,phpName);
                }
            };
            mainHandler.post(myRunnable);

            return null;
        }
        private void makeAddProdRestCall(RequestParams rp, String phpName) {
            HttpUtils.post(phpName, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    Log.d("res", "---------------response from host>>> " + res + "Status is" + statusCode);
                    try {
                        deleteFileFromPath(tempImgFilePath);
                        respMsg = res.getString("res");
                        errCode = res.getInt("error_code");
                        Log.d("res", "Response message is>>>>" + respMsg);
                        performingSuccessOperation(respMsg,errCode);
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
    private void performingSuccessOperation(String respMsg, int errCode) {
        if(errCode==0){
            deleteFileFromPath(tempImgFilePath);
            showValidationMsg(respMsg);
            Intent i = new Intent(this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

        }
        else{
            deleteFileFromPath(tempImgFilePath);
            showValidationMsg(respMsg);
        }
    }

}
