package xssoft.club.minio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;
import io.minio.MinioClient;

public class LoginActivity extends AppCompatActivity {

    EditText[] texts = new EditText[3];
    private SharedPreferences pef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pef = getSharedPreferences(getString(R.string.prefreence_file_key), Context.MODE_PRIVATE);
        texts[0] = findViewById(R.id.server);
        texts[1] = findViewById(R.id.key);
        texts[2] = findViewById(R.id.token);
        texts[0].setText(pef.getString("address", ""));
        texts[1].setText(pef.getString("key", ""));
        texts[2].setText(pef.getString("token", ""));
    }

    public void login(View view) {
        String address = texts[0].getText().toString();
        String key = texts[1].getText().toString();
        String token = texts[2].getText().toString();
        pef.edit().putString("address", address)
                .putString("key", key)
                .putString("token", token).apply();
        try{
            MinioClient.builder().endpoint(address).credentials(key, token).build().listBuckets();
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            //Toasty.success(intent,this.getString(R.string.login_success), Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toasty.error(this,this.getString(R.string.login_error), Toast.LENGTH_LONG).show();
        }
    }

}