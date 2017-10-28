package in.goods24.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import in.goods24.common.ChangePwdActivity;
import in.goods24.common.MainActivity;
import in.goods24.R;
import in.goods24.common.UpdateProfile;
import in.goods24.common.ViewProfile;
import in.goods24.util.ConstantsUtil;

public class HomeUserActivity extends AppCompatActivity {
    private int backButtonCount= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        SharedPreferences sharedpreferences = getSharedPreferences(ConstantsUtil.MyPREFERENCES, Context.MODE_PRIVATE);
        String userTypeName =  sharedpreferences.getString("loginUserTypeName","");
        TextView userTypeTextView= (TextView)findViewById(R.id.userTypeTextView);
        userTypeTextView.setText(userTypeName);

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
