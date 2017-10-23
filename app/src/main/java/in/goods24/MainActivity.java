package in.goods24;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import in.goods24.dialog.CustomDialogUserType;
import in.goods24.util.ConstantsUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
private boolean isRegPressed=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String loginUserTypeID =  sharedpreferences.getString("selectedUserTypeID","");
        String loginUserTypeName = sharedpreferences.getString("loginUserTypeName","");
        String loggedInUserID =  sharedpreferences.getString("loggedInUserID","");
        if(!"".equalsIgnoreCase(loginUserTypeID)&&!"".equalsIgnoreCase(loginUserTypeName)
                &&!"".equalsIgnoreCase(loggedInUserID)){
            Intent i = new Intent(this,HomeActivity.class);
            startActivity(i);
        }
        else {
            setContentView(R.layout.activity_main);
            RelativeLayout relLayoutProgress = (RelativeLayout) findViewById(R.id.progressBarViewProfLayout);
            relLayoutProgress.setVisibility(View.VISIBLE);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            findViewById(R.id.regButton).setOnClickListener(this);
            findViewById(R.id.loginButton).setOnClickListener(this);
            relLayoutProgress.setVisibility(View.GONE);
        }
        /*this.finish();
        System.exit(0);*/
    }
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.regButton:
                isRegPressed=true;
                showUserTypeDialog();
                break;
            case R.id.loginButton:
                isRegPressed=false;
                showUserTypeDialog();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onUtypeRadio(View v){
        Intent i=null;
        if(isRegPressed)
            i = new Intent(this,RegisterActivity.class);
        else
            i = new Intent(this,LoginActivity.class);
        int id = v.getId();
        String utype = "";
        if(id==R.id.userRadio){
            utype = "5";
        }
        else if(id==R.id.distributorRadio){
            utype = "4";
        }

        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.putString("selectedUserTypeID",utype);
        editor.commit();
        startActivity(i);
    }
    public void showUserTypeDialog(){
        CustomDialogUserType customDialog= new CustomDialogUserType();
        customDialog.show(getSupportFragmentManager(), "CustomDialogFragment");
    }
}
