package mfy.com.hotupdatedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void calculate(View v) {
        Toast.makeText(this, TestUtil.calculate() + "", Toast.LENGTH_SHORT).show();
    }

    public void fix(View v) {
        FixUtil.fix(this, Environment.getExternalStorageState() + File.separator + Const.PATCH_NAME);
    }

}
